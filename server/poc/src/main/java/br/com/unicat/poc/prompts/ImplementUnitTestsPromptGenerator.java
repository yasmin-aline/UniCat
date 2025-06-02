package br.com.unicat.poc.prompts;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ImplementUnitTestsPromptGenerator {

  public Prompt get(
      final String targetClassName,
      final String targetClassCode,
      final String targetClassPackage,
      final String guidelines,
      final String dependencies,
      final String scenarios) {
    final String prompt =
        String.format(
            """
            Tarefa: Gerar o código Java completo para a classe de teste `%sTest` usando JUnit5 e Mockito, implementando todos os cenários de teste detalhados fornecidos.

            Contexto:
            Classe Original: `%s`
            Pacote: `%s`
            Código Fonte (para referência):
            ```java
            %s
            ```

            Dependências a Mockar:

            %s

            Cenários de Teste Detalhados a Implementar:

            %s

            Instruções:

            %s

            Formato da Saída:\s
            Retorne apenas o código Java completo para a classe de teste %sTest.java. Não inclua nenhuma explicação ou texto antes ou depois do bloco de código Java. O código deve estar pronto para ser compilado e executado.

            --- INÍCIO DO CÓDIGO DA CLASSE DE TESTE ---
            ```java
            // Código completo de %sTest.java aqui...
            ```
            --- FIM DO CÓDIGO DA CLASSE DE TESTE ---
            """,
            targetClassName,
            targetClassName,
            targetClassPackage,
            targetClassCode,
            dependencies,
            scenarios,
            guidelines,
            targetClassName,
            targetClassName);

    return new Prompt(prompt);
  }
}
