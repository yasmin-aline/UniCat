package br.com.unicat.poc.shared.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.Objects;
import org.springframework.ai.chat.messages.AssistantMessage;

public class JsonLlmResponseParser {

  private static final ObjectMapper objectMapper =
      new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

  public static <T> T parseLlmResponse(
      AssistantMessage assistantMessage, TypeReference<T> typeReference) throws Exception {
    String rawText = Objects.requireNonNull(assistantMessage.getText()).trim();

    if (rawText.startsWith("```json")) {
      rawText = rawText.substring("```json".length()).trim();
    }

    if (rawText.endsWith("```")) {
      rawText = rawText.substring(0, rawText.length() - 3).trim();
    }

    return objectMapper.readValue(rawText, typeReference);
  }
}
