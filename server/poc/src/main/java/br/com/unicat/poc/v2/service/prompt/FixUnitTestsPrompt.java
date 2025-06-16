package br.com.unicat.poc.v2.service.prompt;

import static java.util.Objects.isNull;

import br.com.unicat.poc.v2.controller.context.RequestContextHolderV2;
import br.com.unicat.poc.v2.controller.context.RequestContextV2;
import java.util.Map;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FixUnitTestsPrompt {

  @Value("classpath:/prompts/v5/prompt4B-correcao-testes.st")
  private Resource templateResource;

  public Prompt get(final String diagnosticJson) {
    RequestContextV2 contextV2 = RequestContextHolderV2.getContext();

    PromptTemplate promptTemplate =
        PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('$')
                    .endDelimiterToken('$')
                    .build())
            .resource(this.templateResource)
            .build();

    final var guidelines =
        isNull(contextV2.guidelines()) || contextV2.guidelines().isEmpty()
            ? "null"
            : contextV2.guidelines();

    final Map<String, Object> vars =
        Map.of(
            "targetClassCode", contextV2.targetClassCode(),
            "dependenciesCode", contextV2.dependenciesCode(),
            "testClassCode", contextV2.testClassCode(),
            "diagnosticJson", diagnosticJson);

    final var promptText = promptTemplate.render(vars);
    final var userMessage = new UserMessage(promptText);

    return new Prompt(userMessage);
  }
}
