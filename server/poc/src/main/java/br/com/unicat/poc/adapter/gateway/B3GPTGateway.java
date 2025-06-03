package br.com.unicat.poc.adapter.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class B3GPTGateway {
  private final AzureOpenAiChatModel baseChatModel;

  public B3GPTGateway(AzureOpenAiChatModel baseChatModel) {
    this.baseChatModel = baseChatModel;
  }

  public ChatResponse callAPI(final Prompt prompt) {
    try {
      log.info("INIT callAPI. prompt: {}", prompt.toString());
      ChatResponse response = baseChatModel.call(prompt);

      log.info("END callAPI. prompt: {}", prompt.toString());
      return response;
    } catch (Exception e) {
      log.error("ERROR callAPI. exception: {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
