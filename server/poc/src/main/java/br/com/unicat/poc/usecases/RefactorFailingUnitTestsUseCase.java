package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.request.FailingTestDetailsRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.RefactoredTestCodeResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.RefactoredUnitTestResponseDTO;
import br.com.unicat.poc.entities.RefactoredUnitTests;
import br.com.unicat.poc.prompts.FixUnitTestsPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.RefactorFailingUnitTestsInterface;
import br.com.unicat.poc.usecases.utilities.JsonLlmResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefactorFailingUnitTestsUseCase implements RefactorFailingUnitTestsInterface {
  private final FixUnitTestsPromptGenerator fixUnitTestsPromptGenerator;
  private final B3GPTGateway b3gptGateway;

  @Override
  public RefactoredUnitTestResponseDTO execute(final String dependenciesName, final String dependencies, final String testClassCode, final List<FailingTestDetailsRequestDTO> failingTestDetailsRequestDTOS) throws Exception {
    log.info("INIT RefactorFailingUnitTestsUseCase execute. failingTestDetailsRequestDTOS: {}", failingTestDetailsRequestDTOS);

    ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    String failingTestsDetails = mapper.writeValueAsString(failingTestDetailsRequestDTOS);

    final var prompt = this.fixUnitTestsPromptGenerator.get(dependencies, dependenciesName, testClassCode, failingTestsDetails);

    final var chatResponse = this.b3gptGateway.callAPI(prompt);
    final var assistantMessage = chatResponse.getResult().getOutput();

    final var refactoredUnitTests = JsonLlmResponseParser.parseLlmResponse(assistantMessage, RefactoredUnitTests.class);
    assert refactoredUnitTests != null;

    log.info("END RefactorFailingUnitTestsUseCase execute. refactoredUnitTests: {}", refactoredUnitTests);
    return new RefactoredUnitTestResponseDTO(
            refactoredUnitTests.modifiedTestMethods().stream()
                    .map(test -> new RefactoredTestCodeResponseDTO(test.methodName(), test.modifiedCode()))
                    .toList(),
            refactoredUnitTests.requiredNewImports()
    );
  }
}