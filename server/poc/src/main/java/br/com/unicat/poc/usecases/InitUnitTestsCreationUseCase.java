package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.response.AnalysisResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;
import br.com.unicat.poc.entities.AnalysedCode;
import br.com.unicat.poc.prompts.AnalyseCodeAndIdentifyDependenciesPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.InitUnitTestsCreationInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class InitUnitTestsCreationUseCase implements InitUnitTestsCreationInterface {
  private final AnalyseCodeAndIdentifyDependenciesPromptGenerator
      analyseCodeAndIdentifyDependenciesPromptGenerator;
  private final B3GPTGateway gateway;

  public InitUnitTestsCreationUseCase(
      AnalyseCodeAndIdentifyDependenciesPromptGenerator
          analyseCodeAndIdentifyDependenciesPromptGenerator,
      B3GPTGateway gateway) {
    this.analyseCodeAndIdentifyDependenciesPromptGenerator =
        analyseCodeAndIdentifyDependenciesPromptGenerator;
    this.gateway = gateway;
  }

  @Override
  public InitResponseDTO execute() throws JsonProcessingException {
    final Prompt prompt = this.analyseCodeAndIdentifyDependenciesPromptGenerator.get();
    final ChatResponse chatResponse = this.gateway.callAPI(prompt);
    final AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

    final ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    String rawText = Objects.requireNonNull(assistantMessage.getText()).trim();

    if (rawText.startsWith("```json")) {
        rawText = rawText.substring("```json".length()).trim();
    }

    if (rawText.endsWith("```")) {
        rawText = rawText.substring(0, rawText.length() - 3).trim();
    }

    final AnalysedCode analysedCode = objectMapper.readValue(rawText, AnalysedCode.class);

    assert analysedCode != null;
    return InitResponseDTO.builder()
        .analysisResponseDTO(
            AnalysisResponseDTO.builder()
                .classFqn(analysedCode.getAnalysis().getClassFqn())
                .purposeSummary(analysedCode.getAnalysis().getPurposeSummary())
                .mainMethodSignature(analysedCode.getAnalysis().getMainMethodSignature())
                .inputType(analysedCode.getAnalysis().getInputType())
                .outputType(analysedCode.getAnalysis().getOutputType())
                .build()
        )
        .customDependencies(analysedCode.getCustomDependencies())
        .build();
  }
}
