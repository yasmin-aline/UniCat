package br.com.unicat.poc.usecases;

import br.com.unicat.poc.prompts.AnalyzeAndMapScenariosPrompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalyseAndMapClassUnitTestScenarios {
    private final AnalyzeAndMapScenariosPrompt promptService;
    private final AzureOpenAiChatModel baseChatModel;


    public AnalyseAndMapClassUnitTestScenarios(AnalyzeAndMapScenariosPrompt prompt, AzureOpenAiChatModel baseChatModel) {
        this.promptService = prompt;
        this.baseChatModel = baseChatModel;
    }


    public void run(final String targetClassToTest, final String targetClassPackage) {
          log.info("INIT run. target class: {}, target package: {}", targetClassToTest, targetClassToTest);
//        try {
//            ChatResponse response = baseChatModel.call(
//                    new Prompt("Generate the names of 5 famous pirates.")
//            );
//
//            log.info("RESPONSE: {}", response);
//            log.info("END generation.");
//
//            return response.toString();
        // 1. [Prompt] Analisar profundamente a classe a ser testada && Mapear todos Cenários de Teste
        final String prompt = this.promptService.getPrompt(targetClassToTest, targetClassPackage);
        //log.info("PROCESSING pr");
    }
}
