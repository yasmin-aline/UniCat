package br.com.unicat.poc.v2.service.usecase;

import br.com.unicat.poc.shared.gateway.B3GPTGateway;
import br.com.unicat.poc.v2.model.prompt4.FixedUnitTests;
import br.com.unicat.poc.v2.service.prompt.FixUnitTestsPrompt;
import br.com.unicat.poc.v2.service.prompt.StackTraceInterpreterPrompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixUnitTestsUseCase {
    private final StackTraceInterpreterPrompt stackTraceInterpreterPrompt;
    private final FixUnitTestsPrompt fixUnitTestsPrompt;


    private final ObjectMapper objectMapper;
    private final B3GPTGateway gateway;

    public FixedUnitTests execute() throws Exception {
        log.info("INIT FixUnitTests execute.\n\n");
        final var errorsDetailed = requestLLMWith(stackTraceInterpreterPrompt.get());
        final var fixedUnitTests = requestLLMWith(fixUnitTestsPrompt.get(errorsDetailed), FixedUnitTests.class);

        log.info("END FixUnitTests execute. fixed unit tests: {}", fixedUnitTests);
        return fixedUnitTests;
    }

    private String requestLLMWith(final Prompt prompt) {
        return callLlmAPI(prompt).getText();
    }

    private <T> T requestLLMWith(final Prompt prompt, final Class<T> clazz) throws Exception {
        final var assistantMessage = callLlmAPI(prompt);
        final var rawText = stripJsonFences(assistantMessage);
        objectMapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
        return objectMapper.readValue(rawText, clazz);
    }

    private AssistantMessage callLlmAPI(Prompt prompt) {
        final var chatResponse = this.gateway.callAPI(prompt);
        return chatResponse.getResult().getOutput();
    }

    private static String stripJsonFences(AssistantMessage assistantMessage) {
        var rawText = Objects.requireNonNull(assistantMessage.getText()).trim();
        if (rawText.startsWith("```json")) rawText = rawText.substring("```json".length()).trim();
        if (rawText.endsWith("```")) rawText = rawText.substring(0, rawText.length() - 3).trim();
        return rawText;
    }
}
