package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.CompleteResponseDTO;
import br.com.unicat.poc.entities.AnalysedLogic;
import br.com.unicat.poc.entities.GeneratedClass;
import br.com.unicat.poc.prompts.GenerateUnitTestsPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.CompleteUnitTestsCreationInterface;
import br.com.unicat.poc.usecases.utilities.JsonLlmResponseParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteUnitTestsCreationUseCase implements CompleteUnitTestsCreationInterface {
  private final GenerateUnitTestsPromptGenerator generateUnitTestsPromptGenerator;
  private final B3GPTGateway gateway;

  @Override
  public CompleteResponseDTO execute(
      CompleteRequestDTO requestDTO, AnalysedLogic analysedLogic)
      throws Exception {
    log.info(
        "INIT CompleteUnitTestsCreationUseCase execute. request: {}, analysed logic: {}",
        requestDTO,
        analysedLogic);

    ObjectMapper mapper =
        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    String testScenariosJson = mapper.writeValueAsString(analysedLogic.testScenarios());

    final var prompt =
        this.generateUnitTestsPromptGenerator.get(
            requestDTO.dependenciesName(),
            requestDTO.dependencies(),
            testScenariosJson,
            requestDTO.guidelines());

    final var chatResponse = this.gateway.callAPI(prompt);
    final var assistantMessage = chatResponse.getResult().getOutput();

    final var generatedClass =
        JsonLlmResponseParser.parseLlmResponse(
            assistantMessage, new TypeReference<GeneratedClass>() {});
    assert generatedClass != null;

    log.info("END CompleteUnitTestsCreationUseCase execute. generatedClass: {}", generatedClass);
    return CompleteResponseDTO.builder()
        .generatedTestClassFqn(generatedClass.getGeneratedTestClassFqn())
        .generatedTestCode(generatedClass.getGeneratedTestCode())
        .build();
  }
}
