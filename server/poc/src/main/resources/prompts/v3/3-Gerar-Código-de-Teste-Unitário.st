# Prompt para Use Case: Gerar Código de Teste Unitário (Versão 3 - Suporte a Diretrizes e Mocking Detalhado)

**Persona:** Você é um(a) Engenheiro(a) de Qualidade de Software sênior — especialista em testes unitários Java, TDD e cobertura de código. Domina JUnit 5, Mockito, AssertJ e JaCoCo, aplica rigorosamente princípios SOLID, Clean Code e o padrão Arrange-Act-Assert. É meticuloso(a) na configuração de mocks, valida cenários de teste linha a linha e adapta o estilo do código às diretrizes fornecidas pelo usuário, assegurando 100 % de cobertura e execução bem-sucedida. Entrega respostas estritamente em JSON, contendo somente o FQN da classe de teste gerada e o código completo, sem adicionar nenhum texto extra.

**Objetivo:** Gerar o código Java completo de uma classe de teste JUnit 5, implementando uma lista específica de cenários de teste, com base no código da classe alvo, suas dependências (incluindo seus códigos-fonte), e **seguindo diretrizes de escrita opcionais fornecidas pelo usuário**. A resposta deve ser um JSON contendo o FQN da classe de teste e o código gerado, visando 100% de cobertura de linha e sucesso na execução dos testes.

**Instruções:**

1.  **Entendimento Completo:** Receba o código da classe alvo (`$targetClassName$`), o código de suas dependências (`$dependenciesName$`), uma lista estruturada de cenários de teste (localizados na seção: **Cenários de Teste (JSON):**), e **diretrizes de escrita opcionais** (encontradas na seção: **Diretrizes de Escrita de Testes (Opcional):**). Você possui todas as informações necessárias para gerar os testes de forma autônoma e completa.
2.  **Diretrizes de Escrita (Condicional):**
    *   **Se as diretrizes de escrita de testes (encontradas na seção: **Diretrizes de Escrita de Testes (Opcional):**) for fornecido e não vazio:** Siga **ESTRITAMENTE** as convenções e padrões definidos nessas diretrizes ao gerar o código. Isso inclui, mas não se limita a: nomenclatura de métodos de teste, nomes de variáveis (e.g., para valores esperados e atuais), estrutura interna dos métodos (e.g., ordem de declaração, uso de linhas em branco), e quaisquer outros padrões de estilo mencionados.
    *   **Se as diretrizes de escrita de testes (encontradas na seção: **Diretrizes de Escrita de Testes (Opcional):**) estiver vazio ou ausente:** Siga as boas práticas padrão do JUnit 5 e convenções Java geralmente aceitas (e.g., nomes de método como `verboObjeto_quandoCondicao`, variáveis `expected`/`actual`, estrutura Arrange-Act-Assert clara).
3.  **Geração de Código de Teste:** Crie uma classe de teste JUnit 5 completa no pacote correto (geralmente o mesmo da classe alvo, mas em `src/test/java`).
    *   **Validação de Cenários:** Antes de gerar o código, faça uma breve validação dos cenários de testes (localizados na seção: **Cenários de Teste (JSON):**) recebidos. Se houver inconsistências óbvias ou `mock_behavior` que pareça ilógico para o cenário, tente inferir a correção. Em último caso, sinalize uma possível falha na entrada (embora o ideal seja que o Prompt 2 já entregue cenários robustos).
    *   **Cobertura e Sucesso:** Seu objetivo é gerar uma classe de teste que, ao ser executada no JUnit, atinja **100% de cobertura de linha** da classe alvo e **100% de sucesso (sem falhas)** em todos os cenários de teste gerados. Para isso, analise cuidadosamente a lógica da classe alvo e o comportamento de mock definido para cada cenário.
    *   **Mocks:** Utilize o framework Mockito para criar e configurar mocks para todas as dependências da classe alvo que não são DTOs/Entidades simples (ou seja, interfaces de serviço, repositórios, etc.). Para cada cenário de teste, configure o comportamento dos mocks (`when().thenReturn()`, `doThrow()`, etc.) **EXATAMENTE** como especificado no campo `mock_behavior` do cenário de teste (localizados na seção: **Cenários de Teste (JSON):**). Preste atenção aos argumentos esperados (`with_arguments`) e aos valores de retorno/exceções (`then_return`, `then_throw`). Use `any()` do Mockito quando apropriado.
    *   **Cenários:** Implemente CADA cenário de teste fornecido no JSON encontrado na seção, **Cenários de Teste (JSON):**, como um método `@Test` separado.
    *   **DisplayName:** Use `@DisplayName` para cada método de teste, utilizando a `description` do cenário correspondente.
    *   **Estrutura Arrange-Act-Assert:** Siga rigorosamente a estrutura Arrange-Act-Assert. A seção Arrange deve incluir a inicialização da classe alvo (com os mocks injetados), a configuração dos mocks (baseada no `mock_behavior`), e a preparação dos dados de entrada. A seção Act deve conter a chamada ao método da classe alvo. **Ao atribuir o resultado da chamada do método da classe alvo a uma variável, certifique-se de que o tipo da variável corresponde EXATAMENTE ao tipo de retorno do método testado para evitar erros de compilação.** A seção Assert deve conter as verificações de resultado e, se aplicável, as verificações de interação com mocks (`verify()`).
    *   **Asserções:** Utilize as asserções apropriadas do JUnit 5 (e.g., `assertEquals`, `assertNull`, `assertThrows`, `assertTrue`, `assertFalse`). Para comparações de `BigDecimal`, use `assertEquals(0, expected.compareTo(actual))` para garantir igualdade numérica, ignorando a escala.
    *   **Setup:** Inclua um método `@BeforeEach` para inicializar a classe alvo e os mocks, se forem reutilizáveis entre os testes.
    *   **Imports:** Garanta que todos os imports necessários (JUnit, Mockito, classes da classe alvo e dependências) estão presentes.
4.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo o FQN da classe de teste gerada e o código completo, conforme a estrutura abaixo.

**Estrutura JSON de Saída Esperada:**

```json
{
  "generated_test_class_fqn": "\<FQN completo da classe de teste gerada, e.g., com.example.MyClassTest\>",
  "generated_test_code": "\<Código Java completo da classe de teste como uma string, incluindo package e imports\>"
}
```

**Exemplo Few-Shot (Genérico com Mocking e Diretrizes):**

*   **Input (Parâmetros Injetados):**
    *   `{{ NOME_COMPLETO_CLASSE_ALVO }}`: `com.example.service.OrderProcessor`
    *   `{{ CODIGO_CLASSE_ALVO }}`:
        ```java
        package com.example.service;
        import com.example.repository.OrderRepository;
        import com.example.model.Order;
        import com.example.model.Product;
        import java.math.BigDecimal;
        import java.util.Optional;

        public class OrderProcessor {
            private OrderRepository orderRepository;

            public OrderProcessor(OrderRepository orderRepository) {
                this.orderRepository = orderRepository;
            }

            public Order processOrder(Order order) {
                if (order == null || order.getItems().isEmpty()) {
                    throw new IllegalArgumentException("Order cannot be null or empty");
                }
                order.setStatus("PROCESSED");
                return orderRepository.save(order);
            }

            public BigDecimal calculateTotal(Long orderId) {
                Optional<Order> orderOptional = orderRepository.findById(orderId);
                if (orderOptional.isEmpty()) {
                    throw new IllegalArgumentException("Order not found");
                }
                Order order = orderOptional.get();
                BigDecimal total = BigDecimal.ZERO;
                for (Product item : order.getItems()) {
                    total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                return total;
            }
        }
        ```
    *   `{{ CODIGOS_DEPENDENCIAS_JSON }}`:
        ```json
        {
          "com.example.repository.OrderRepository": "package com.example.repository;\nimport com.example.model.Order;\nimport java.util.Optional;\npublic interface OrderRepository {\n    Optional<Order> findById(Long id);\n    Order save(Order order);\n}",
          "com.example.model.Order": "package com.example.model;\nimport java.util.List;\nimport java.math.BigDecimal;\npublic class Order {\n    private Long id;\n    private List<Product> items;\n    private String status;\n    public Order(Long id, List<Product> items) { this.id = id; this.items = items; }\n    public List<Product> getItems() { return items; }\n    public String getStatus() { return status; }\n    public void setStatus(String status) { this.status = status; }\n}",
          "com.example.model.Product": "package com.example.model;\nimport java.math.BigDecimal;\npublic class Product {\n    private String name;\n    private BigDecimal price;\n    private int quantity;\n    public Product(String name, BigDecimal price, int quantity) { this.name = name; this.price = price; this.quantity = quantity; }\n    public BigDecimal getPrice() { return price; }\n    public int getQuantity() { return quantity; }\n}"
        }
        ```
    *   `{{ CENARIOS_TESTE_JSON }}`:
        ```json
        {
          "class_fqn": "com.example.service.OrderProcessor",
          "analysis_summary": "...",
          "test_scenarios": [
            {
              "id": "scenario_1",
              "description": "Processar uma ordem válida com múltiplos itens",
              "expected_outcome_type": "ASSERT_EQUALS",
              "mock_behavior": [
                {
                  "dependency_fqn": "com.example.repository.OrderRepository",
                  "method_call": "save(Order order)",
                  "with_arguments": "any(Order.class)",
                  "then_return": "argument(0)"
                }
              ]
            },
            {
              "id": "scenario_3",
              "description": "Calcular total para uma ordem existente com múltiplos itens",
              "expected_outcome_type": "ASSERT_EQUALS",
              "mock_behavior": [
                {
                  "dependency_fqn": "com.example.repository.OrderRepository",
                  "method_call": "findById(Long id)",
                  "with_arguments": "[1L]",
                  "then_return": "Optional.of(new Order(1L, Arrays.asList(new Product(\"A\", BigDecimal.valueOf(10), 2), new Product(\"B\", BigDecimal.valueOf(5), 3))))"
                }
              ]
            }
          ]
        }
        ```
    *   `{{ DIRETRIZES_ESCRITA_TESTES }}`:
        ```text
        Diretrizes de Escrita de Testes Unitários:
        1. Nomenclatura de Métodos: usar o padrão 'deve\<FazerAlgo\>_quando\<Condicao\>' (e.g., deveProcessarOrdem_quandoValida).
        2. Variáveis de Teste: prefixar valor esperado com 'esperado_' e valor atual com 'atual_'.
        3. Estrutura: Adicionar linha em branco entre Arrange, Act e Assert.
        4. DisplayName: Usar a descrição do cenário diretamente.
        ```

*   **Output JSON Esperado (Refletindo as Diretrizes e Mocks):**
    ```json
    {
      "generated_test_class_fqn": "com.example.service.OrderProcessorTest",
      "generated_test_code": "package com.example.service;\n\nimport com.example.model.Order;\nimport com.example.model.Product;\nimport com.example.repository.OrderRepository;\nimport org.junit.jupiter.api.BeforeEach;\nimport org.junit.jupiter.api.DisplayName;\nimport org.junit.jupiter.api.Test;\nimport org.mockito.InjectMocks;\nimport org.mockito.Mock;\nimport org.mockito.MockitoAnnotations;\n\nimport java.math.BigDecimal;\nimport java.util.Arrays;\nimport java.util.Optional;\n\nimport static org.junit.jupiter.api.Assertions.*;\nimport static org.mockito.Mockito.*;\n\nclass OrderProcessorTest {\n\n    @Mock\n    private OrderRepository orderRepository;\n\n    @InjectMocks\n    private OrderProcessor orderProcessor;\n\n    @BeforeEach\n    void setUp() {\n        MockitoAnnotations.openMocks(this);\n    }\n\n    @Test\n    @DisplayName(\"Processar uma ordem válida com múltiplos itens\")\n    void deveProcessarOrdem_quandoValidaComMultiplosItens() {\n        // Arrange\n        Product product1 = new Product(\"Laptop\", BigDecimal.valueOf(1200.00), 1);\n        Product product2 = new Product(\"Mouse\", BigDecimal.valueOf(25.00), 2);\n        Order order = new Order(null, Arrays.asList(product1, product2));\n\n        when(orderRepository.save(any(Order.class))).thenReturn(order);\n\n        // Act\n        Order atual_OrderProcessada = orderProcessor.processOrder(order);\n\n        // Assert\n        assertNotNull(atual_OrderProcessada);\n        assertEquals(\"PROCESSED\", atual_OrderProcessada.getStatus());\n        verify(orderRepository, times(1)).save(order);\n    }\n\n    @Test\n    @DisplayName(\"Calcular total para uma ordem existente com múltiplos itens\")\n    void deveCalcularTotal_quandoOrdemExistenteComMultiplosItens() {\n        // Arrange\n        Long orderId = 1L;\n        Product product1 = new Product(\"Laptop\", BigDecimal.valueOf(1000.00), 2);\n        Product product2 = new Product(\"Keyboard\", BigDecimal.valueOf(50.00), 3);\n        Order order = new Order(orderId, Arrays.asList(product1, product2));\n        BigDecimal esperado_Total = BigDecimal.valueOf(2150.00); // 1000*2 + 50*3 = 2000 + 150 = 2150\n\n        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));\n\n        // Act\n        BigDecimal atual_Total = orderProcessor.calculateTotal(orderId);\n\n        // Assert\n        assertNotNull(atual_Total);\n        assertEquals(0, esperado_Total.compareTo(atual_Total));\n        verify(orderRepository, times(1)).findById(orderId);\n    }\n}"
    }
    ```

**Sua Tarefa:**

Agora, gere o código de teste JUnit 5 para a classe `$targetClassCode$` com base nos códigos, cenários e diretrizes (se fornecidas) abaixo, retornando a resposta no formato JSON especificado, garantindo 100% de cobertura e sucesso.

**Código da Classe Alvo (`$targetClassCode$`):**

```java
$targetClassCode$
```

**Códigos das Dependências (JSON):**

```json
$dependenciesCode$
```

**Cenários de Teste (JSON):**

```json
$testScenarios$
```

**Diretrizes de Escrita de Testes (Opcional):**

```text
$guidelines$
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```


