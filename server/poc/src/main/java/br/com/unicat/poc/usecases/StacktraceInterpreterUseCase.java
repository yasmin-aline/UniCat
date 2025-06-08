package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.entities.StacktraceInterpreted;
import br.com.unicat.poc.prompts.StacktraceInterpreterPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.StacktraceInterpreterInterface;
import br.com.unicat.poc.usecases.utilities.JsonLlmResponseParser;
import com.fasterxml.jackson.core.type.TypeReference;
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
  public List<StacktraceInterpreted> execute(
      String dependenciesName, String dependenciesCode, String failedTestsDetails)
      throws Exception {
    log.info(
        "INIT StacktraceInterpreterUseCase execute. dependenciesName: {}, dep. code is null: {}, failedTestsDetails is null: {}",
        dependenciesName,
        Objects.isNull(dependenciesCode),
        Objects.isNull(failedTestsDetails));

    final var prompt =
        this.stacktraceInterpreterPromptGenerator.get(
            dependenciesName, dependenciesCode, failedTestsDetails);

    final var chatResponse = this.b3gptGateway.callAPI(prompt);
    final var assistantMessage = chatResponse.getResult().getOutput();

    final var stackTraceInterpreted =
        JsonLlmResponseParser.parseLlmResponse(
            assistantMessage, new TypeReference<List<StacktraceInterpreted>>() {});
    assert stackTraceInterpreted != null;

    log.info(
        "END StacktraceInterpreterUseCase execute. stackTraceInterpreted: {}",
        stackTraceInterpreted);
    return stackTraceInterpreted;
  }
}
