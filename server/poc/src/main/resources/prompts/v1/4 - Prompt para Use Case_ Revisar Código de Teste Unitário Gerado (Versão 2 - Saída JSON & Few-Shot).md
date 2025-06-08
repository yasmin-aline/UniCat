# Prompt para Use Case: Revisar Código de Teste Unitário Gerado (Versão 2 - Saída JSON & Few-Shot)

**Objetivo:** Revisar um código de teste JUnit 5 gerado, comparando-o com a classe alvo e os cenários de teste solicitados, verificando cobertura, correção das asserções, boas práticas e sugerindo melhorias. A resposta deve ser um JSON estruturado contendo a avaliação.

**Instruções:**

1.  **Entendimento:** Receba o código da classe alvo (`{{ CODIGO_CLASSE_ALVO }}`), o código de teste JUnit 5 gerado (`{{ CODIGO_TESTE_GERADO }}`), e a lista original de cenários de teste solicitados (`{{ CENARIOS_TESTE_JSON }}`).
2.  **Revisão Detalhada:** Analise o código de teste gerado em relação aos inputs.
    *   **Cobertura de Cenários:** Verifique se CADA cenário listado em `{{ CENARIOS_TESTE_JSON }}` (na chave `test_scenarios`) foi implementado como um método `@Test` distinto no código gerado.
    *   **Correção das Asserções:** Para cada método de teste, avalie criticamente se as asserções utilizadas (e seus valores esperados) correspondem CORRETAMENTE ao comportamento esperado da classe alvo (`{{ CODIGO_CLASSE_ALVO }}`) para aquele cenário específico. Considere a lógica da classe alvo.
    *   **Boas Práticas:** Verifique o uso adequado de anotações JUnit 5 (`@Test`, `@DisplayName`, `@BeforeEach`, etc.), a clareza da estrutura Arrange-Act-Assert, nomes de métodos e variáveis, e a ausência de lógica complexa dentro dos testes.
    *   **Sugestões:** Identifique possíveis cenários de teste importantes que foram omitidos ou sugira melhorias no código de teste existente (e.g., refatoração, asserções mais específicas).
3.  **Formato JSON de Saída:** Retorne sua avaliação EXCLUSIVAMENTE como um objeto JSON válido, seguindo a estrutura definida abaixo.

**Estrutura JSON de Saída Esperada:**

```json
{
  "review_summary": {
    "overall_assessment": "<Avaliação geral da qualidade e completude do teste gerado, e.g., 'Bom', 'Razoável com problemas', 'Incompleto'>",
    "coverage_check": {
      "status": "<OK | INCOMPLETO>",
      "missing_scenarios": [
        "<ID do cenário faltante 1 (se houver)>",
        // ...
      ],
      "comments": "<Comentários sobre a cobertura>"
    },
    "correctness_check": {
      "status": "<OK | PROBLEMAS_ENCONTRADOS>",
      "issues": [
        {
          "test_method": "<Nome do método de teste com problema>",
          "issue_description": "<Descrição do problema na asserção ou lógica do teste>"
        }
        // ... mais problemas
      ],
      "comments": "<Comentários sobre a correção das asserções>"
    },
    "best_practices_check": {
      "status": "<OK | PONTOS_DE_MELHORIA>",
      "suggestions": [
        "<Sugestão de melhoria 1>",
        // ...
      ],
      "comments": "<Comentários sobre boas práticas>"
    },
    "additional_suggestions": [
      "<Sugestão adicional 1 (e.g., novo cenário)>",
      // ...
    ]
  }
}
```

**Exemplo Few-Shot (Genérico):**

*   **Input (Parâmetros Injetados):**
    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe SimpleDiscountCalculator)
    *   `{{ CODIGO_TESTE_GERADO }}`: (Código da classe SimpleDiscountCalculatorTest gerado no prompt anterior, *supondo que implementou todos os 11 cenários*)
    *   `{{ CENARIOS_TESTE_JSON }}`: (JSON com os 11 cenários de SimpleDiscountCalculator identificados anteriormente)

*   **Output JSON Esperado (Exemplo de um teste correto e completo):**
    ```json
    {
      "review_summary": {
        "overall_assessment": "Bom",
        "coverage_check": {
          "status": "OK",
          "missing_scenarios": [],
          "comments": "Todos os 11 cenários solicitados foram implementados."
        },
        "correctness_check": {
          "status": "OK",
          "issues": [],
          "comments": "As asserções em todos os testes parecem corretas, incluindo a comparação de BigDecimal e o tratamento de exceções para inputs inválidos."
        },
        "best_practices_check": {
          "status": "OK",
          "suggestions": [
             "Considerar criar constantes para valores mágicos de desconto (e.g., 10, 50, 60) para melhor legibilidade."
          ],
          "comments": "O código segue boas práticas do JUnit 5. O método auxiliar createProduct é útil."
        },
        "additional_suggestions": [
          "Testar com valores BigDecimal de alta precisão ou escala, se relevante para o domínio.",
          "Testar o comportamento com DiscountType.FIXED_AMOUNT negativo, se a classe permitir (atualmente lança IAE, o que está coberto)."
        ]
      }
    }
    ```

**Sua Tarefa:**

Agora, revise o código de teste (`{{ CODIGO_TESTE_GERADO }}`) gerado para a classe (`{{ CODIGO_CLASSE_ALVO }}`), comparando com os cenários solicitados (`{{ CENARIOS_TESTE_JSON }}`), e forneça sua avaliação no formato JSON especificado.

**Código da Classe Alvo:**

```java
{{ CODIGO_CLASSE_ALVO }}
```

**Código de Teste Gerado:**

```java
{{ CODIGO_TESTE_GERADO }}
```

**Cenários de Teste Solicitados (JSON):**

```json
{{ CENARIOS_TESTE_JSON }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```

