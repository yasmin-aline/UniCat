# Prompt para Use Case: Analisar Lógica e Identificar Cenários de Teste (Versão 5 - Saída JSON, CoT & Few-Shot Complexo com Mocking e Snippets Java, Foco em Cobertura Real e Métodos Privados)

**Objetivo:** Realizar uma análise profunda e passo a passo (Chain-of-Thought) da lógica de uma classe Java e suas dependências, identificar fluxos principais, casos de borda, potenciais erros, e propor uma lista abrangente de cenários de teste descritivos, **garantindo que reflitam APENAS o comportamento implementado na classe alvo**, e **incluindo estratégias para cobrir métodos privados através de seus chamadores públicos**, com o comportamento esperado das dependências mockadas para cada cenário e snippets de código Java válidos para argumentos e retornos. A saída será um JSON estruturado.

**Instruções:**

1.  **Análise Chain-of-Thought (CoT):** Antes de gerar o JSON final, realize internamente um raciocínio passo a passo detalhado sobre a lógica do método público principal da classe `$targetClassName$` (código abaixo), considerando também o código das dependências (`$dependenciesName$`). Seu CoT deve cobrir:
    *   **Fluxo Principal e Comportamento Implementado:** Descreva os passos lógicos centrais da execução, focando EXCLUSIVAMENTE no comportamento que JÁ ESTÁ IMPLEMENTADO na classe alvo. Não gere cenários para funcionalidades não existentes ou que extrapolam a lógica presente no código.
    *   **Estruturas de Controle:** Analise loops (`forEach`, `for`, `while`) e condicionais (`if`, `else`, `switch`, `try-catch`).
    *   **Entradas e Estados:** Considere o impacto de entradas nulas, listas/coleções vazias, objetos com campos nulos ou vazios, valores numéricos (zero, negativos, positivos, limites BigDecimal).
    *   **Casos de Borda:** Identifique limites (0, 1, N itens), valores mínimos/máximos, empates, descontos que zeram o preço, descontos que ultrapassam o preço, **APENAS se a lógica da classe alvo os tratar explicitamente**.
    *   **Tratamento de Erros:** Avalie blocos `try-catch` existentes. Antecipe exceções não tratadas (e.g., `NullPointerException`, `IllegalArgumentException` para valores inválidos), **se a lógica da classe alvo puder levar a elas**.
    *   **Interações com Dependências:** Detalhe como a classe alvo interage com suas dependências (métodos chamados, parâmetros passados, valores de retorno esperados). Isso é CRÍTICO para o mocking.
    *   **Cobertura de Métodos Privados:** Para cada método privado na classe alvo, identifique os métodos públicos (ou outros métodos que são acessíveis a partir de um método público) que o chamam. Analise o fluxo de dados e as condições que levam à execução de diferentes caminhos dentro do método privado. Seu CoT deve detalhar como os inputs para o método público chamador devem ser configurados, e quais mocks devem ser estabelecidos, para garantir que todas as linhas do método privado sejam exercitadas.
    *   **Robustez:** Avalie criticamente se a lógica é robusta contra os casos de borda e erros identificados, **dentro do escopo da implementação atual**.
2.  **Geração de Cenários com Comportamento de Mock:** Com base na sua análise CoT, derive uma lista COMPLETA de cenários de teste descritivos que cubram todos os fluxos, casos de borda e tratamentos de erro relevantes **conforme a implementação existente**. Para cada cenário:
    *   Indique brevemente o tipo de verificação principal (e.g., igualdade, nulidade, exceção).
    *   **MUITO IMPORTANTE:** Descreva o comportamento EXATO que as dependências mockadas devem ter para que este cenário seja testado corretamente. Isso inclui chamadas de método esperadas, parâmetros de entrada para essas chamadas e valores de retorno ou exceções que os mocks devem lançar. **Os campos `with_arguments` e `then_return` DEVEM conter snippets de código Java válidos, que possam ser diretamente inseridos em chamadas Mockito (e.g., `when(mock.method(with_arguments)).thenReturn(then_return)`).**
    *   **Cenários para Métodos Privados:** Para cenários que visam cobrir métodos privados, a descrição do cenário e o `mock_behavior` devem ser focados no método público (ou acessível) que invoca o método privado, garantindo que os dados de entrada e os mocks levem à execução do código privado.
3.  **Formato JSON:** Estruture sua resposta FINAL exclusivamente como um objeto JSON válido, conforme o esquema abaixo. Inclua um resumo da sua análise CoT e a lista de cenários.

**Estrutura JSON de Saída Esperada:**

```json
{
  "class_fqn": "{{ NOME_COMPLETO_CLASSE_ALVO }}",
  "analysis_summary": "\<Resumo conciso da análise CoT, destacando pontos críticos, casos de borda, interações com dependências e estratégias para cobertura de métodos privados, dentro do escopo da implementação atual\>",
  "test_scenarios": [
    {
      "id": "scenario_1",
      "description": "\<Descrição clara e concisa do cenário de teste 1, refletindo o comportamento implementado e, se aplicável, a cobertura de método privado via chamador\>",
      "expected_outcome_type": "\<Tipo de asserção principal, e.g., ASSERT_EQUALS, ASSERT_NOT_NULL, ASSERT_NULL, ASSERT_THROWS_IllegalArgumentException, ASSERT_TRUE\>",
      "mock_behavior": [
        {
          "dependency_fqn": "\<FQN da dependência a ser mockada, e.g., com.example.repository.ProductRepository\>",
          "method_call": "\<Assinatura do método a ser mockado, e.g., findById(Long id)\>",
          "with_arguments": "\<Snippet de código Java para os argumentos. Ex: `1L`, `any(Long.class)`, `new ProductDTO(\"Test\", BigDecimal.valueOf(10.0))`\>",
          "then_return": "\<Snippet de código Java para o valor de retorno. Ex: `new Product(1L, \"Product A\")`, `Optional.of(someProduct)`, `null`, `true`\>",
          "then_throw": "\<Exceção a ser lançada, e.g., new RuntimeException(\"Database error\")\>",
          "times": "\<Número de vezes que o método deve ser chamado, e.g., 1\>"
        }
        // ... mais comportamentos de mock para este cenário
      ]
    },
    // ... mais cenários
  ]
}
```

**Exemplo Few-Shot (Genérico com Mocking, Snippets Java explícitos e foco em método privado):**

*   **Input (Parâmetros Injetados):**
    *   `{{ NOME_COMPLETO_CLASSE_ALVO }}`: `com.example.service.DiscountService`
    *   `{{ CODIGO_CLASSE_ALVO }}`:
        ```java
        package com.example.service;
        import com.example.model.Product;
        import java.math.BigDecimal;

        public class DiscountService {

            public BigDecimal applyDiscount(Product product, BigDecimal discountPercentage) {
                if (product == null || discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Invalid input for discount application.");
                }
                BigDecimal finalPrice = calculateFinalPrice(product.getPrice(), discountPercentage);
                return finalPrice;
            }

            private BigDecimal calculateFinalPrice(BigDecimal originalPrice, BigDecimal discountPercentage) {
                if (originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    return originalPrice; // No discount for zero or negative price
                }
                BigDecimal discountAmount = originalPrice.multiply(discountPercentage.divide(BigDecimal.valueOf(100)));
                return originalPrice.subtract(discountAmount);
            }

            private boolean isProductEligibleForSpecialDiscount(Product product) {
                // Simula uma lógica interna complexa para desconto especial
                return product != null && product.getName().contains("Premium");
            }

            public BigDecimal getSpecialDiscountedPrice(Product product) {
                if (isProductEligibleForSpecialDiscount(product)) {
                    return applyDiscount(product, BigDecimal.valueOf(10)); // 10% de desconto especial
                }
                return product.getPrice();
            }
        }
        ```
    *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`:
        ```json
        {
          "com.example.model.Product": "package com.example.model;\nimport java.math.BigDecimal;\npublic class Product {\n    private String name;\n    private BigDecimal price;\n    // getters, setters, constructor\n    public Product(String name, BigDecimal price) { this.name = name; this.price = price; }\n    public String getName() { return name; }\n    public BigDecimal getPrice() { return price; }\n}"
        }
        ```

*   **Output JSON Esperado:**
    ```json
    {
      "class_fqn": "com.example.service.DiscountService",
      "analysis_summary": "A classe DiscountService aplica descontos a produtos. O método `applyDiscount` valida entradas e chama o método privado `calculateFinalPrice`. O método `getSpecialDiscountedPrice` verifica elegibilidade para desconto especial via `isProductEligibleForSpecialDiscount` (privado) e aplica desconto se elegível. A análise focará em cobrir todos os caminhos de `calculateFinalPrice` e `isProductEligibleForSpecialDiscount` através de chamadas a `applyDiscount` e `getSpecialDiscountedPrice`.",
      "test_scenarios": [
        {
          "id": "scenario_1",
          "description": "Aplicar desconto válido a um produto com preço positivo (cobre `calculateFinalPrice` - caminho de cálculo)",
          "expected_outcome_type": "ASSERT_EQUALS",
          "mock_behavior": [],
          "input_parameters": [
            "new com.example.model.Product(\"Laptop\", new java.math.BigDecimal(\"1000.00\"))",
            "new java.math.BigDecimal(\"10\")"
          ]
        },
        {
          "id": "scenario_2",
          "description": "Aplicar desconto a produto com preço zero (cobre `calculateFinalPrice` - caminho de preço zero)",
          "expected_outcome_type": "ASSERT_EQUALS",
          "mock_behavior": [],
          "input_parameters": [
            "new com.example.model.Product(\"Freebie\", new java.math.BigDecimal(\"0.00\"))",
            "new java.math.BigDecimal(\"10\")"
          ]
        },
        {
          "id": "scenario_3",
          "description": "Obter preço com desconto especial para produto elegível (cobre `isProductEligibleForSpecialDiscount` - true)",
          "expected_outcome_type": "ASSERT_EQUALS",
          "mock_behavior": [],
          "input_parameters": [
            "new com.example.model.Product(\"Premium Headset\", new java.math.BigDecimal(\"200.00\"))"
          ]
        },
        {
          "id": "scenario_4",
          "description": "Obter preço sem desconto especial para produto não elegível (cobre `isProductEligibleForSpecialDiscount` - false)",
          "expected_outcome_type": "ASSERT_EQUALS",
          "mock_behavior": [],
          "input_parameters": [
            "new com.example.model.Product(\"Basic Mouse\", new java.math.BigDecimal(\"50.00\"))"
          ]
        },
        {
          "id": "scenario_5",
          "description": "Tentar aplicar desconto com produto nulo (cobre validação de entrada)",
          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException",
          "mock_behavior": [],
          "input_parameters": [
            "null",
            "new java.math.BigDecimal(\"10\")"
          ]
        }
      ]
    }
    ```

**Sua Tarefa:**

Agora, aplique esta análise CoT e geração de cenários à classe `$targetClassName$` com os códigos fornecidos abaixo e gere a resposta JSON correspondente.

**Código da Classe Alvo (`$targetClassName$`):**

```java
$targetClassCode$
```

**Códigos das Dependências (JSON):**

```json
$dependenciesCode$
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```


