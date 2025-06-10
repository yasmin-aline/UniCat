package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FixUnitTestsPromptGenerator {

  @Value("classpath:/prompts/v3/5-Corrigir-Testes-Unitários-Falhos.st")
  private Resource templateResource;

  public Prompt get(
      final String dependenciesCode,
      final String dependenciesName,
      final String testClassCode,
      final String failingTestsDetails,
      final String coverageDetails,
      final String attemptNumber) {
    final var context = RequestContextHolder.getContext();
    final var targetClassName =
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
            "dependenciesCode", dependenciesCode,
            "testClassCode", testClassCode,
            "failingTestsDetails", failingTestsDetails,
            "coverageDetails", coverageDetails,
            "attemptNumber", attemptNumber);

    final var promptText = promptTemplate.render(vars);
    final var userMessage = new UserMessage(promptText);

    return new Prompt(userMessage);
  }
}
