package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class GenerateUnitTestsPromptGenerator {

    public Prompt get(
            final String dependenciesList,
            final String dependencies,
            final String testScenarios,
            final String guidelines
    ) {
        RequestContext context = RequestContextHolder.getContext();
        final var targetClassName = context.getTargetClassPackage() + "." + context.getTargetClassName();

        final var prompt = String.format("""
                # Prompt para Use Case: Gerar Código de Teste Unitário (Versão 2.1 - Suporte a Diretrizes)
                
                **Objetivo:** Gerar o código Java completo de uma classe de teste JUnit 5, implementando uma lista específica de cenários de teste, com base no código da classe alvo, suas dependências, e **seguindo diretrizes de escrita opcionais fornecidas pelo usuário**. A resposta deve ser um JSON contendo o FQN da classe de teste e o código gerado.
                
                **Instruções:**
                
                1.  **Entendimento:** Receba o código da classe alvo (`%s`), o código de suas dependências (`%s`), uma lista estruturada de cenários de teste (localizados na seção: **Cenários de Teste (JSON):**), e **diretrizes de escrita opcionais** (localizadas na seção: **Diretrizes de Escrita de Testes (Opcional):**).
                2.  **Diretrizes de Escrita (Condicional):**
                    *   **Se `{{ DIRETRIZES_ESCRITA_TESTES }}` (localizadas na seção: **Diretrizes de Escrita de Testes (Opcional):**) for fornecido e não vazio:** Siga **ESTRITAMENTE** as convenções e padrões definidos nessas diretrizes ao gerar o código. Isso inclui, mas não se limita a: nomenclatura de métodos de teste, nomes de variáveis (e.g., para valores esperados e atuais), estrutura interna dos métodos (e.g., ordem de declaração, uso de linhas em branco), e quaisquer outros padrões de estilo mencionados.
                    *   **Se `{{ DIRETRIZES_ESCRITA_TESTES }}` (encontradas na seção: **Diretrizes de Escrita de Testes (Opcional):**) estiver vazio ou ausente:** Siga as boas práticas padrão do JUnit 5 e convenções Java geralmente aceitas (e.g., nomes de método como `verboObjeto_quandoCondicao`, variáveis `expected`/`actual`, estrutura Arrange-Act-Assert clara).
                3.  **Geração de Código:** Crie uma classe de teste JUnit 5 completa no pacote correto (geralmente o mesmo da classe alvo, mas em `src/test/java`).
                    *   Implemente CADA cenário de teste fornecido no JSON encontrado na seção, **Cenários de Teste (JSON):**, como um método `@Test` separado.
                    *   Use `@DisplayName` para cada método de teste, utilizando a `description` do cenário correspondente.
                    *   Aplique as **diretrizes de escrita** (se fornecidas) ou os padrões default à nomenclatura e estrutura do método.
                    *   Siga rigorosamente a estrutura Arrange-Act-Assert, aplicando as diretrizes de escrita à nomeação de variáveis e organização do código dentro desta estrutura.
                    *   Utilize as asserções apropriadas do JUnit 5 (e.g., `assertEquals`, `assertNull`, `assertThrows`) conforme sugerido pelo `expected_outcome_type` do cenário, mas adapte conforme necessário para a verificação completa. Use `BigDecimal.valueOf()` para criar valores e `assertEquals(0, expected.compareTo(actual))` para comparar BigDecimals.
                    *   Inclua um método `@BeforeEach` para inicializar a classe alvo (`%s`) se for reutilizável entre os testes.
                    *   Garanta que todos os imports necessários estão presentes.
                4.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo o FQN da classe de teste gerada e o código completo, conforme a estrutura abaixo.
                
                **Estrutura JSON de Saída Esperada:**
                
                ```json
                {
                  '''generated_test_class_fqn''': '''<FQN completo da classe de teste gerada, e.g., com.example.MyClassTest>''',
                  '''generated_test_code''': '''<Código Java completo da classe de teste como uma string, incluindo package e imports>'''
                }
                ```
                
                **Exemplo Few-Shot (Genérico com Diretrizes):**
                
                *   **Input (Parâmetros Injetados):**
                    *   `{{ NOME_COMPLETO_CLASSE_ALVO }}`: `com.example.service.SimpleDiscountCalculator`
                    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe SimpleDiscountCalculator)
                    *   `{{ CODIGO_DEPENDENCIAS }}`: (Códigos de ProductDTO e DiscountType)
                    *   `{{ CENARIOS_TESTE_JSON }}`:
                        ```json
                        {
                          '''class_fqn''': '''com.example.service.SimpleDiscountCalculator''',
                          '''analysis_summary''': '''...''',
                          '''test_scenarios''': [
                            {'''id''': '''scenario_1''', '''description''': '''Testar desconto percentual válido abaixo do cap (e.g., 10%%)''', '''expected_outcome_type''': '''ASSERT_EQUALS'''},
                            {'''id''': '''scenario_9''', '''description''': '''Testar com produto nulo''', '''expected_outcome_type''': '''ASSERT_THROWS_IllegalArgumentException'''}
                            // ... (outros cenários omitidos)
                          ]
                        }
                        ```
                    *   `{{ DIRETRIZES_ESCRITA_TESTES }}`:
                        ```text
                        Diretrizes de Escrita de Testes Unitários:
                        1. Nomenclatura de Métodos: usar o padrão '''deve<FazerAlgo>_quando<Condicao>''' (e.g., deveAplicarDescontoPercentual_quandoValorValidoAbaixoCap).
                        2. Variáveis de Teste: prefixar valor esperado com '''esperado_''' e valor atual com '''atual_'''.
                        3. Estrutura: Adicionar linha em branco entre Arrange, Act e Assert.
                        4. DisplayName: Usar a descrição do cenário diretamente.
                        ```
                
                *   **Output JSON Esperado (Refletindo as Diretrizes):**
                    ```json
                    {
                      '''generated_test_class_fqn''': '''com.example.service.SimpleDiscountCalculatorTest''',
                      '''generated_test_code''': '''package com.example.service;\\n\\nimport com.example.dto.ProductDTO;\\nimport com.example.enums.DiscountType;\\nimport org.junit.jupiter.api.BeforeEach;\\nimport org.junit.jupiter.api.DisplayName;\\nimport org.junit.jupiter.api.Test;\\nimport java.math.BigDecimal;\\n\\nimport static org.junit.jupiter.api.Assertions.*;\\n\\nclass SimpleDiscountCalculatorTest {\\n\\n    private SimpleDiscountCalculator calculator;\\n\\n    @BeforeEach\\n    void setUp() {\\n        calculator = new SimpleDiscountCalculator();\\n    }\\n\\n    private ProductDTO createProduct(double price) {\\n        return new ProductDTO(BigDecimal.valueOf(price)); \\n    }\\n\\n    @Test\\n    @DisplayName(\\'Testar desconto percentual válido abaixo do cap (e.g., 10%%)\\')\\n    void deveAplicarDescontoPercentual_quandoValorValidoAbaixoCap() {\\n        // Arrange\\n        ProductDTO product = createProduct(100.00);\\n        BigDecimal esperado_Preco = BigDecimal.valueOf(90.00);\\n        \\n        // Act\\n        BigDecimal atual_Preco = calculator.calculateDiscountedPrice(product, DiscountType.PERCENTAGE, BigDecimal.valueOf(10));\\n        \\n        // Assert\\n        assertNotNull(atual_Preco);\\n        assertEquals(0, esperado_Preco.compareTo(atual_Preco));\\n    }\\n\\n    @Test\\n    @DisplayName(\\'Testar com produto nulo\\')\\n    void deveLancarExcecao_quandoProdutoNulo() {\\n        // Arrange\\n        // (Nenhum arrange específico necessário aqui)\\n        \\n        // Act & Assert\\n        assertThrows(IllegalArgumentException.class, () -> {\\n            calculator.calculateDiscountedPrice(null, DiscountType.PERCENTAGE, BigDecimal.TEN);\\n        });\\n    }\\n    // ... (Implementação dos outros métodos de teste seguindo as diretrizes) ...\\n}'''
                    }
                    ```
                
                **Sua Tarefa:**
                
                Agora, gere o código de teste JUnit 5 para a classe `%s` com base nos códigos, cenários e diretrizes (se fornecidas) abaixo, retornando a resposta no formato JSON especificado.
                
                **Código da Classe Alvo (`%s`):**
                
                ```java
                {{ CODIGO_CLASSE_ALVO }}
                %s
                ```
                
                **Código das Dependências:**
                
                ```java
                {{ CODIGO_DEPENDENCIAS }}
                %s
                ```
                
                **Cenários de Teste (JSON):**
                
                ```json
                {{ CENARIOS_TESTE_JSON }}
                %s
                ```
                
                **Diretrizes de Escrita de Testes (Opcional):**
                
                ```text
                {{ DIRETRIZES_ESCRITA_TESTES }}
                %s
                ```
                
                **Resposta JSON:**
                
                ```json
                // Sua resposta JSON aqui
                ```
                """, targetClassName, dependenciesList, targetClassName, targetClassName, targetClassName, context.getTargetClassCode(), dependencies, testScenarios, guidelines);



        return new Prompt(prompt);
    }
}
