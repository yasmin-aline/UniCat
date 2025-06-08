# Prompt para Use Case: Corrigir Testes Unitários Falhos (Versão 3 - Saída JSON, CoT & Few-Shot Complexo com Análise de Cobertura)

**Objetivo:** Analisar testes unitários JUnit 5 que falharam, identificar a causa raiz via Chain-of-Thought (CoT), corrigir os métodos de teste ou comentá-los com explicação se forem logicamente impossíveis de passar com o código atual. **Priorizar a correção para alcançar 100% de cobertura de linha e sucesso na execução dos testes.** Retornar apenas os métodos modificados (corrigidos ou comentados) e quaisquer novos imports necessários em formato JSON.

**Instruções:**

1.  **Entendimento Completo:** Receba o código da classe alvo (`{{ CODIGO_CLASSE_ALVO }}`), o código de suas dependências (`{{ CODIGOS_DEPENDENCIAS_JSON }}`), o código completo da classe de teste original (`{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`), uma lista detalhada dos testes que falharam (`{{ DETALHES_TESTES_FALHOS_JSON }}`), e **informações de cobertura de linha da classe alvo** (`{{ COBERTURA_LINHA_JSON }}`). Você possui todas as informações necessárias para diagnosticar e corrigir os testes de forma autônoma e completa.

2.  **Análise Chain-of-Thought (CoT) por Teste Falho:** Para CADA teste listado em `{{ DETALHES_TESTES_FALHOS_JSON }}`, realize um raciocínio passo a passo INTERNO e DETALHADO para diagnosticar a falha. **Considere também as informações de cobertura de linha para entender se a falha está relacionada a um caminho de código não coberto ou mal testado.**
    *   **Localize o Teste:** Identifique o método de teste correspondente em `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`.
    *   **Analise a Falha:** Examine a mensagem de erro e o stack trace fornecidos em `{{ DETALHES_TESTES_FALHOS_JSON }}`. Qual asserção falhou? Qual foi o valor esperado vs. o valor real? Onde a exceção ocorreu?
    *   **Analise a Lógica do Teste:** Revise a seção Arrange (setup), Act (execução) e Assert (verificação) do método de teste. Os dados de entrada (Arrange) estão corretos para o cenário pretendido? A chamada no Act está correta? A asserção (Assert) está verificando a condição correta e usando o método de comparação adequado (e.g., `assertEquals(0, expected.compareTo(actual))` para BigDecimal, `assertThrows` para exceções)? **Verifique se a configuração dos mocks está alinhada com o comportamento esperado para o cenário.**
    *   **Analise a Lógica da Classe Alvo:** Revise o `{{ CODIGO_CLASSE_ALVO }}` (e `{{ CODIGOS_DEPENDENCIAS_JSON }}` se relevante) para entender como ele *deveria* se comportar com os inputs fornecidos no teste. A lógica da classe corresponde à expectativa do teste? **Identifique se a falha ocorre em uma linha de código que não está sendo coberta ou que possui uma lógica complexa que não foi totalmente explorada pelo teste.**
    *   **Diagnóstico:** Conclua a causa raiz da falha. Exemplos: erro na lógica do teste (setup errado, asserção incorreta, mock mal configurado), erro sutil na lógica da classe alvo que o teste revelou, ou incompatibilidade fundamental entre o cenário do teste e a implementação da classe.

3.  **Ação (Correção ou Comentário):**
    *   **Prioridade:** Sua principal prioridade é **corrigir o teste** para que ele passe e contribua para a cobertura de 100% da classe alvo. **SÓ COMENTE UM TESTE SE FOR ABSOLUTAMENTE IMPOSSÍVEL FAZÊ-LO PASSAR COM A LÓGICA ATUAL DA CLASSE ALVO E SE ISSO NÃO COMPROMETER A COBERTURA DE LINHA ALMEJADA.**
    *   **Se a Falha for Corrigível no Teste:** Modifique o método de teste original para corrigir o problema identificado (e.g., ajustar o Arrange, corrigir a asserção, **reconfigurar mocks**). Mantenha o `@DisplayName` original.
    *   **Se a Falha for Devido à Lógica da Classe Alvo (Teste Impossível de Passar SEM ALTERAR A CLASSE ALVO):** Se, após uma análise exaustiva, você determinar que o teste não pode passar sem uma alteração na lógica da classe alvo, e se o cenário coberto por este teste já estiver sendo coberto por outro teste funcional, ou se a linha de código que este teste tenta cobrir já estiver coberta por outro teste, então você pode comentar o teste. Pegue o código ORIGINAL do método de teste, comente-o inteiramente (bloco de comentário `/* ... */`), e adicione um comentário explicativo logo acima do bloco comentado, detalhando *por que* o teste não pode passar com a lógica atual da classe alvo e *se* isso afeta a cobertura total. Exemplo:
        ```java
        // TESTE COMENTADO: Este cenário espera que o desconto fixo possa negativar o preço,
        // mas a lógica atual em SimpleDiscountCalculator impede isso, retornando zero.
        // A linha de código relevante já é coberta pelo teste 'deveAplicarDescontoFixo_quandoResultadoZero'.
        /*
        @Test
        @DisplayName("Testar desconto fixo que negativaria o preço")
        void calculateDiscountedPrice_shouldAllowNegativePrice_forLargeFixedDiscount() {
            // Arrange
            ProductDTO product = createProduct(10.00);
            BigDecimal expectedPrice = BigDecimal.valueOf(-5.00);
            // Act
            BigDecimal actualPrice = calculator.calculateDiscountedPrice(product, DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(15.00));
            // Assert
            assertEquals(0, expectedPrice.compareTo(actualPrice)); // Falharia, atual retorna 0
        }
        */
        ```
    *   **Preservar Testes Funcionais:** NÃO modifique ou inclua na resposta nenhum método de teste que NÃO estava listado em `{{ DETALHES_TESTES_FALHOS_JSON }}`.

4.  **Identificar Novos Imports:** Verifique se as suas correções introduziram a necessidade de novas classes/imports que não estavam presentes no `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`.

5.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo uma lista dos métodos modificados (corrigidos ou comentados) e uma lista dos novos imports necessários, conforme a estrutura abaixo.

**Estrutura JSON de Saída Esperada:**

```json
{
  "modified_test_methods": [
    {
      "method_name": "<Nome do método de teste corrigido/comentado 1>",
      "modified_code": "<Código completo do método de teste corrigido OU comentado com explicação, como uma string Java>"
    },
    {
      "method_name": "<Nome do método de teste corrigido/comentado 2>",
      "modified_code": "<Código completo do método de teste corrigido OU comentado com explicação, como uma string Java>"
    }
    // ... mais métodos modificados
  ],
  "required_new_imports": [
    "<FQN do novo import necessário 1>",
    "<FQN do novo import necessário 2>"
    // ... mais imports, se houver
  ]
}
```

**Exemplo Few-Shot Complexo (Genérico com Análise de Cobertura):**

*   **Input (Parâmetros Injetados):**
    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `SimpleDiscountCalculator` que aplica cap de 50% em percentual e não negativa preço em fixo)
    *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Códigos de `ProductDTO` e `DiscountType`)
    *   `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`:
        ```java
        package com.example.service;
        // ... imports originais (sem org.junit.jupiter.params.*)
        class SimpleDiscountCalculatorTest {
            // ... setUp e createProduct
            @Test @DisplayName("Testar cap de 50%")
            void testPercentageCap() { /* ... código correto ... */ }

            @Test @DisplayName("Testar comparação BigDecimal com equals")
            void testBigDecimalComparisonError() {
                ProductDTO product = createProduct(100.0); 
                BigDecimal expected = BigDecimal.valueOf(90.0); // Escala 1
                BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN); // Pode retornar 90.00 (Escala 2)
                assertEquals(expected, actual); // FALHA: BigDecimal.equals compara valor E escala
            }

            @Test @DisplayName("Testar cenário impossível de preço negativo")
            void testImpossibleNegativePrice() {
                ProductDTO product = createProduct(10.0); 
                BigDecimal expected = BigDecimal.valueOf(-5.0);
                BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(15.0));
                assertEquals(0, expected.compareTo(actual)); // FALHA: Atual retorna 0, não -5
            }
            // ... outros testes que passaram
        }
        ```
    *   `{{ DETALHES_TESTES_FALHOS_JSON }}`:
        ```json
        [
          {
            "method_name": "testBigDecimalComparisonError",
            "error_message": "org.opentest4j.AssertionFailedError: expected: java.math.BigDecimal<90.0> but was: java.math.BigDecimal<90.00>",
            "stack_trace": "...at SimpleDiscountCalculatorTest.testBigDecimalComparisonError(SimpleDiscountCalculatorTest.java:XX)..."
          },
          {
            "method_name": "testImpossibleNegativePrice",
            "error_message": "org.opentest4j.AssertionFailedError: expected: <-5> but was: <0>",
            "stack_trace": "...at SimpleDiscountCalculatorTest.testImpossibleNegativePrice(SimpleDiscountCalculatorTest.java:YY)..."
          }
        ]
        ```
    *   `{{ COBERTURA_LINHA_JSON }}`:
        ```json
        {
          "class_fqn": "com.example.service.SimpleDiscountCalculator",
          "lines_total": 50,
          "lines_covered": 48,
          "lines_missed": [
            {"line": 35, "reason": "Condição 'if (value < 0)' não coberta"}
          ],
          "coverage_percentage": 96.0
        }
        ```

*   **Output JSON Esperado:**
    ```json
    {
      "modified_test_methods": [
        {
          "method_name": "testBigDecimalComparisonError",
          "modified_code": "    @Test\n    @DisplayName(\"Testar comparação BigDecimal com equals\")\n    void testBigDecimalComparisonError() {\n        // Arrange\n        ProductDTO product = createProduct(100.0); \n        BigDecimal expected = BigDecimal.valueOf(90.00); \n        \n        // Act\n        BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN); \n        \n        // Assert\n        assertNotNull(actual);\n        assertEquals(0, expected.compareTo(actual), \"BigDecimal values should be numerically equal\"); \n    }"
        },
        {
          "method_name": "testImpossibleNegativePrice",
          "modified_code": "        // TESTE COMENTADO: Este cenário espera que o desconto fixo possa negativar o preço,\n        // mas a lógica atual em SimpleDiscountCalculator impede isso, retornando zero.\n        // A linha de código relevante já é coberta pelo teste 'deveAplicarDescontoFixo_quandoResultadoZero'.\n        /*\n        @Test @DisplayName(\"Testar cenário impossível de preço negativo\")\n        void testImpossibleNegativePrice() {\n            ProductDTO product = createProduct(10.0); \n            BigDecimal expected = BigDecimal.valueOf(-5.0);\n            BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(15.0));\n            assertEquals(0, expected.compareTo(actual)); \n        }\n        */"
        }
      ],
      "required_new_imports": [] 
    }
    ```

**Sua Tarefa:**

Agora, processe os detalhes dos testes falhos (`{{ DETALHES_TESTES_FALHOS_JSON }}`) para a classe (`{{ CODIGO_CLASSE_ALVO }}`) e suas dependências (`{{ CODIGOS_DEPENDENCIAS_JSON }}`), usando o código de teste original (`{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`) e as informações de cobertura (`{{ COBERTURA_LINHA_JSON }}`) como referência. Realize a análise CoT para cada falha e gere a resposta JSON contendo os métodos corrigidos/comentados e novos imports.

**Código da Classe Alvo:**

```java
{{ CODIGO_CLASSE_ALVO }}
```

**Códigos das Dependências (JSON):**

```json
{{ CODIGOS_DEPENDENCIAS_JSON }}
```

**Código de Teste Original Completo:**

```java
{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}
```

**Detalhes dos Testes Falhos (JSON):**

```json
{{ DETALHES_TESTES_FALHOS_JSON }}
```

**Informações de Cobertura de Linha (JSON):**

```json
{{ COBERTURA_LINHA_JSON }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```


