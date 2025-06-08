# Prompt para Use Case: Analisar Lógica e Identificar Cenários de Teste (Versão 3 - Saída JSON, CoT & Few-Shot Complexo com Mocking)

**Objetivo:** Realizar uma análise profunda e passo a passo (Chain-of-Thought) da lógica de uma classe Java e suas dependências, identificar fluxos principais, casos de borda, potenciais erros, e propor uma lista abrangente de cenários de teste descritivos, **incluindo o comportamento esperado das dependências mockadas para cada cenário**, retornando a análise e os cenários em formato JSON estruturado.

**Instruções:**

1.  **Análise Chain-of-Thought (CoT):** Antes de gerar o JSON final, realize internamente um raciocínio passo a passo detalhado sobre a lógica do método público principal da classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` (código abaixo), considerando também o código das dependências (`{{ CODIGOS_DEPENDENCIAS_JSON }}`). Seu CoT deve cobrir:
    *   **Fluxo Principal:** Descreva os passos lógicos centrais da execução.
    *   **Estruturas de Controle:** Analise loops (`forEach`, `for`, `while`) e condicionais (`if`, `else`, `switch`, `try-catch`).
    *   **Entradas e Estados:** Considere o impacto de entradas nulas, listas/coleções vazias, objetos com campos nulos ou vazios, valores numéricos (zero, negativos, positivos, limites BigDecimal).
    *   **Casos de Borda:** Identifique limites (0, 1, N itens), valores mínimos/máximos, empates, descontos que zeram o preço, descontos que ultrapassam o preço.
    *   **Tratamento de Erros:** Avalie blocos `try-catch` existentes. Antecipe exceções não tratadas (e.g., `NullPointerException`, `IllegalArgumentException` para valores inválidos).
    *   **Interações com Dependências:** Detalhe como a classe alvo interage com suas dependências (métodos chamados, parâmetros passados, valores de retorno esperados). Isso é CRÍTICO para o mocking.
    *   **Robustez:** Avalie criticamente se a lógica é robusta contra os casos de borda e erros identificados.
2.  **Geração de Cenários com Comportamento de Mock:** Com base na sua análise CoT, derive uma lista COMPLETA de cenários de teste descritivos que cubram todos os fluxos, casos de borda e tratamentos de erro relevantes. Para cada cenário:
    *   Indique brevemente o tipo de verificação principal (e.g., igualdade, nulidade, exceção).
    *   **MUITO IMPORTANTE:** Descreva o comportamento EXATO que as dependências mockadas devem ter para que este cenário seja testado corretamente. Isso inclui chamadas de método esperadas, parâmetros de entrada para essas chamadas e valores de retorno ou exceções que os mocks devem lançar. Seja o mais específico possível.
3.  **Formato JSON:** Estruture sua resposta FINAL exclusivamente como um objeto JSON válido, conforme o esquema abaixo. Inclua um resumo da sua análise CoT e a lista de cenários.

**Estrutura JSON de Saída Esperada:**

```json
{
  "class_fqn": "{{ NOME_COMPLETO_CLASSE_ALVO }}",
  "analysis_summary": "<Resumo conciso da análise CoT, destacando pontos críticos, casos de borda, interações com dependências e potenciais problemas identificados>",
  "test_scenarios": [
    {
      "id": "scenario_1",
      "description": "<Descrição clara e concisa do cenário de teste 1>",
      "expected_outcome_type": "<Tipo de asserção principal, e.g., ASSERT_EQUALS, ASSERT_NOT_NULL, ASSERT_NULL, ASSERT_THROWS_IllegalArgumentException, ASSERT_TRUE>",
      "mock_behavior": [
        {
          "dependency_fqn": "<FQN da dependência a ser mockada, e.g., com.example.repository.ProductRepository>",
          "method_call": "<Assinatura do método a ser mockado, e.g., findById(Long id)>",
          "with_arguments": "<Argumentos esperados para a chamada do método, em formato JSON ou string descritiva, e.g., [1L] ou 'any(Long.class)'>",
          "then_return": "<Valor de retorno esperado, em formato JSON ou string descritiva, e.g., {'id': 1, 'name': 'Product A'} ou 'new Product(1L, "Product A")'>",
          "then_throw": "<Exceção a ser lançada, e.g., new RuntimeException(\"Database error\")>",
          "times": "<Número de vezes que o método deve ser chamado, e.g., 1>"
        }
        // ... mais comportamentos de mock para este cenário
      ]
    },
    // ... mais cenários
  ]
}
```

**Exemplo Few-Shot (Genérico com Mocking):**

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
                // Simulate saving order
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
          "com.example.model.Order": "package com.example.model;\nimport java.util.List;\nimport java.math.BigDecimal;\npublic class Order {\n    private Long id;\n    private List<Product> items;\n    private String status;\n    // getters, setters, constructor\n    public Order(Long id, List<Product> items) { this.id = id; this.items = items; }\n    public List<Product> getItems() { return items; }\n    public String getStatus() { return status; }\n    public void setStatus(String status) { this.status = status; }\n}",
          "com.example.model.Product": "package com.example.model;\nimport java.math.BigDecimal;\npublic class Product {\n    private String name;\n    private BigDecimal price;\n    private int quantity;\n    // getters, setters, constructor\n    public Product(String name, BigDecimal price, int quantity) { this.name = name; this.price = price; this.quantity = quantity; }\n    public BigDecimal getPrice() { return price; }\n    public int getQuantity() { return quantity; }\n}"
        }
        ```

*   **Output JSON Esperado:**
    ```json
    {
      "class_fqn": "com.example.service.OrderProcessor",
      "analysis_summary": "A classe OrderProcessor processa pedidos e calcula o total. O método processOrder valida a ordem e a salva via OrderRepository. O método calculateTotal busca a ordem por ID e soma os totais dos itens. Lança IllegalArgumentException para ordens nulas/vazias ou não encontradas. Interage com OrderRepository para persistência e busca.",
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
              "then_return": "argument(0)" // Retorna o próprio objeto Order passado como argumento
            }
          ]
        },
        {
          "id": "scenario_2",
          "description": "Tentar processar uma ordem nula",
          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException",
          "mock_behavior": []
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
        },
        {
          "id": "scenario_4",
          "description": "Calcular total para uma ordem não encontrada",
          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException",
          "mock_behavior": [
            {
              "dependency_fqn": "com.example.repository.OrderRepository",
              "method_call": "findById(Long id)",
              "with_arguments": "any(Long.class)",
              "then_return": "Optional.empty()"
            }
          ]
        }
      ]
    }
    ```

**Sua Tarefa:**

Agora, aplique esta análise CoT e geração de cenários à classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` com os códigos fornecidos abaixo e gere a resposta JSON correspondente.

**Código da Classe Alvo (`{{ NOME_COMPLETO_CLASSE_ALVO }}`):**

```java
{{ CODIGO_CLASSE_ALVO }}
```

**Códigos das Dependências (JSON):**

```json
{{ CODIGOS_DEPENDENCIAS_JSON }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```


