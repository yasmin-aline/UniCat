package br.com.unicat.poc.adapter.http;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UniCatController {

    private final AzureOpenAiChatModel baseChatModel;

    private final String API_KEY = "7562f54094dd2f506a90064b609f2cdd33b9983df53d60a5dfb1a810765c0d8";
    private final String B3GPT_API = "https://api-b3gpt.b3.com.br/internal-api/b3gpt-llms/v1";

    public UniCatController(AzureOpenAiChatModel baseChatModel) {
        this.baseChatModel = baseChatModel;
    }

    @GetMapping(path = "/unicat")
    String generation() {
        log.info("INIT generation.");
        try {
            var openAIClientBuilder = new OpenAIClientBuilder()
                    .credential(new AzureKeyCredential(this.API_KEY))
                    .endpoint(this.B3GPT_API);

            var openAIChatOption = AzureOpenAiChatOptions.builder()
                    .deploymentName("gpt-4o")
                    .temperature(0.3)
                    .maxTokens(100)
                    .build();

            var chatModel = AzureOpenAiChatModel.builder()
                    .openAIClientBuilder(openAIClientBuilder)
                    .defaultOptions(openAIChatOption)
                    .build();

            ChatResponse response = chatModel.call(
                    new Prompt("Generate the names of 5 famous pirates.")
            );

            log.info("RESPONSE: {}", response);
            log.info("END generation.");

            return response.toString();
         } catch (Exception ex) {
            log.error("ERROR generation. exception: {}", ex.getMessage());
        }

        return null;
    }

}
