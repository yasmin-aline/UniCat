package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.response.CompleteResponseDTO;
import br.com.unicat.poc.entities.GeneratedClasse;
import br.com.unicat.poc.prompts.GenerateUnitTestsPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.CompleteUnitTestsCreationInterface;
import java.util.Objects;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;

public class CompleteUnitTestsCreationUseCase implements CompleteUnitTestsCreationInterface {

  private final GenerateUnitTestsPromptGenerator generateUnitTestsPromptGenerator;
  private final B3GPTGateway gateway;

  public CompleteUnitTestsCreationUseCase(
      GenerateUnitTestsPromptGenerator generateUnitTestsPromptGenerator, B3GPTGateway gateway) {
    this.generateUnitTestsPromptGenerator = generateUnitTestsPromptGenerator;
    this.gateway = gateway;
  }

  @Override
  public CompleteResponseDTO execute() {
    // Prompt 3
    final GeneratedClasse generated = this.generateUnitTests();

    return CompleteResponseDTO.builder()
        .generatedTestClassFqn(generated.getGeneratedTestClassFqn())
        .generatedTestCode(generated.getGeneratedTestCode())
        .build();
  }

  private GeneratedClasse generateUnitTests() {
    final Prompt prompt = this.generateUnitTestsPromptGenerator.get();
    final ChatResponse chatResponse = this.gateway.callAPI(prompt);
    final AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
    final BeanOutputConverter<GeneratedClasse> converter =
        new BeanOutputConverter<>(GeneratedClasse.class);
    return converter.convert(Objects.requireNonNull(assistantMessage.getText()));
  }
}
