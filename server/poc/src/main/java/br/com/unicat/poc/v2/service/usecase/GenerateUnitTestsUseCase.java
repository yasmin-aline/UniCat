package br.com.unicat.poc.v2.service.usecase;

import br.com.unicat.poc.shared.gateway.B3GPTGateway;
import br.com.unicat.poc.v2.service.prompt.DesignScenariosPrompt;
import br.com.unicat.poc.v2.service.prompt.GenerateUnitTestsPrompt;
import br.com.unicat.poc.v2.service.prompt.InitialAnalysesPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.Charset;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateUnitTestsUseCase {
  private final InitialAnalysesPrompt initialAnalysesPrompt;
  private final DesignScenariosPrompt designScenariosPrompt;
  private final GenerateUnitTestsPrompt generateUnitTestsPrompt;

  private final ObjectMapper objectMapper;
  private final B3GPTGateway gateway;

  @Value("classpath:/mocks/v5/prompt1-answer.st")
  private Resource prompt1Answer;

  @Value("classpath:/mocks/v5/prompt2-answer.st")
  private Resource prompt2Answer;

  @Value("classpath:/mocks/v5/prompt3-answer.st")
  private Resource prompt3Answer;

  public String execute() throws Exception {
    log.info("INIT GenerateUnitTests execute.\n\n");
    final var initialAnalysesRawText = requestLLMWith(initialAnalysesPrompt.get());
    log.info(initialAnalysesRawText);
//    		final var mockAns1 = prompt1Answer.getContentAsString(Charset.defaultCharset());
//    		final var scenarios = requestLLMWith(designScenariosPrompt.get(mockAns1));
    final var scenarios = requestLLMWith(designScenariosPrompt.get(initialAnalysesRawText));
    log.info(scenarios);

//    		final var mockAns2 = prompt2Answer.getContentAsString(Charset.defaultCharset());
//    		final var generatedTests = requestLLMWith(generateUnitTestsPrompt.get(mockAns2));
    final var generatedTests = requestLLMWith(generateUnitTestsPrompt.get(scenarios));
    log.info("Generated tests: {}", generatedTests);
    return generatedTests;
//    		return prompt3Answer.getContentAsString(Charset.defaultCharset());
  }

  private String requestLLMWith(final Prompt prompt) {
    return callLlmAPI(prompt).getText();
  }

  private <T> T requestLLMWith(final Prompt prompt, final Class<T> clazz) throws Exception {
    final var assistantMessage = callLlmAPI(prompt);
    final var rawText = stripJsonFences(assistantMessage.getText());
    return objectMapper.readValue(rawText, clazz);
  }

  private AssistantMessage callLlmAPI(Prompt prompt) {
    final var chatResponse = this.gateway.callAPI(prompt);
    return chatResponse.getResult().getOutput();
  }

  private static String stripJsonFences(String text) {
    var rawText = Objects.requireNonNull(text).trim();
    if (rawText.startsWith("```json")) rawText = rawText.substring("```json".length()).trim();
    if (rawText.endsWith("```")) rawText = rawText.substring(0, rawText.length() - 3).trim();
    return rawText;
  }
}
