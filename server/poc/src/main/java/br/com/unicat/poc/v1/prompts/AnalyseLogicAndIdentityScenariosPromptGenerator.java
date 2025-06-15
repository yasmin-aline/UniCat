package br.com.unicat.poc.v1.prompts;

import br.com.unicat.poc.v1.adapter.http.context.RequestContext;
import br.com.unicat.poc.v1.adapter.http.context.RequestContextHolder;
import java.util.Map;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class AnalyseLogicAndIdentityScenariosPromptGenerator {

  @Value("classpath:/prompts/v4/2-Analisar-Lógica-e-Identificar-Cenários.st")
  private Resource templateResource;

  public Prompt get(final String dependenciesCode, final String dependenciesName) {
    RequestContext context = RequestContextHolder.getContext();
    final String targetClassName =
        context.getTargetClassPackage() + "." + context.getTargetClassName();

    PromptTemplate promptTemplate =
        PromptTemplate.builder()
            .renderer(
                StTemplateRenderer.builder()
                    .startDelimiterToken('$')
                    .endDelimiterToken('$')
                    .build())
            .resource(this.templateResource)
            .build();

    final Map<String, Object> vars =
        Map.of(
            "targetClassName", targetClassName,
            "targetClassCode", context.getTargetClassCode(),
            "dependenciesName", dependenciesName,
            "dependenciesCode", dependenciesCode);

    final var promptText = promptTemplate.render(vars);
    final var userMessage = new UserMessage(promptText);

    return new Prompt(userMessage);
  }
}
