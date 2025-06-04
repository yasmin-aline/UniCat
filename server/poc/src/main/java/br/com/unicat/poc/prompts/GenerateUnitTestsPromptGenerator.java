package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class GenerateUnitTestsPromptGenerator {

  public Prompt get() {
    RequestContext context = RequestContextHolder.getContext();
    final String scenariosJson = "";
    final String dependencies = "";
    String prompt =
        String.format(
            """
				# Prompt para Use Case: Gerar Código de Teste Unitário (Versão 2 - Saída JSON & Few-Shot)

				**Objetivo:** Gerar o código Java completo de uma classe de teste JUnit 5, implementando uma lista específica de cenários de teste, com base no código da classe alvo e suas dependências. A resposta deve ser um JSON contendo o FQN da classe de teste e o código gerado.

				**Instruções:**

				1.  **Entendimento:** Receba o código da classe alvo (`{{ NOME_COMPLETO_CLASSE_ALVO }}`), o código de suas dependências (`{{ CODIGO_DEPENDENCIAS }}`), e uma lista estruturada de cenários de teste (`{{ CENARIOS_TESTE_JSON }}`).
				2.  **Geração de Código:** Crie uma classe de teste JUnit 5 completa no pacote correto (geralmente o mesmo da classe alvo, mas em `src/test/java`).
				    *   Implemente CADA cenário de teste fornecido no JSON `{{ CENARIOS_TESTE_JSON }}` como um método `@Test` separado.
				    *   Use `@DisplayName` para cada método de teste, utilizando a `description` do cenário correspondente.
				    *   Siga rigorosamente a estrutura Arrange-Act-Assert.
				    *   Utilize as asserções apropriadas do JUnit 5 (e.g., `assertEquals`, `assertNull`, `assertThrows`) conforme sugerido pelo `expected_outcome_type` do cenário, mas adapte conforme necessário para a verificação completa. Use `BigDecimal.valueOf()` para criar valores e `assertEquals(0, expected.compareTo(actual))` para comparar BigDecimals.
				    *   Inclua um método `@BeforeEach` para inicializar a classe alvo (`{{ NOME_COMPLETO_CLASSE_ALVO }}`) se for reutilizável entre os testes.
				    *   Garanta que todos os imports necessários estão presentes.
				3.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo o FQN da classe de teste gerada e o código completo, conforme a estrutura abaixo.

				**Estrutura JSON de Saída Esperada:**

				```json
				{
				  "generated_test_class_fqn": "<FQN completo da classe de teste gerada, e.g., com.example.MyClassTest>",
				  "generated_test_code": "<Código Java completo da classe de teste como uma string, incluindo package e imports>"
				}
				```

				**Exemplo Few-Shot (Genérico):**

				*   **Input (Parâmetros Injetados):**
				    *   `{{ NOME_COMPLETO_CLASSE_ALVO }}`: `com.example.service.SimpleDiscountCalculator`
				    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe SimpleDiscountCalculator)
				    *   `{{ CODIGO_DEPENDENCIAS }}`: (Códigos de ProductDTO e DiscountType)
				    *   `{{ CENARIOS_TESTE_JSON }}`:
				        ```json
				        {
				          "class_fqn": "com.example.service.SimpleDiscountCalculator",
				          "analysis_summary": "...",
				          "test_scenarios": [
				            {"id": "scenario_1", "description": "Testar desconto percentual válido abaixo do cap (e.g., 10%)", "expected_outcome_type": "ASSERT_EQUALS"},
				            {"id": "scenario_2", "description": "Testar desconto percentual exatamente no cap (50%)", "expected_outcome_type": "ASSERT_EQUALS"},
				            {"id": "scenario_3", "description": "Testar desconto percentual acima do cap (e.g., 60%) deve aplicar o cap (50%)", "expected_outcome_type": "ASSERT_EQUALS"},
				            {"id": "scenario_6", "description": "Testar desconto de valor fixo válido que não zera o preço", "expected_outcome_type": "ASSERT_EQUALS"},
				            {"id": "scenario_8", "description": "Testar desconto de valor fixo maior que o preço (deve resultar em preço zero)", "expected_outcome_type": "ASSERT_EQUALS"},
				            {"id": "scenario_9", "description": "Testar com produto nulo", "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException"}
				            // ... (outros cenários omitidos para brevidade do exemplo)
				          ]
				        }
				        ```

				*   **Output JSON Esperado:**
				    ```json
				    {
				      "generated_test_class_fqn": "com.example.service.SimpleDiscountCalculatorTest",
				      "generated_test_code": "package com.example.service;\\n\\nimport com.example.dto.ProductDTO;\\nimport com.example.enums.DiscountType;\\nimport org.junit.jupiter.api.BeforeEach;\\nimport org.junit.jupiter.api.DisplayName;\\nimport org.junit.jupiter.api.Test;\\nimport java.math.BigDecimal;\\n\\nimport static org.junit.jupiter.api.Assertions.*;\\n\\nclass SimpleDiscountCalculatorTest {\\n\\n    private SimpleDiscountCalculator calculator;\\n\\n    @BeforeEach\\n    void setUp() {\\n        calculator = new SimpleDiscountCalculator();\\n    }\\n\\n    private ProductDTO createProduct(double price) {\\n        // Suposição: ProductDTO tem construtor ou builder\\n        return new ProductDTO(BigDecimal.valueOf(price)); \\n    }\\n\\n    @Test\\n    @DisplayName(\\"Testar desconto percentual válido abaixo do cap (e.g., 10%)\\")\\n    void calculateDiscountedPrice_shouldApplyValidPercentageDiscount() {\\n        // Arrange\\n        ProductDTO product = createProduct(100.00);\\n        BigDecimal expectedPrice = BigDecimal.valueOf(90.00);\\n\\n        // Act\\n        BigDecimal actualPrice = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.valueOf(10));\\n\\n        // Assert\\n        assertNotNull(actualPrice);\\n        assertEquals(0, expectedPrice.compareTo(actualPrice));\\n    }\\n\\n    // ... (Implementação dos outros métodos de teste baseados nos cenários) ...\\n\\n    @Test\\n    @DisplayName(\\"Testar com produto nulo\\")\\n    void calculateDiscountedPrice_shouldThrowExceptionForNullProduct() {\\n        // Arrange\\n        // Act & Assert\\n        assertThrows(IllegalArgumentException.class, () -> {\\n            calculator.calculateDiscountedPrice(null, DiscountType.PERCENTAGE, BigDecimal.TEN);\\n        });\\n    }\\n}"
				    }
				    ```

				**Sua Tarefa:**

				Agora, gere o código de teste JUnit 5 para a classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` com base nos códigos e cenários fornecidos abaixo, retornando a resposta no formato JSON especificado.

				**Código da Classe Alvo (`{{ NOME_COMPLETO_CLASSE_ALVO }}`):**

				```java
				{{ CODIGO_CLASSE_ALVO }}
				```

				**Código das Dependências:**

				```java
				{{ CODIGO_DEPENDENCIAS }}
				```

				**Cenários de Teste (JSON):**

				```json
				{{ CENARIOS_TESTE_JSON }}
				```

				**Resposta JSON:**

				```json
				// Sua resposta JSON aqui
				```
				""",
            context.getTargetClassName(),
            context.getTargetClassCode(),
            scenariosJson,
            scenariosJson,
            context.getTargetClassName(),
            context.getTargetClassName(),
            context.getTargetClassName(),
            context.getTargetClassCode(),
            dependencies,
            scenariosJson);

    return new Prompt(prompt);
  }
}
