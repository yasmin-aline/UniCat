package br.com.unicat.poc.v1.usecases;

import br.com.unicat.poc.v1.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.v1.adapter.http.dtos.response.RefactoredTestCodeResponseDTO;
import br.com.unicat.poc.v1.adapter.http.dtos.response.RefactoredUnitTestResponseDTO;
import br.com.unicat.poc.v1.entities.RefactoredUnitTests;
import br.com.unicat.poc.v1.entities.TestResults;
import br.com.unicat.poc.v1.prompts.FixUnitTestsPromptGenerator;
import br.com.unicat.poc.v1.usecases.interfaces.RefactorFailingUnitTestsInterface;
import br.com.unicat.poc.v1.usecases.utilities.JsonLlmResponseParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefactorFailingUnitTestsUseCase implements RefactorFailingUnitTestsInterface {
  private final FixUnitTestsPromptGenerator fixUnitTestsPromptGenerator;
  private final B3GPTGateway b3gptGateway;

  @Override
  public RefactoredUnitTestResponseDTO execute(
      final String dependenciesName,
      final String dependencies,
      final String testClassCode,
      final TestResults testResults,
      final String attemptNumber)
      throws Exception {
    log.info(
        "INIT RefactorFailingUnitTestsUseCase execute. stacktraceInterpretedList: {}", testResults);

    ObjectMapper mapper =
        new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    String testResultsJSON = mapper.writeValueAsString(testResults);
    String coverageDetails = mapper.writeValueAsString(testResults.coverageReport());

    final var num = Integer.parseInt(attemptNumber) + 1;
    var str = String.valueOf(num);
    if (num >= 3) {
      str +=
          "\nLimite de attemps atingido! Comente todos os testes que ainda estão falhando com a devida justificativa.";
    }

    final var prompt =
        this.fixUnitTestsPromptGenerator.get(
            dependencies, dependenciesName, testClassCode, testResultsJSON, coverageDetails, str);

    final var chatResponse = this.b3gptGateway.callAPI(prompt);
    final var assistantMessage = chatResponse.getResult().getOutput();

    final var refactoredUnitTests =
        JsonLlmResponseParser.parseLlmResponse(
            assistantMessage, new TypeReference<RefactoredUnitTests>() {});
    assert refactoredUnitTests != null;

    log.info(
        "END RefactorFailingUnitTestsUseCase execute. refactoredUnitTests: {}",
        refactoredUnitTests);
    return new RefactoredUnitTestResponseDTO(
        refactoredUnitTests.modifiedTestMethods().stream()
            .map(test -> new RefactoredTestCodeResponseDTO(test.methodName(), test.modifiedCode()))
            .toList(),
        refactoredUnitTests.requiredNewImports());
  }
}
