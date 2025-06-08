# Prompt para Use Case: Aplicar Revisão ao Código de Teste Gerado (Versão 2 - Saída JSON & Few-Shot)

**Objetivo:** Refatorar ou corrigir um código de teste JUnit 5 existente com base nas recomendações específicas fornecidas em um JSON de revisão. Aplicar **apenas** as mudanças apontadas na revisão.

**Instruções:**

1.  **Entendimento:** Receba o código de teste JUnit 5 original que foi gerado anteriormente (`{{ CODIGO_TESTE_ORIGINAL_GERADO }}`) e o JSON completo contendo a análise de revisão desse código (`{{ REVISAO_TESTE_JSON }}`).
2.  **Análise da Revisão:** Examine cuidadosamente o JSON de revisão (`{{ REVISAO_TESTE_JSON }}`), focando nas seções `correctness_check.issues` e `best_practices_check.suggestions`.
3.  **Aplicação das Modificações:** Modifique o `{{ CODIGO_TESTE_ORIGINAL_GERADO }}` aplicando **EXATAMENTE** e **APENAS** as correções e sugestões listadas nessas seções da revisão.
    *   Para cada `issue` em `correctness_check.issues`, localize o `test_method` correspondente no código original e aplique a correção descrita em `issue_description`.
    *   Para cada `suggestion` em `best_practices_check.suggestions`, aplique a melhoria sugerida no código (pode afetar múltiplos métodos ou a estrutura geral).
    *   **NÃO** altere partes do código que não foram explicitamente mencionadas como necessitando de correção ou melhoria na revisão.
    *   **NÃO** adicione novos testes ou remova testes existentes (a menos que a sugestão da revisão seja especificamente para remover um teste redundante, o que é raro).
4.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVamente como um objeto JSON válido, contendo o código de teste refatorado/corrigido, conforme a estrutura abaixo.

**Estrutura JSON de Saída Esperada:**

```json
{
  "refactored_test_code": "<Código Java completo da classe de teste APÓS aplicar as correções/sugestões da revisão, como uma string>"
}
```

**Exemplo Few-Shot (Genérico):**

*   **Input (Parâmetros Injetados):**
    *   `{{ CODIGO_TESTE_ORIGINAL_GERADO }}`:
        ```java
        package com.example.service;
        // ... imports ...
        class SimpleDiscountCalculatorTest {
            // ... setUp, createProduct ...
            @Test @DisplayName("Testar comparação BigDecimal com equals")
            void testBigDecimalComparisonError() {
                ProductDTO product = createProduct(100.0); 
                BigDecimal expected = BigDecimal.valueOf(90.0); 
                BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN);
                assertEquals(expected, actual); // <-- Ponto da falha original
            }
            // ... outros testes ...
        }
        ```
    *   `{{ REVISAO_TESTE_JSON }}`:
        ```json
        {
          "review_summary": {
            "overall_assessment": "Razoável com problemas",
            // ... outras seções da revisão ...
            "correctness_check": {
              "status": "PROBLEMAS_ENCONTRADOS",
              "issues": [
                {
                  "test_method": "testBigDecimalComparisonError",
                  "issue_description": "A asserção usa assertEquals(BigDecimal, BigDecimal), que compara escala além do valor. Use assertEquals(0, expected.compareTo(actual)) para comparação numérica."
                }
              ],
              "comments": "Principalmente erro na comparação de BigDecimal."
            },
            "best_practices_check": {
              "status": "PONTOS_DE_MELHORIA",
              "suggestions": [
                "Adicionar assertNotNull(actual) antes da comparação de valor no teste testBigDecimalComparisonError."
              ],
              "comments": ""
            },
            "additional_suggestions": []
          }
        }
        ```

*   **Output JSON Esperado:**
    ```json
    {
      "refactored_test_code": "package com.example.service;\n\n// ... imports ...\n\nclass SimpleDiscountCalculatorTest {\n    // ... setUp, createProduct ...\n\n    @Test\n    @DisplayName(\"Testar comparação BigDecimal com equals\")\n    void testBigDecimalComparisonError() {\n        // Arrange\n        ProductDTO product = createProduct(100.0); \n        BigDecimal expected = BigDecimal.valueOf(90.0); // Mantido como no original, mas a comparação mudou\n        \n        // Act\n        BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN);\n        \n        // Assert\n        assertNotNull(actual); // SUGESTÃO APLICADA\n        assertEquals(0, expected.compareTo(actual), \"BigDecimal values should be numerically equal\"); // CORREÇÃO APLICADA\n    }\n    // ... outros testes inalterados ...\n}"
    }
    ```

**Sua Tarefa:**

Agora, aplique as correções e sugestões do JSON de revisão (`{{ REVISAO_TESTE_JSON }}`) ao código de teste original (`{{ CODIGO_TESTE_ORIGINAL_GERADO }}`) e retorne o código resultante no formato JSON especificado.

**Código de Teste Original Gerado:**

```java
{{ CODIGO_TESTE_ORIGINAL_GERADO }}
```

**JSON da Revisão do Teste:**

```json
{{ REVISAO_TESTE_JSON }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```

