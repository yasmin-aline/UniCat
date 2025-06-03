package br.com.unicat.poc.prompts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RetryUnitTestsPromptGenerator {

    public Prompt get(
            String targetClassName,         // Nome da classe original (ex: "UserService")
            String targetClassPackage,      // Pacote da classe original
            String targetClassCode,       // Código da classe original (para contexto)
            String testClassName,         // Nome da classe de teste (ex: "UserServiceTest")
            String testClassCode,         // Código ATUAL da classe de teste a ser corrigida
            String guidelines,            // Diretrizes de codificação originais
            String dependencies,          // Lista de dependências a mockar original
            String scenarios,             // Descrição dos cenários de teste originais
            List<Map<String, String>> failedTestsAndErrors, // Lista de mapas, cada mapa com "methodName" e "errorMessage"
            String assertionLibrary       // Ex: "AssertJ", "JUnit 5 Assertions"
    ) {
        // Formata a seção de erros para o prompt
        String errorsSection = failedTestsAndErrors.stream()
                .map(errorInfo -> String.format(
                        "- Método de Teste Falho: `%s`\n  Erro Reportado:\n  ```\n%s\n  ```",
                        errorInfo.getOrDefault("methodName", "N/A"),
                        errorInfo.getOrDefault("errorMessage", "N/A")
                ))
                .collect(Collectors.joining("\n\n"));

        String prompt = String.format(
                """
                **Tarefa Principal:** Refatorar a classe de teste Java `%s` para corrigir **APENAS** os métodos de teste `@Test` listados abaixo que falharam durante a execução anterior. Utilize JUnit 5, Mockito e a biblioteca de asserções `%s`. O objetivo é que todos os testes, incluindo os corrigidos, passem na próxima execução, mantendo a conformidade com as diretrizes e cenários originais.

                **Contexto:**
                - Classe Original (para referência): `%s`
                - Pacote da Classe Original: `%s`
                - Código da Classe Original (para referência):
                ```java
                %s
                ```
                - Classe de Teste a ser Corrigida: `%s`
                - Código ATUAL da Classe de Teste (contém os testes falhos):
                ```java
                %s
                ```
                - Biblioteca de Asserções Preferida: `%s`
                
                - Diretrizes de Codificação Originais (para referência):
                %s
                
                - Dependências a Mockar Originais (para referência):
                %s
                
                - Cenários de Teste Originais (para referência):
                %s
                
                - Testes Falhos e Seus Erros Reportados:
                %s

                **Instruções Detalhadas para Correção:**

                1.  **Análise Focada:** Examine o `Código ATUAL da Classe de Teste` (`%s`) fornecido acima.

                2.  **Diagnóstico Preciso por Teste Falho:** Para CADA método listado em `Testes Falhos e Seus Erros Reportados`:
                    *   Analise cuidadosamente a `Erro Reportado` (mensagem de erro/stack trace) associada.
                    *   Localize o código do método `@Test` correspondente dentro do `Código ATUAL da Classe de Teste`.
                    *   Considere as `Diretrizes de Codificação Originais`, `Dependências a Mockar Originais` e `Cenários de Teste Originais` para entender a intenção do teste.
                    *   Determine a causa raiz **específica** da falha. Exemplos comuns: Configuração incorreta de mock (`Mockito.when`), verificação de mock falha (`Mockito.verify`), asserção incorreta (usando `%s`), dados de entrada inadequados para o cenário, ou lógica interna do próprio teste que não reflete o cenário original ou as diretrizes.

                3.  **Correção Cirúrgica e Eficiente:**
                    *   Modifique **ESTRITAMENTE O NECESSÁRIO** dentro do corpo dos métodos `@Test` que falharam para corrigir a causa raiz identificada, mantendo a conformidade com as `Diretrizes de Codificação Originais`.
                    *   **NÃO ALTERE** métodos `@Test` que **NÃO** estão listados na seção de falhas.
                    *   **NÃO ALTERE** o código da classe original (`%s`). A correção é **EXCLUSIVAMENTE** na classe de teste (`%s`).
                    *   **PRIORIZE** a correção mais simples e direta que resolva o erro específico. Evite refatorações complexas ou alterações em outras partes da classe de teste, a menos que seja absolutamente indispensável para a correção da falha.
                    *   **Exemplos de Correções Comuns:** Ajustar `Mockito.when(...).thenReturn(...)` ou `Mockito.when(...).thenThrow(...)` para alinhar com o cenário, corrigir parâmetros ou número de invocações em `Mockito.verify(...)`, ajustar valores esperados/atuais nas asserções `%s`, modificar os dados de entrada passados ao método sob teste dentro do método `@Test`.

                4.  **Manutenção da Estrutura:** Preserve a estrutura geral da classe de teste, incluindo imports, anotações de classe (`@ExtendWith`), campos (`@Mock`, `@InjectMocks`), métodos de setup (`@BeforeEach`) e métodos auxiliares. Só altere essas partes se for **essencial** para a correção de um teste falho listado.

                5.  **Garantia de Qualidade:** O código da classe de teste resultante DEVE compilar sem erros. O objetivo principal é que, após suas correções, **TODOS** os testes na classe (incluindo os que já passavam e os que foram corrigidos) executem com sucesso.

                **Formato da Saída:**
                Retorne **APENAS** o código Java completo e atualizado para a classe de teste `%s`. Não inclua NENHUM texto explicativo, introdução, comentários de bloco desnecessários, marcadores (`--- INÍCIO ---`, `--- FIM ---`) ou qualquer texto antes ou depois do bloco de código Java. A saída deve ser diretamente compilável.

                --- INÍCIO DO CÓDIGO DA CLASSE DE TESTE REFATORADA ---
                ```java
                // Pacote, imports e código completo e corrigido de %s aqui...
                ```
                --- FIM DO CÓDIGO DA CLASSE DE TESTE REFATORADA ---
                """,
                // Tarefa Principal
                testClassName, assertionLibrary,
                // Contexto
                targetClassName, targetClassPackage, targetClassCode, testClassName, testClassCode, assertionLibrary, guidelines, dependencies, scenarios, errorsSection,
                // Instruções
                testClassName, // 1. Análise Focada
                assertionLibrary, // 2. Diagnóstico (asserções)
                targetClassName, testClassName, // 3. Correção (não alterar original, focar no teste)
                assertionLibrary, // 3. Correção (exemplos)
                // Formato Saída
                testClassName, testClassName
        );

        log.info("PROCESSING OptimizedRetryUnitTestsPromptGenerator. prompt: {}", prompt);
        return new Prompt(prompt);
    }
}