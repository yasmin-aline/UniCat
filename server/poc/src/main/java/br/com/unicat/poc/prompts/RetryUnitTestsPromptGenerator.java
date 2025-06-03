package br.com.unicat.poc.prompts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class RetryUnitTestsPromptGenerator {

    public Prompt get(
            String targetClassName,
            String targetClassCode,
            String targetClassPackage,
            String guidelines,
            String dependencies,
            String scenarios,
            List<String> testErrors
    ) {
        StringBuilder errorsSection = new StringBuilder();
        if (testErrors != null && !testErrors.isEmpty()) {
            errorsSection.append("Erros encontrados nos testes:\n");
            for (String error : testErrors) {
                errorsSection.append("- ").append(error).append("\n");
            }
        }
// exemplo d prompt -> adicionar o que vai ser criado aqui
        String prompt = String.format(
                """
                Classe: %s
                Pacote: %s
                Código:
                %s

                Diretrizes: %s
                Dependências: %s
                Cenários: %s

                %s
                Por favor, refatore os testes considerando os erros acima.
                """,
                targetClassName,
                targetClassPackage,
                targetClassCode,
                guidelines,
                dependencies,
                scenarios,
                errorsSection.toString()
        );

        log.info("PROCESSING RetryUnitTestsPromptGenerator. prompt: {}", prompt);
        return new Prompt(prompt);
    }
}