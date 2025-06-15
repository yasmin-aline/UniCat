package br.com.unicat.poc.v2.service.prompt;

import br.com.unicat.poc.v2.controller.context.RequestContextHolderV2;
import br.com.unicat.poc.v2.controller.context.RequestContextV2;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.isNull;

@Component
public class GenerateUnitTestsPrompt {

	@Value("classpath:/prompts/v5/prompt3-gerar-testes.st")
	private Resource templateResource;

	public Prompt get(final String prompt2Answer) {
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

		final var guidelines = isNull(contextV2.guidelines()) || contextV2.guidelines().isEmpty() ? "null" : contextV2.guidelines();

		final Map<String, Object> vars =
				Map.of(
						"targetClassCode", contextV2.targetClassCode(),
						"dependenciesCode", contextV2.dependenciesCode(),
						"guidelines", guidelines,
						"prompt2Answer", prompt2Answer
				);

		final var promptText = promptTemplate.render(vars);
		final var userMessage = new UserMessage(promptText);

		return new Prompt(userMessage);
	}

}
