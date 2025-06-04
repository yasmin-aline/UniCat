package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.prompts.RetryUnitTestsPromptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RetryUnitTestsUseCase {
    private final RetryUnitTestsPromptGenerator promptGenerator;
    private final B3GPTGateway b3GPTGateway;

    public RetryUnitTestsUseCase(
            RetryUnitTestsPromptGenerator promptGenerator,
            B3GPTGateway b3GPTGateway
    ) {
        this.promptGenerator = promptGenerator;
        this.b3GPTGateway = b3GPTGateway;
    }

    public AssistantMessage run(
            String targetClassName,
            String targetClassPackage,
            String targetClassCode,
            String testClassName,
            String testClassCode,
            String guidelines,
            String dependencies,
            String scenarios,
            String failedTestsAndErrors,
            String assertionLibrary
    ) {
        log.info("INIT RetryUnitTestsUseCase run for class: {}", targetClassName);

        Prompt prompt = this.promptGenerator.get(
                targetClassName,
                targetClassPackage,
                targetClassCode,
                testClassName,
                testClassCode,
                guidelines,
                dependencies,
                scenarios,
                failedTestsAndErrors,
                assertionLibrary
        );

        log.info("PROCESSING RetryUnitTestsUseCase run. prompt: {}", prompt.toString());
        ChatResponse response = this.b3GPTGateway.callAPI(prompt);
        AssistantMessage output = response.getResult().getOutput();

        log.info("END RetryUnitTestsUseCase run. message: {}", output.getText());
        return output;
    }
}