package br.com.unicat.poc.v1.prompts;

import br.com.unicat.poc.v1.adapter.http.context.RequestContextHolder;
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
      final String failingTestsDetails,
      final String coverageDetails,
      final String attemptNumber) {
    final var context = RequestContextHolder.getContext();
    final var targetClassName =
        context.getTargetClassPackage() + "." + context.getTargetClassName();

    String prompt =
        String.format(
            """
					# Prompt para Use Case: Corrigir Testes Unitários Falhos (Versão 5 - Saída JSON, CoT & Few-Shot Complexo com Análise de Cobertura e Otimização por Tentativas)

				   **Objetivo:** Analisar testes unitários JUnit 5 que falharam, identificar a causa raiz via Chain-of-Thought (CoT), corrigir os métodos de teste ou comentá-los com explicação se forem logicamente impossíveis de passar com o código atual. **Priorizar a correção para alcançar 100%% de cobertura de linha e sucesso na execução dos testes.** Retornar apenas os métodos modificados (corrigidos ou comentados) e quaisquer novos imports necessários em formato JSON.

				   **Instruções:**

				   1.  **Entendimento Completo:** Receba o código da classe alvo (`%s`), o código de suas dependências (`%s`), o código completo da classe de teste original (`{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`), uma lista detalhada dos testes que falharam (`{{ DETALHES_TESTES_FALHOS_JSON }}`), **informações de cobertura de linha da classe alvo** (`{{ COBERTURA_LINHA_JSON }}`), e o **número da tentativa atual** (`%s`). Você possui todas as informações necessárias para diagnosticar e corrigir os testes de forma autônoma e completa.
					   *   **Otimização por Tentativa:** Você terá um máximo de 5 tentativas para ajustar o código. À medida que `{{ ATTEMPT_NUMBER }}` (%s) se aproxima de 3, maximize a assertividade e a busca por correção nas suas respostas. Na 5ª tentativa, se ainda houver testes falhando, adicione comentários detalhados conforme instruído no ponto 3.c.

				   2.  **Análise Chain-of-Thought (CoT) por Teste Falho:** Para CADA teste listado em `{{ DETALHES_TESTES_FALHOS_JSON }}`, realize um raciocínio passo a passo INTERNO e DETALHADO para diagnosticar a falha. **Considere também as informações de cobertura de linha para entender se a falha está relacionada a um caminho de código não coberto ou mal testado.**
					   *   **Localize o Teste:** Identifique o método de teste correspondente em `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`.
					   *   **Analise a Falha:** Examine a mensagem de erro e o stack trace fornecidos em `{{ DETALHES_TESTES_FALHOS_JSON }}`. Qual asserção falhou? Qual foi o valor esperado vs. o valor real? Onde a exceção ocorreu?
					   *   **Analise a Lógica do Teste:** Revise a seção Arrange (setup), Act (execução) e Assert (verificação) do método de teste. Os dados de entrada (Arrange) estão corretos para o cenário pretendido? A chamada no Act está correta? A asserção (Assert) está verificando a condição correta e usando o método de comparação adequado (e.g., `assertEquals(0, expected.compareTo(actual))` para BigDecimal, `assertThrows` para exceções)? **Verifique se a configuração dos mocks está alinhada com o comportamento esperado para o cenário.**
					   *   **Analise a Lógica da Classe Alvo:** Revise o `%s` (e `%s` se relevante) para entender como ele *deveria* se comportar com os inputs fornecidos no teste. A lógica da classe corresponde à expectativa do teste? **Identifique se a falha ocorre em uma linha de código que não está sendo coberta ou que possui uma lógica complexa que não foi totalmente explorada pelo teste.**
					   *   **Diagnóstico:** Conclua a causa raiz da falha. Exemplos: erro na lógica do teste (setup errado, asserção incorreta, mock mal configurado), erro sutil na lógica da classe alvo que o teste revelou, ou incompatibilidade fundamental entre o cenário do teste e a implementação da classe.

				   3.  **Ação (Correção ou Comentário):**
					   *   **Prioridade:** Sua principal prioridade é **corrigir o teste** para que ele passe e contribua para a cobertura de 100%% da classe alvo. **SÓ COMENTE UM TESTE SE FOR ABSOLUTAMENTE IMPOSSÍVEL FAZÊ-LO PASSAR COM A LÓGICA ATUAL DA CLASSE ALVO E SE ISSO NÃO COMPROMETER A COBERTURA DE LINHA ALMEJADA.**
					   *   **Se a Falha for Corrigível no Teste:** Modifique o método de teste original para corrigir o problema identificado (e.g., ajustar o Arrange, corrigir a asserção, **reconfigurar mocks**).
					   *   **Se a Falha for Devido à Lógica da Classe Alvo (Teste Impossível de Passar SEM ALTERAR A CLASSE ALVO) OU Comportamento Incompatível:** Se, após uma análise exaustiva, você determinar que o teste não pode passar sem uma alteração na lógica da classe alvo, ou se o comportamento esperado no teste não estiver alinhado com o comportamento que ocorre no algoritmo testado, então você pode comentar o teste. Pegue o código ORIGINAL do método de teste, comente-o inteiramente (bloco de comentário `/* ... */`), e adicione um comentário explicativo logo acima do bloco comentado, detalhando *por que* o teste não pode passar com a lógica atual da classe alvo, *por que não foi possível solucionar o erro*, *o que o usuário poderia fazer para buscar corrigir*, e *se* isso afeta a cobertura total. Exemplo:
						   ```java
						   // TESTE COMENTADO: Este cenário espera que o desconto fixo possa negativar o preço,
						   // mas a lógica atual em SimpleDiscountCalculator impede isso, retornando zero.
						   // Não foi possível solucionar este erro pois a alteração exigiria modificação na lógica de negócio da classe alvo.
						   // O usuário poderia considerar ajustar a lógica de negócio para permitir valores negativos ou remover este teste se o comportamento atual for o desejado.
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
					   *   **Comentários Detalhados na 5ª Tentativa:** Se `
					   s` for igual a 5 e o teste ainda estiver falhando (ou for comentado por impossibilidade de correção), o comentário explicativo DEVE incluir:
						   *   Uma explicação clara do *porquê* o erro ocorreu.
						   *   Uma justificativa do *porquê não foi possível solucionar o erro* (ex: exige alteração na lógica de negócio da classe alvo, comportamento esperado do teste não alinhado com a implementação).
						   *   Sugestões sobre *o que o usuário poderia fazer para buscar corrigir* (ex: ajustar a lógica da classe alvo, reavaliar o comportamento esperado do teste, remover o teste se o cenário não for mais relevante).
					   *   **Preservar Testes Funcionais:** NÃO modifique ou inclua na resposta nenhum método de teste que NÃO estava listado em `{{ DETALHES_TESTES_FALHOS_JSON }}`.

				   4.  **Identificar Novos Imports:** Verifique se as suas correções introduziram a necessidade de novas classes/imports que não estavam presentes no `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`.

				   5.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo uma lista dos métodos modificados (corrigidos ou comentados) e uma lista dos novos imports necessários, conforme a estrutura abaixo.
						*   **Nunca acrescente qualquer outra informação no corpo de resposta além da estrutura JSON de saída esperada informada abaixo! O sistema que recebe a sua resposta espera somente um objeto JSON e se você adicionar qualquer texto antes ou depois, irá quebrar a aplicação e gerar um bug. Não faça isso. Retorne exatamente o que lhe foi pedido.**

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
					   *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `SimpleDiscountCalculator` que aplica cap de 50%% em percentual e não negativa preço em fixo)
					   *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Códigos de `ProductDTO` e `DiscountType`)
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
								   BigDecimal expected = BigDecimal.valueOf(-5.0); // Escala 1
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
					   *   `{{ ATTEMPT_NUMBER }}`: `1` (ou qualquer número de 1 a 5)

				   *   **Output JSON Esperado:**
					   ```json
					   {
						 "modified_test_methods": [
						   {
							 "method_name": "testBigDecimalComparisonError",
							 "modified_code": "    @Test\\n    @DisplayName(\\"Testar comparação BigDecimal com equals\\")\\n    void testBigDecimalComparisonError() {\\n        // Arrange\\n        ProductDTO product = createProduct(100.0); \\n        BigDecimal expected = BigDecimal.valueOf(90.00); \\n        \\n        // Act\\n        BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.TEN); \\n        \\n        // Assert\\n        assertNotNull(actual);\\n        assertEquals(0, expected.compareTo(actual), \\"BigDecimal values should be numerically equal\\"); \\n    }"
						   },
						   {
							 "method_name": "testImpossibleNegativePrice",
							 "modified_code": "        // TESTE COMENTADO: Este cenário espera que o desconto fixo possa negativar o preço,\\n        // mas a lógica atual em SimpleDiscountCalculator impede isso, retornando zero.\\n        // Não foi possível solucionar este erro pois a alteração exigiria modificação na lógica de negócio da classe alvo.\\n        // O usuário poderia considerar ajustar a lógica de negócio para permitir valores negativos ou remover este teste se o comportamento atual for o desejado.\\n        // A linha de código relevante já é coberta pelo teste 'deveAplicarDescontoFixo_quandoResultadoZero'.\\n        /*\\n        @Test @DisplayName(\\"Testar cenário impossível de preço negativo\\")\\n        void testImpossibleNegativePrice() {\\n            ProductDTO product = createProduct(10.0); \\n            BigDecimal expected = BigDecimal.valueOf(-5.0);\\n            BigDecimal actual = calculator.calculateDiscountedPrice(product, DiscountType.FIXED_AMOUNT, BigDecimal.valueOf(15.0));\\n            assertEquals(0, expected.compareTo(actual)); \\n        }\\n        */"
						   }
						 ],
						 "required_new_imports": []\s
					   }
					   ```

				   **Exemplo Few-Shot (Comentário Detalhado na 5ª Tentativa):**

				   *   **Input (Parâmetros Injetados):**
					   *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `PaymentProcessor` que não lida com moedas diferentes)
					   *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Códigos de `Transaction` e `Currency`)
					   *   `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`:
						   ```java
						   package com.example.payment;
						   // ... imports
						   class PaymentProcessorTest {
							   // ... setup
							   @Test @DisplayName("Deve falhar ao processar transação com moeda diferente")
							   void processTransaction_shouldFailWithDifferentCurrency() {
								   Transaction transaction = new Transaction(BigDecimal.TEN, Currency.USD); // Classe alvo só aceita BRL
								   assertThrows(IllegalArgumentException.class, () -> processor.process(transaction));
							   }
						   }
						   ```
					   *   `{{ DETALHES_TESTES_FALHOS_JSON }}`:
						   ```json
						   [
							 {
							   "method_name": "processTransaction_shouldFailWithDifferentCurrency",
							   "error_message": "org.opentest4j.AssertionFailedError: Expected java.lang.IllegalArgumentException to be thrown, but nothing was thrown.",
							   "stack_trace": "...at PaymentProcessorTest.processTransaction_shouldFailWithDifferentCurrency(PaymentProcessorTest.java:XX)..."
							 }
						   ]
						   ```
					   *   `{{ COBERTURA_LINHA_JSON }}`:
						   ```json
						   {
							 "class_fqn": "com.example.payment.PaymentProcessor",
							 "lines_total": 20,
							 "lines_covered": 18,
							 "lines_missed": [
							   {"line": 15, "reason": "Condição de validação de moeda não coberta"}
							 ],
							 "coverage_percentage": 90.0
						   }
						   ```
					   *   `{{ ATTEMPT_NUMBER }}`: `5`

				   *   **Output JSON Esperado:**
					   ```json
					   {
						 "modified_test_methods": [
						   {
							 "method_name": "processTransaction_shouldFailWithDifferentCurrency",
							 "modified_code": "        // TESTE COMENTADO: Este cenário espera que o processamento de transação falhe com uma moeda diferente de BRL.\\n        // O erro ocorreu porque a implementação atual de PaymentProcessor não possui lógica para validar ou lançar exceção para moedas diferentes.\\n        // Não foi possível solucionar este erro pois a correção exigiria a adição de lógica de validação de moeda na classe alvo, o que é uma alteração de regra de negócio.\\n        // O usuário poderia considerar adicionar uma validação de moeda na classe PaymentProcessor ou remover este teste se o sistema não for projetado para lidar com múltiplas moedas.\\n        // A linha de código relevante (validação de moeda) não está coberta pela implementação atual.\\n        /*\\n        @Test @DisplayName(\\"Deve falhar ao processar transação com moeda diferente\\")\\n        void processTransaction_shouldFailWithDifferentCurrency() {\\n            Transaction transaction = new Transaction(BigDecimal.TEN, Currency.USD); \\n            assertThrows(IllegalArgumentException.class, () -> processor.process(transaction));\\n        }\\n        */"
						   }
						 ],
						 "required_new_imports": []\s
					   }
					   ```

				   **Exemplo Few-Shot (Teste Comentado por Comportamento Incompatível):**

				   *   **Input (Parâmetros Injetados):**
					   *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe `DataFormatter` que sempre retorna string em maiúsculas)
					   *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`: (Nenhum)
					   *   `{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`:
						   ```java
						   package com.example.util;
						   // ... imports
						   class DataFormatterTest {
							   // ... setup
							   @Test @DisplayName("Deve formatar string para minúsculas")
							   void formatString_shouldReturnLowercase() {
								   String input = "HELLO";
								   String expected = "hello";
								   String actual = formatter.formatString(input);
								   assertEquals(expected, actual);
							   }
						   }
						   ```
					   *   `{{ DETALHES_TESTES_FALHOS_JSON }}`:
						   ```json
						   [
							 {
							   "method_name": "formatString_shouldReturnLowercase",
							   "error_message": "org.opentest4j.AssertionFailedError: expected: <hello> but was: <HELLO>",
							   "stack_trace": "...at DataFormatterTest.formatString_shouldReturnLowercase(DataFormatterTest.java:XX)..."
							 }
						   ]
						   ```
					   *   `{{ COBERTURA_LINHA_JSON }}`:
						   ```json
						   {
							 "class_fqn": "com.example.util.DataFormatter",
							 "lines_total": 10,
							 "lines_covered": 10,
							 "lines_missed": [],
							 "coverage_percentage": 100.0
						   }
						   ```
					   *   `{{ ATTEMPT_NUMBER }}`: `2`

				   *   **Output JSON Esperado:**
					   ```json
					   {
						 "modified_test_methods": [
						   {
							 "method_name": "formatString_shouldReturnLowercase",
							 "modified_code": "        // TESTE COMENTADO: Este cenário espera que a string seja formatada para minúsculas,\\n        // mas a lógica atual em DataFormatter.formatString() sempre retorna a string em maiúsculas.\\n        // Não foi possível solucionar este erro pois o comportamento esperado do teste ('retornar minúsculas')\\n        // é fundamentalmente incompatível com a implementação atual da classe alvo ('sempre retornar maiúsculas').\\n        // O usuário poderia considerar ajustar a lógica de DataFormatter para suportar formatação para minúsculas,\\n        // ou remover/modificar este teste se o comportamento de sempre retornar maiúsculas for o desejado.\\n        // A cobertura de linha da classe alvo não é afetada, pois a linha de formatação já está coberta.\\n        /*\\n        @Test @DisplayName(\\"Deve formatar string para minúsculas\\")\\n        void formatString_shouldReturnLowercase() {\\n            String input = \\"HELLO\\";\\n            String expected = \\"hello\\";\\n            String actual = formatter.formatString(input);\\n            assertEquals(expected, actual);\\n        }\\n        */"
						   }
						 ],
						 "required_new_imports": []\s
					   }
					   ```

				   **Sua Tarefa:**

				   Agora, processe os detalhes dos testes falhos (`{{ DETALHES_TESTES_FALHOS_JSON }}`) para a classe (`%s`) e suas dependências (`%s`), usando o código de teste original (`{{ CODIGO_TESTE_ORIGINAL_COMPLETO }}`) e as informações de cobertura (`{{ COBERTURA_LINHA_JSON }}`) como referência. Realize a análise CoT para cada falha e gere a resposta JSON contendo os métodos corrigidos/comentados e novos imports.

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

				   **Código de Teste Original Completo:**

				   ```java
				   {{ CODIGO_TESTE_ORIGINAL_COMPLETO }}
				   %s
				   ```

				   **Detalhes dos Testes Falhos (JSON):**

				   ```json
				   {{ DETALHES_TESTES_FALHOS_JSON }}
				   %s
				   ```

				   **Informações de Cobertura de Linha (JSON):**

				   ```json
				   {{ COBERTURA_LINHA_JSON }}
				   %s
				   ```

				   **Número da Tentativa Atual:**

				   ```json
				   {{ ATTEMPT_NUMBER }}
				   %s
				   ```

				   **Resposta JSON:**

				   ```json
				   // Sua resposta JSON aqui
				   ```
				""",
            targetClassName,
            dependenciesList,
            attemptNumber,
            attemptNumber,
            targetClassName,
            dependenciesList,
            targetClassName,
            dependenciesList,
            context.getTargetClassCode(),
            dependencies,
            originalTestClassCode,
            failingTestsDetails,
            coverageDetails,
            attemptNumber);

    return new Prompt(prompt);
  }
}
