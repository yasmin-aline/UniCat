package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FixUnitTestsPromptGenerator {

	public Prompt get(
			final String dependencies,
			final String dependenciesList,
			final String originalTestClassCode,
			final String failingTestsDetails
	) {
		final var context = RequestContextHolder.getContext();
		final var targetClassName = context.getTargetClassPackage() + "." + context.getTargetClassName();

		String prompt = String.format("""
				# Prompt para Use Case: Corrigir Testes Unitários Falhos (Versão 2 - Saída JSON, CoT & Few-Shot Complexo)
				
				**Objetivo:** Analisar testes unitários JUnit 5 que falharam, identificar a causa raiz via Chain-of-Thought (CoT), corrigir os métodos de teste ou comentá-los com explicação se forem logicamente impossíveis de passar com o código atual. Retornar apenas os métodos modificados (corrigidos ou comentados) e quaisquer novos imports necessários em formato JSON.
				
				**Instruções:**
				
				1.  **Entendimento:** Receba o código da classe alvo (`%s`), o código de suas dependências (`%s`), o código completo da classe de teste original (localizado na seção: **Código de Teste Original Completo:**), e uma lista detalhada dos testes que falharam (encontrados na seção: **Detalhes dos Testes Falhos (JSON):**).
				
				2.  **Análise Chain-of-Thought (CoT) por Teste Falho:** Para CADA teste listado na seção **Detalhes dos Testes Falhos (JSON):**, realize um raciocínio passo a passo INTERNO e DETALHADO para diagnosticar a falha:
				    *   **Localize o Teste:** Identifique o método de teste correspondente na seção **Detalhes dos Testes Falhos (JSON):**.
				    *   **Analise a Falha:** Examine a mensagem de erro e o stack trace fornecidos localizado na seção **Código de Teste Original Completo:**. Qual asserção falhou? Qual foi o valor esperado vs. o valor real? Onde a exceção ocorreu?
				    *   **Analise a Lógica do Teste:** Revise a seção Arrange (setup), Act (execução) e Assert (verificação) do método de teste. Os dados de entrada (Arrange) estão corretos para o cenário pretendido? A chamada no Act está correta? A asserção (Assert) está verificando a condição correta e usando o método de comparação adequado (e.g., `assertEquals(0, expected.compareTo(actual))` para BigDecimal, `assertThrows` para exceções)?
				    *   **Analise a Lógica da Classe Alvo:** Revise o `%s` (e `%s` se relevante) para entender como ele *deveria* se comportar com os inputs fornecidos no teste. A lógica da classe corresponde à expectativa do teste?
				    *   **Diagnóstico:** Conclua a causa raiz da falha. Exemplos: erro na lógica do teste (setup errado, asserção incorreta), erro sutil na lógica da classe alvo que o teste revelou, ou incompatibilidade fundamental entre o cenário do teste e a implementação da classe.
				
				3.  **Ação (Correção ou Comentário):**
				    *   **Se a Falha for Corrigível no Teste:** Modifique o método de teste original para corrigir o problema identificado (e.g., ajustar o Arrange, corrigir a asserção). Mantenha o `@DisplayName` original.
				    *   **Se a Falha for Devido à Lógica da Classe Alvo (Teste Impossível):** NÃO modifique o teste para fazê-lo passar artificialmente. Em vez disso, pegue o código ORIGINAL do método de teste, comente-o inteiramente (bloco de comentário `/* ... */`), e adicione um comentário explicativo logo acima do bloco comentado, detalhando *por que* o teste não pode passar com a lógica atual da classe alvo. Exemplo:
				        ```java
				        // TESTE COMENTADO: Este cenário espera que o desconto fixo possa negativar o preço,
				        // mas a lógica atual em SimpleDiscountCalculator impede isso, retornando zero.
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
				    *   **Preservar Testes Funcionais:** NÃO modifique ou inclua na resposta nenhum método de teste que NÃO estava listado na seção **Detalhes dos Testes Falhos (JSON):**.
				
				4.  **Identificar Novos Imports:** Verifique se as suas correções introduziram a necessidade de novas classes/imports que não estavam presentes na seção **Código de Teste Original Completo:**.
				
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
				
				**Exemplo Few-Shot Complexo (Genérico):**
				
				*   **Input (Parâmetros Injetados):**
				    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `SimpleDiscountCalculator` que aplica cap de 50%% em percentual e não negativa preço em fixo)
				    *   `{{ CODIGO_DEPENDENCIAS }}`: (Códigos de `ProductDTO` e `DiscountType`)
				    *   `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`:
				        ```java
				        package com.example.service;
				        // ... imports originais (sem org.junit.jupiter.params.*)
				        class SimpleDiscountCalculatorTest {
				            // ... setUp e createProduct
				            @Test @DisplayName("Testar cap de 50%%")
				            void testPercentageCap() { /* ... código correto ... */ }
				
				            @Test @DisplayName("Testar comparação BigDecimal com equals")
				            void testBigDecimalComparisonError() {
				                ProductDTO product = createProduct(100.0);\s
				                BigDecimal expected = BigDecimal.valueOf(90.0); // Escala 1
				                BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN); // Pode retornar 90.00 (Escala 2)
				                assertEquals(expected, actual); // FALHA: BigDecimal.equals compara valor E escala
				            }
				
				            @Test @DisplayName("Testar cenário impossível de preço negativo")
				            void testImpossibleNegativePrice() {
				                ProductDTO product = createProduct(10.0);\s
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
				
				*   **Output JSON Esperado:**
				    ```json
				    {
				      "modified_test_methods": [
				        {
				          "method_name": "testBigDecimalComparisonError",
				          "modified_code": "    @Test\\n    @DisplayName(\\"Testar comparação BigDecimal com equals\\")\\n    void testBigDecimalComparisonError() {\\n        // Arrange\\n        ProductDTO product = createProduct(100.0); \\n        BigDecimal expected = BigDecimal.valueOf(90.00); // Ajustar escala ou usar compareTo\\n        \\n        // Act\\n        BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN); \\n        \\n        // Assert\\n        assertNotNull(actual);\\n        assertEquals(0, expected.compareTo(actual), \\"BigDecimal values should be numerically equal\\"); // CORREÇÃO: Usar compareTo para igualdade numérica\\n    }"
				        },
				        {
				          "method_name": "testImpossibleNegativePrice",
				          "modified_code": "        // TESTE COMENTADO: Este cenário espera que o desconto fixo possa negativar o preço,\\n        // mas a lógica atual em SimpleDiscountCalculator impede isso, retornando zero.\\n        /*\\n        @Test @DisplayName(\\"Testar cenário impossível de preço negativo\\")\\n        void testImpossibleNegativePrice() {\\n            ProductDTO product = createProduct(10.0); \\n            BigDecimal expected = BigDecimal.valueOf(-5.0);\\n            BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(15.0));\\n            assertEquals(0, expected.compareTo(actual)); // FALHA: Atual retorna 0, não -5\\n        }\\n        */"
				        }
				      ],
				      "required_new_imports": [] // Nenhum novo import foi necessário neste exemplo
				    }
				    ```
				
				**Sua Tarefa:**
				
				Agora, processe os detalhes dos testes falhos (localizados na seção: **Detalhes dos Testes Falhos (JSON):**) para a classe (`%s`) e suas dependências (`%s`), usando o código de teste original (encontrado na seção: **Código de Teste Original Completo:**) como referência. Realize a análise CoT para cada falha e gere a resposta JSON contendo os métodos corrigidos/comentados e novos imports.
				
				**Código da Classe Alvo:**
				
				```java
				%s
				```
				
				**Código das Dependências:**
				
				```java
				%s
				```
				
				**Código de Teste Original Completo:**
				
				```java
				%s
				```
				
				**Detalhes dos Testes Falhos (JSON):**
				
				```json
				%s
				```
				
				**Resposta JSON:**
				
				```json
				// Sua resposta JSON aqui
				```
				""", targetClassName, dependenciesList, targetClassName, dependenciesList, targetClassName, dependenciesList, context.getTargetClassCode(), dependencies,
				originalTestClassCode, failingTestsDetails);

		return new Prompt(prompt);
	}
}