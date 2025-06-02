package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.prompts.ImplementUnitTestsPromptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImplementUnitTests {
    private final ImplementUnitTestsPromptGenerator promptGenerator;
    private final B3GPTGateway b3GPTGateway;

    public ImplementUnitTests(ImplementUnitTestsPromptGenerator promptGenerator, B3GPTGateway b3GPTGateway) {
        this.promptGenerator = promptGenerator;
        this.b3GPTGateway = b3GPTGateway;
    }

    public AssistantMessage run(String targetClassName, String targetClassCode, String targetClassPackage, String guidelines, String dependencies, String scenarios) {
        log.info("INIT ImplementUnitTests run. class: {}, package: {}, guidelines: {}, dependencies: {}, scenarios: {}", targetClassName, targetClassPackage, guidelines, dependencies, scenarios);
        Prompt prompt = this.promptGenerator.get(targetClassName, targetClassCode, targetClassPackage, guidelines, dependencies, scenarios);

        log.info("PROCESSING ImplementUnitTests run. prompt: {}", prompt.toString());
        AssistantMessage message = this.b3GPTGateway.callAPI(prompt).getResult().getOutput();

        log.info("END ImplementUnitTests run. message: {}", message.getText());
        return message;
    }

}
