package br.com.unicat.poc.v1.prompts;

import br.com.unicat.poc.v1.adapter.http.context.RequestContext;
import br.com.unicat.poc.v1.adapter.http.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class StacktraceInterpreterPromptGenerator {

  public Prompt get(
      final String dependenciesName,
      final String dependenciesCode,
      final String failedTestsDetails) {
    RequestContext context = RequestContextHolder.getContext();
    final var targetClassName =
        context.getTargetClassPackage() + "." + context.getTargetClassName();

    final var prompt =
        String.format(
            """
                # Prompt para Use Case: StackTrace Interpreter (Versão 2 - Saída JSON, CoT & Few-Shot Complexo)

                **Objetivo:** Analisar detalhes de testes falhos (nome do método e stack trace), realizar uma análise profunda da causa raiz do erro, reanalisar a lógica da classe alvo em relação ao cenário falho, explicar detalhadamente o stack trace e fornecer um passo a passo para a correção do problema. A saída deve ser um JSON robusto com informações detalhadas para cada método falho, utilizando técnicas avançadas de Chain-of-Thought (CoT) e Few-Shot para garantir precisão e abrangência.

                **Instruções:**

                1.  **Entendimento Completo:** Receba o código da classe alvo (`%s`), o código de suas dependências (`%s`), e uma lista de detalhes de testes falhos (`{{ DETALHES_TESTES_FALHOS_JSON }}`, localizado na seção: **Detalhes dos Testes Falhos (JSON):**). Você possui todas as informações necessárias para diagnosticar e propor soluções de forma autônoma e completa.

                2.  **Análise Chain-of-Thought (CoT) Detalhada por Teste Falho:** Para CADA teste listado em `{{ DETALHES_TESTES_FALHOS_JSON }}` (localizado na seção: **Detalhes dos Testes Falhos (JSON):**), realize um raciocínio passo a passo INTERNO e EXAUSTIVO para diagnosticar a falha. Seu CoT deve cobrir:
                    *   **1. Localização e Contexto da Falha:**
                        *   Identifique o `method_name` do teste falho.
                        *   Analise o `stack_trace` para pinpointar a linha exata e o método onde a exceção foi lançada ou a asserção falhou. Determine se a falha ocorreu no código de teste (Arrange, Act, Assert), no código da classe alvo, ou em uma dependência mockada.
                        *   Contextualize a falha: qual cenário de teste estava sendo executado? Quais inputs foram usados?
                    *   **2. Reanálise da Lógica da Classe Alvo vs. Teste:**
                        *   Revise a lógica do método da classe alvo que está sendo testado no cenário falho. Entenda seu comportamento esperado para os inputs fornecidos.
                        *   Compare o comportamento *esperado* da classe alvo com o comportamento *observado* que levou à falha. Há uma discrepância entre a lógica do teste e a lógica da classe alvo? O teste está verificando o comportamento correto?
                        *   Se houver mocks, verifique se o comportamento mockado (`when().thenReturn()`, `doThrow()`, etc.) está alinhado com o que a classe alvo espera e com o cenário de teste. Mocks mal configurados são uma causa comum de falhas.
                    *   **3. Interpretação Profunda do StackTrace:**
                        *   Percorra o `stack_trace` de cima para baixo (do ponto da falha até a origem da chamada). Para cada frame relevante, explique o que a chamada de método representa e como ela contribui para o fluxo que levou ao erro.
                        *   Traduza as mensagens de erro e tipos de exceção (e.g., `NullPointerException`, `IllegalArgumentException`, `AssertionFailedError`) em termos claros, relacionando-os diretamente com o comportamento do código da classe alvo ou do teste.
                        *   Identifique padrões comuns de erro (e.g., `NullPointerException` geralmente indica um objeto não inicializado, `IllegalArgumentException` um input inválido, `AssertionFailedError` uma discrepância entre esperado e real).
                    *   **4. Identificação da Causa Raiz:**
                        *   Sintetize as informações dos passos anteriores para determinar a causa raiz precisa da falha. É um erro no setup do teste (Arrange), na execução (Act), na verificação (Assert), na configuração dos mocks, ou um bug real na lógica da classe alvo?
                        *   Se for um bug na classe alvo, descreva-o concisamente.
                    *   **5. Formulação do Plano de Solução:**
                        *   Com base na causa raiz, forneça um plano de solução detalhado, passo a passo, para corrigir o problema. Este plano deve ser acionável e focado em como o código de teste (Arrange, Act, Assert, mocks) deve ser alterado para que o teste passe.
                        *   Se a causa raiz for um bug na classe alvo, o plano deve indicar que a classe alvo precisa ser corrigida, mas o foco principal da `solution_steps` deve ser como o teste *deveria* ser escrito para validar o comportamento *correto* (assumindo que a classe alvo será corrigida).

                3.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo uma lista de objetos, onde cada objeto representa um teste falho analisado, com as informações detalhadas da análise e do plano de solução.
                    *   **Nunca acrescente qualquer outra informação no corpo de resposta além da estrutura JSON de saída esperada informada abaixo! O sistema que recebe a sua resposta espera somente um objeto JSON e se você adicionar qualquer texto antes ou depois, irá quebrar a aplicação e gerar um bug. Não faça isso. Retorne exatamente o que lhe foi pedido.**

                **Estrutura JSON de Saída Esperada:**

                ```json
                [
                  {
                    "method_name": "<Nome do método de teste falho>",
                    "error_summary": "<Resumo conciso do erro e sua causa raiz>",
                    "detailed_analysis": "<Análise detalhada da lógica da classe alvo em relação ao cenário falho, explicando a ponte entre o algoritmo original e o comportamento testado>",
                    "stack_trace_explanation": "<Explicação detalhada dos erros do StackTrace, relacionando com o comportamento original da classe alvo>",
                    "solution_steps": [
                      "<Passo 1 para solucionar o problema>",
                      "<Passo 2 para solucionar o problema>"
                      // ... mais passos
                    ]
                  }
                  // ... mais objetos para outros testes falhos
                ]
                ```

                **Exemplo Few-Shot Complexo 1 (Falha de Asserção - BigDecimal Scale):**

                *   **Input (Parâmetros Injetados):**
                    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `SimpleDiscountCalculator` que aplica cap de 50%% em percentual e não negativa preço em fixo)
                    *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Códigos de `ProductDTO` e `DiscountType`)
                    *   `{{ DETALHES_TESTES_FALHOS_JSON }}`:
                        ```json
                        [
                          {
                            "method_name": "testBigDecimalComparisonError",
                            "error_message": "org.opentest4j.AssertionFailedError: expected: java.math.BigDecimal<90.0> but was: java.math.BigDecimal<90.00>",
                            "stack_trace": "...at SimpleDiscountCalculatorTest.testBigDecimalComparisonError(SimpleDiscountCalculatorTest.java:XX)\\n\\tat org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:YYY)\\n..."
                          }
                        ]
                        ```

                *   **Output JSON Esperado:**
                    ```json
                    [
                      {
                        "method_name": "testBigDecimalComparisonError",
                        "error_summary": "A asserção de BigDecimal falhou devido à diferença de escala, não de valor numérico. O teste esperava um BigDecimal com uma escala específica (90.0), mas o método retornou um com outra escala (90.00), fazendo com que assertEquals falhasse.",
                        "detailed_analysis": "O método `calculateDiscountedPrice` na `SimpleDiscountCalculator` retorna um `BigDecimal` que, dependendo das operações internas, pode ter uma escala diferente daquela esperada no teste. O teste `testBigDecimalComparisonError` usa `assertEquals` para comparar dois `BigDecimal`s. No entanto, `BigDecimal.equals()` compara não apenas o valor numérico, mas também a escala. Assim, 90.0 (escala 1) é diferente de 90.00 (escala 2) para `equals()`, mesmo que numericamente sejam o mesmo valor. O comportamento original da classe é retornar um `BigDecimal` com a escala resultante das operações, enquanto o teste espera uma escala específica que não é garantida pela implementação.",
                        "stack_trace_explanation": "O `org.opentest4j.AssertionFailedError` indica que uma asserção falhou. A mensagem `expected: <90.0> but was: <90.00>` confirma que os valores numéricos são os mesmos, mas a representação (escala) é diferente. A linha `SimpleDiscountCalculatorTest.testBigDecimalComparisonError(SimpleDiscountCalculatorTest.java:XX)` aponta para a linha da asserção no método de teste, confirmando que o problema está na forma como a comparação é feita, e não necessariamente na lógica de cálculo da classe alvo. O frame `org.junit.jupiter.api.Assertions.assertEquals` mostra que a falha ocorreu na chamada da asserção.",
                        "solution_steps": [
                          "Altere a asserção de `assertEquals(expected, actual)` para `assertEquals(0, expected.compareTo(actual))` para comparar apenas o valor numérico dos BigDecimals, ignorando a escala.",
                          "Garanta que todos os imports necessários para `compareTo` e `BigDecimal` estejam presentes no arquivo de teste."
                        ]
                      }
                    ]
                    ```

                **Exemplo Few-Shot Complexo 2 (Falha de Mocking - `NullPointerException` em Dependência):**

                *   **Input (Parâmetros Injetados):**
                    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `OrderProcessor` que usa `OrderRepository`)
                    *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Códigos de `OrderRepository`, `Order`, `Product`)
                    *   `{{ DETALHES_TESTES_FALHOS_JSON }}`:
                        ```json
                        [
                          {
                            "method_name": "deveProcessarOrdem_quandoValida",
                            "error_message": "java.lang.NullPointerException: Cannot invoke \\"com.example.repository.OrderRepository.save(Object)\\" because \\"this.orderRepository\\" is null",
                            "stack_trace": "...at com.example.service.OrderProcessor.processOrder(OrderProcessor.java:25)\\n\\tat com.example.service.OrderProcessorTest.deveProcessarOrdem_quandoValida(OrderProcessorTest.java:40)\\n..."
                          }
                        ]
                        ```

                *   **Output JSON Esperado:**
                    ```json
                    [
                      {
                        "method_name": "deveProcessarOrdem_quandoValida",
                        "error_summary": "O teste falhou com NullPointerException porque o mock de OrderRepository não foi injetado ou inicializado corretamente na classe OrderProcessor.",
                        "detailed_analysis": "O método `processOrder` da classe `OrderProcessor` tenta chamar `orderRepository.save()`. O stack trace indica que `this.orderRepository` é nulo no momento da chamada. Isso geralmente ocorre quando a dependência (`OrderRepository`) não foi mockada e injetada na instância de `OrderProcessor` que está sendo testada. O teste provavelmente inicializou `OrderProcessor` sem passar o mock do repositório, ou esqueceu de chamar `MockitoAnnotations.openMocks(this)` no método `@BeforeEach` para injetar os mocks anotados com `@Mock` e `@InjectMocks`. O comportamento esperado era que `orderRepository` fosse uma instância mockada, mas era nula.",
                        "stack_trace_explanation": "A mensagem `java.lang.NullPointerException: Cannot invoke \\"com.example.repository.OrderRepository.save(Object)\\" because \\"this.orderRepository\\" is null` é clara: uma tentativa de chamar o método `save` em um objeto `orderRepository` que é nulo. A linha `OrderProcessor.processOrder(OrderProcessor.java:25)` aponta para a chamada `orderRepository.save(order)` dentro do método da classe alvo. O frame `OrderProcessorTest.deveProcessarOrdem_quandoValida(OrderProcessorTest.java:40)` indica que a execução do teste levou a essa `NullPointerException`. Isso confirma que o problema está na configuração do teste, onde o `orderRepository` não foi devidamente inicializado ou injetado na instância de `OrderProcessor` que o teste está utilizando.",
                        "solution_steps": [
                          "Verifique se a classe de teste possui as anotações `@Mock` para `OrderRepository` e `@InjectMocks` para `OrderProcessor`.",
                          "Certifique-se de que o método `@BeforeEach` da classe de teste chama `MockitoAnnotations.openMocks(this)` para inicializar e injetar os mocks.",
                          "Se a injeção for manual, garanta que o mock de `OrderRepository` seja passado para o construtor de `OrderProcessor` ao inicializá-lo no teste."
                        ]
                      }
                    ]
                    ```

                **Sua Tarefa:**

                Agora, analise os detalhes dos testes falhos (`{{ DETALHES_TESTES_FALHOS_JSON }}`, localizado na seção: **Detalhes dos Testes Falhos (JSON):**) para a classe (`%s`) e suas dependências (`%s`), e gere a resposta JSON no formato especificado, aplicando as técnicas de CoT e Few-Shot complexo.

                **Código da Classe Alvo:**

                ```java
                {{ CODIGO_CLASSE_ALVO }}
                %s
                ```

                **Códigos das Dependências (JSON):**

                ```json
                {{ CODIGOS_DEPENDENCIAS_JSON }}
                %s
                ```

                **Detalhes dos Testes Falhos (JSON):**

                ```json
                {{ DETALHES_TESTES_FALHOS_JSON }}
                %s
                ```

                **Resposta JSON:**

                ```json
                // Sua resposta JSON aqui
                ```
                """,
            targetClassName,
            dependenciesName,
            targetClassName,
            dependenciesName,
            context.getTargetClassCode(),
            dependenciesCode,
            failedTestsDetails);

    return new Prompt(prompt);
  }
}
