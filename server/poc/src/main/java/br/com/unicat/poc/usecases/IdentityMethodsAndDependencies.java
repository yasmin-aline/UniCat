package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.prompts.IdentifyDependenciesPromptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IdentityMethodsAndDependencies {
    private final IdentifyDependenciesPromptGenerator promptGenerator;
    private final B3GPTGateway b3gptGateway;

    public IdentityMethodsAndDependencies(IdentifyDependenciesPromptGenerator promptGenerator, B3GPTGateway b3gptGateway) {
        this.promptGenerator = promptGenerator;
        this.b3gptGateway = b3gptGateway;
    }

    /**
     * Extracts scenarios, methods, and dependencies from the previous response,
     * generates a prompt to identify dependencies per scenario, calls the IA, and returns the result.
     *
     * @param mappedScenariosResponse The AssistantMessage containing mapped scenarios, methods, and dependencies.
     * @param targetClassName The name of the class being analyzed.
     * @param targetClassCode The source code of the class being analyzed.
     * @return AssistantMessage containing the identified dependencies per scenario or an error message.
     */
    public AssistantMessage run(final AssistantMessage mappedScenariosResponse, final String targetClassName, final String targetClassCode) {
        log.info("INIT IdentityMethodsAndDependencies run for class: {}", targetClassName);
        final String mappedScenariosResponseText = mappedScenariosResponse.getText();

        final Prompt prompt = this.promptGenerator.get(targetClassName, targetClassCode, mappedScenariosResponseText);
        final ChatResponse response = this.b3gptGateway.callAPI(prompt);

        log.info("END IdentityMethodsAndDependencies run successfully for class: {}", response.getResult());
        return response.getResult().getOutput();
    }
}
