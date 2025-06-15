package br.com.unicat.poc.v1.usecases;

import br.com.unicat.poc.v1.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.v1.entities.AnalysedLogic;
import br.com.unicat.poc.v1.prompts.AnalyseLogicAndIdentityScenariosPromptGenerator;
import br.com.unicat.poc.v1.usecases.interfaces.AnalyseLogicAndIdentifyScenariosInterface;
import br.com.unicat.poc.v1.usecases.utilities.JsonLlmResponseParser;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyseLogicAndIdentifyScenariosUseCase
    implements AnalyseLogicAndIdentifyScenariosInterface {
  private final AnalyseLogicAndIdentityScenariosPromptGenerator
      analyseLogicAndIdentityScenariosPromptGenerator;
  private final B3GPTGateway b3gptGateway;

  @Value("classpath:/mocks/prompt-2-chat-response.st")
  private Resource mockChatResponse;

  @Override
  public AnalysedLogic execute(final String dependenciesName, final String dependencies)
      throws Exception {
    log.info(
        "INIT AnalyseLogicAndIdentifyScenariosUseCase execute. dependenciesName: {}",
        dependenciesName);
    final var prompt =
        this.analyseLogicAndIdentityScenariosPromptGenerator.get(dependencies, dependenciesName);

        final var chatResponse =  this.b3gptGateway.callAPI(prompt);
        final var assistantMessage = chatResponse.getResult().getOutput();

    final var analysedLogic =
        JsonLlmResponseParser.parseLlmResponse(
//            new AssistantMessage(mockChatResponse.getContentAsString(Charset.defaultCharset())),
            assistantMessage,
            new TypeReference<AnalysedLogic>() {});

    log.info(
        "END AnalyseLogicAndIdentifyScenariosUseCase execute. analysedLogic: {}", analysedLogic);
    return analysedLogic;
  }
}
