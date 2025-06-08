# Prompt para Use Case: StackTrace Interpreter (Novo Prompt 5)

**Objetivo:** Analisar detalhes de testes falhos (nome do método e stack trace), realizar uma análise profunda da causa raiz do erro, reanalisar a lógica da classe alvo em relação ao cenário falho, explicar detalhadamente o stack trace e fornecer um passo a passo para a correção do problema. A saída deve ser um JSON robusto com informações detalhadas para cada método falho.

**Instruções:**

1.  **Entendimento:** Receba o código da classe alvo (`{{ CODIGO_CLASSE_ALVO }}`), o código de suas dependências (`{{ CODIGOS_DEPENDENCIAS_JSON }}`), e uma lista de detalhes de testes falhos (`{{ DETALHES_TESTES_FALHOS_JSON }}`).

2.  **Análise Chain-of-Thought (CoT) por Teste Falho:** Para CADA teste listado em `{{ DETALHES_TESTES_FALHOS_JSON }}`, realize um raciocínio passo a passo INTERNO e DETALHADO para diagnosticar a falha. Seu CoT deve cobrir:
    *   **Localização e Contexto:** Identifique o método de teste falho e a linha exata no stack trace onde a exceção foi lançada ou a asserção falhou. Relacione isso com o código da classe alvo e suas dependências.
    *   **Reanálise da Lógica da Classe Alvo:** Foco na lógica do cenário de teste atual que está falhando. Explique por que o teste falhou, fazendo uma ponte clara entre o algoritmo original da classe alvo, o comportamento esperado e o comportamento que está sendo testado e que resultou na falha. Considere os inputs que levaram à falha.
    *   **Explicação Detalhada do StackTrace:** Analise cada linha relevante do stack trace, explicando o que cada chamada de método representa e como ela contribuiu para o erro. Relacione as mensagens de erro com o comportamento original da classe alvo.
    *   **Identificação da Causa Raiz:** Determine a causa raiz do problema. É um erro na lógica do teste (setup, asserção, mock), um erro na lógica da classe alvo, ou uma incompatibilidade entre o teste e a implementação?

3.  **Plano de Solução (Passo a Passo):** Com base na análise CoT, forneça um plano de solução detalhado, passo a passo, para corrigir o problema identificado. Este plano deve ser acionável e focado em como o teste ou a configuração do mock deve ser alterada para que o teste passe. Se a causa raiz for um erro na classe alvo, o plano deve indicar isso, mas o foco principal é a correção do teste.

4.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo uma lista de objetos, onde cada objeto representa um teste falho analisado, com as informações detalhadas da análise e do plano de solução.

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

**Exemplo Few-Shot (Genérico):**

*   **Input (Parâmetros Injetados):**
    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `SimpleDiscountCalculator`)
    *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Códigos de `ProductDTO` e `DiscountType`)
    *   `{{ DETALHES_TESTES_FALHOS_JSON }}`:
        ```json
        [
          {
            "method_name": "testBigDecimalComparisonError",
            "error_message": "org.opentest4j.AssertionFailedError: expected: java.math.BigDecimal<90.0> but was: java.math.BigDecimal<90.00>",
            "stack_trace": "...at SimpleDiscountCalculatorTest.testBigDecimalComparisonError(SimpleDiscountCalculatorTest.java:XX)..."
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
        "stack_trace_explanation": "O `org.opentest4j.AssertionFailedError` indica que uma asserção falhou. A mensagem `expected: <90.0> but was: <90.00>` confirma que os valores numéricos são os mesmos, mas a representação (escala) é diferente. A linha `SimpleDiscountCalculatorTest.testBigDecimalComparisonError(SimpleDiscountCalculatorTest.java:XX)` aponta para a linha da asserção no método de teste, confirmando que o problema está na forma como a comparação é feita, e não necessariamente na lógica de cálculo da classe alvo.",
        "solution_steps": [
          "Altere a asserção de `assertEquals(expected, actual)` para `assertEquals(0, expected.compareTo(actual))` para comparar apenas o valor numérico dos BigDecimals, ignorando a escala.",
          "Garanta que todos os imports necessários para `compareTo` e `BigDecimal` estejam presentes no arquivo de teste."
        ]
      }
    ]
    ```

**Sua Tarefa:**

Agora, analise os detalhes dos testes falhos (`{{ DETALHES_TESTES_FALHOS_JSON }}`) para a classe (`{{ CODIGO_CLASSE_ALVO }}`) e suas dependências (`{{ CODIGOS_DEPENDENCIAS_JSON }}`), e gere a resposta JSON no formato especificado.

**Código da Classe Alvo:**

```java
{{ CODIGO_CLASSE_ALVO }}
```

**Códigos das Dependências (JSON):**

```json
{{ CODIGOS_DEPENDENCIAS_JSON }}
```

**Detalhes dos Testes Falhos (JSON):**

```json
{{ DETALHES_TESTES_FALHOS_JSON }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```


