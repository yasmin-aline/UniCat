package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.prompts.AnalyzeClassToTestPromptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AnalyseClassToTest {
    private final AnalyzeClassToTestPromptGenerator promptGenerator;
    private final B3GPTGateway b3gptGateway;

    public AnalyseClassToTest(AnalyzeClassToTestPromptGenerator prompt, B3GPTGateway b3gptGateway) {
        this.promptGenerator = prompt;
        this.b3gptGateway = b3gptGateway;
    }


    public AssistantMessage run(final String targetClassName, final String targetClassToTest, final String targetClassPackage) {
        // 1. [Prompt] Analisar profundamente a classe a ser testada && Mapear todos Cenários de Teste
        log.info("INIT run AnalyseAndMapClassUnitTestScenarios. target class: {}, target package: {}", targetClassName, targetClassPackage);
        final Prompt prompt = this.promptGenerator.get(targetClassName, targetClassPackage, targetClassToTest);
        ChatResponse response = this.b3gptGateway.callAPI(prompt);

        log.info("END AnalyseAndMapClassUnitTestScenarios run. response: \n{}", response.getResult().getOutput().getText());
        return response.getResult().getOutput();
    }
}
