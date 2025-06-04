package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;
import br.com.unicat.poc.entities.AnalysedCode;
import br.com.unicat.poc.entities.AnalysedLogic;
import br.com.unicat.poc.prompts.AnalyseCodeAndIdentifyDependenciesPromptGenerator;
import br.com.unicat.poc.prompts.AnalyseLogicAndIdentityScenariosPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.InitUnitTestsCreationInterface;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;

import java.util.Objects;

public class InitUnitTestsCreationUseCase implements InitUnitTestsCreationInterface {
  private final AnalyseCodeAndIdentifyDependenciesPromptGenerator
      analyseCodeAndIdentifyDependenciesPromptGenerator;
  private final AnalyseLogicAndIdentityScenariosPromptGenerator
      analyseLogicAndIdentityScenariosPromptGenerator;
  private final B3GPTGateway gateway;

  public InitUnitTestsCreationUseCase(
      AnalyseCodeAndIdentifyDependenciesPromptGenerator
          analyseCodeAndIdentifyDependenciesPromptGenerator,
      AnalyseLogicAndIdentityScenariosPromptGenerator
          analyseLogicAndIdentityScenariosPromptGenerator,
      B3GPTGateway gateway) {
    this.analyseCodeAndIdentifyDependenciesPromptGenerator =
        analyseCodeAndIdentifyDependenciesPromptGenerator;
    this.analyseLogicAndIdentityScenariosPromptGenerator =
        analyseLogicAndIdentityScenariosPromptGenerator;
    this.gateway = gateway;
  }

  @Override
  public InitResponseDTO execute() {
    // Prompt 1
    final AnalysedCode analysedCode = this.analyseCodeAndIdentifyDependencies();

    return null;
  }

//  private AnalysedLogic analyseLogicAndIdentifyScenarios(final AnalysedCode analysedCode) {
//	 final dependenciesName analysedCode.getCustomDependencies()
//
//    final Prompt prompt = this.analyseLogicAndIdentityScenariosPromptGenerator.get("", "");
//    final ChatResponse chatResponse = this.gateway.callAPI(prompt);
//    final AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
//    final BeanOutputConverter<AnalysedLogic> converter =
//        new BeanOutputConverter<>(AnalysedLogic.class);
//    return converter.convert(Objects.requireNonNull(assistantMessage.getText()));
//  }

  private AnalysedCode analyseCodeAndIdentifyDependencies() {
    final Prompt prompt = this.analyseCodeAndIdentifyDependenciesPromptGenerator.get();
    final ChatResponse chatResponse = this.gateway.callAPI(prompt);
    final AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
    final BeanOutputConverter<AnalysedCode> converter =
        new BeanOutputConverter<>(AnalysedCode.class);
    return converter.convert(Objects.requireNonNull(assistantMessage.getText()));
  }
}
