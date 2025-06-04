package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class AnalyseLogicAndIdentityScenariosPromptGenerator {

	public Prompt get() {
		RequestContext context = RequestContextHolder.getContext();
		final String targetClassName = context.getTargetClassPackage() + "." + context.getTargetClassName();
		final String dependencies = "";
		final String dependenciesName = "";

		String prompt = String.format("""
				
				""");

		return new Prompt(prompt);
	}

}
