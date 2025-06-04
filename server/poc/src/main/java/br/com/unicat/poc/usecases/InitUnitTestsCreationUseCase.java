package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.response.AnalysisResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;
import br.com.unicat.poc.entities.AnalysedCode;
import br.com.unicat.poc.prompts.AnalyseCodeAndIdentifyDependenciesPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.InitUnitTestsCreationInterface;
import br.com.unicat.poc.usecases.utilities.JsonLlmResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitUnitTestsCreationUseCase implements InitUnitTestsCreationInterface {
  private final AnalyseCodeAndIdentifyDependenciesPromptGenerator
      analyseCodeAndIdentifyDependenciesPromptGenerator;
  private final B3GPTGateway gateway;

  @Override
  public InitResponseDTO execute() throws Exception {
    final Prompt prompt = this.analyseCodeAndIdentifyDependenciesPromptGenerator.get();
    final ChatResponse chatResponse = this.gateway.callAPI(prompt);
    final AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

    final AnalysedCode analysedCode = JsonLlmResponseParser.parseLlmResponse(assistantMessage, AnalysedCode.class);

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
