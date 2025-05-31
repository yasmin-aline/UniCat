package br.com.unicat.poc.adapter.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UniCatController {

    private final AzureOpenAiChatModel baseChatModel;

    // O Spring irá injetar o AzureOpenAiChatModel que ele configurou
    // com base nas propriedades do application.properties/yml
    public UniCatController(AzureOpenAiChatModel baseChatModel) {
        this.baseChatModel = baseChatModel;
    }

    @GetMapping(path = "/unicat")
    String generation() {
        log.info("INIT generation.");
        try {
            ChatResponse response = baseChatModel.call(
                    new Prompt("Generate the names of 5 famous pirates.")
            );

            log.info("RESPONSE: {}", response);
            log.info("END generation.");

            return response.toString();

        } catch (Exception ex) {
            log.error("ERROR generation. exception: {}", ex.getMessage());
            return "Error occurred: " + ex.getMessage();
        }
    }
}