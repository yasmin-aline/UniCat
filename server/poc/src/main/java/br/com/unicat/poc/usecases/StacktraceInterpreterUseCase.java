package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.request.RetryRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.request.TestResultsRequestDTO;
import br.com.unicat.poc.entities.CoverageReport;
import br.com.unicat.poc.entities.FailedDetails;
import br.com.unicat.poc.entities.TestResults;
import br.com.unicat.poc.prompts.StacktraceInterpreterPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.StacktraceInterpreterInterface;
import br.com.unicat.poc.usecases.utilities.JsonLlmResponseParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StacktraceInterpreterUseCase implements StacktraceInterpreterInterface {
  private final StacktraceInterpreterPromptGenerator stacktraceInterpreterPromptGenerator;
  private final B3GPTGateway b3gptGateway;

  @Override
  public TestResults execute(final RetryRequestDTO requestDTO) throws Exception {
    log.info(
        "INIT StacktraceInterpreterUseCase execute. dependenciesName: {}, dep. code is null: {}, failedTestsDetails is null: {}",
        requestDTO.dependenciesName(),
        Objects.isNull(requestDTO.dependencies()),
        Objects.isNull(requestDTO.failingTestDetailsRequestDTOS()));

    final var prompt =
        this.stacktraceInterpreterPromptGenerator.get(
            requestDTO.dependenciesName(),
            requestDTO.dependencies(),
            requestDTO.failingTestDetailsRequestDTOS());

    final var chatResponse = this.b3gptGateway.callAPI(prompt);
    final var assistantMessage = chatResponse.getResult().getOutput();

    final var failedDetails =
        JsonLlmResponseParser.parseLlmResponse(
            assistantMessage, new TypeReference<List<FailedDetails>>() {});
    assert failedDetails != null;

    String rawText = Objects.requireNonNull(requestDTO.testResults()).trim();
    final var testResultsDTO =
        rawText.isEmpty()
            ? null
            : new ObjectMapper().readValue(rawText, TestResultsRequestDTO.class);

    String rawText2 = Objects.requireNonNull(requestDTO.coverageReport()).trim();
    final var coverageReport =
        rawText2.isEmpty() ? null : new ObjectMapper().readValue(rawText2, CoverageReport.class);

    log.info("END StacktraceInterpreterUseCase execute. stackTraceInterpreted: {}", failedDetails);

    if (testResultsDTO == null) {
      return new TestResults("", "", "", "", failedDetails, coverageReport);
    }

    return new TestResults(
        testResultsDTO.totalTests(),
        testResultsDTO.passedTests(),
        testResultsDTO.failedTests(),
        testResultsDTO.errorTests(),
        failedDetails,
        coverageReport);
  }
}
