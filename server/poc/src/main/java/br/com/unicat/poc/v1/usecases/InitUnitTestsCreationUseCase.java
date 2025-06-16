package br.com.unicat.poc.v1.usecases;

import br.com.unicat.poc.shared.gateway.B3GPTGateway;
import br.com.unicat.poc.shared.utilities.JsonLlmResponseParser;
import br.com.unicat.poc.v1.controller.dtos.response.AnalysisResponseDTO;
import br.com.unicat.poc.v1.controller.dtos.response.InitResponseDTO;
import br.com.unicat.poc.v1.entities.AnalysedCode;
import br.com.unicat.poc.v1.prompts.AnalyseCodeAndIdentifyDependenciesPromptGenerator;
import br.com.unicat.poc.v1.usecases.interfaces.InitUnitTestsCreationInterface;
import com.fasterxml.jackson.core.type.TypeReference;
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

    final AnalysedCode analysedCode =
        JsonLlmResponseParser.parseLlmResponse(
            assistantMessage, new TypeReference<AnalysedCode>() {});
    assert analysedCode != null;

    return InitResponseDTO.builder()
        .analysisResponseDTO(
            AnalysisResponseDTO.builder()
                .classFqn(analysedCode.getAnalysis().getClassFqn())
                .purposeSummary(analysedCode.getAnalysis().getPurposeSummary())
                .mainMethodSignature(analysedCode.getAnalysis().getMainMethodSignature())
                .inputType(analysedCode.getAnalysis().getInputType())
                .outputType(analysedCode.getAnalysis().getOutputType())
                .build())
        .customDependencies(analysedCode.getCustomDependencies())
        .build();
  }
}
