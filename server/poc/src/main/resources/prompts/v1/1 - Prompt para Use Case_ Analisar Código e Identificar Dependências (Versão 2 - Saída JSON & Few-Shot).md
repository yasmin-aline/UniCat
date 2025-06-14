# Prompt para Use Case: Analisar Código e Identificar Dependências (Versão 2 - Saída JSON & Few-Shot)

**Objetivo:** Analisar uma classe Java fornecida, descrever seu propósito e método principal, e identificar suas dependências customizadas (DTOs, Entidades, Enums, etc.), retornando a análise em formato JSON estruturado.

**Instruções:**

1.  Analise CUIDADOSAMENTE o código Java da classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` fornecido abaixo.
2.  Identifique o propósito geral da classe e seu método público principal (aquele que encapsula a lógica central do use case).
3.  Determine os tipos de dados de entrada e saída desse método principal.
4.  Liste os nomes totalmente qualificados (FQNs) de TODAS as classes/enums customizadas (não pertencentes ao JDK ou frameworks comuns como Spring - ignore anotações) que são diretamente referenciadas no código.
5.  Formate sua resposta EXCLUSIVAMENTE como um objeto JSON válido, seguindo a estrutura definida no exemplo abaixo.

**Estrutura JSON de Saída Esperada:**

```json
{
  "analysis": {
    "class_fqn": "<FQN da classe analisada>",
    "purpose_summary": "<Breve descrição do propósito da classe e método principal>",
    "main_method_signature": "<Assinatura completa do método público principal>",
    "input_type": "<FQN ou descrição do tipo de entrada principal>",
    "output_type": "<FQN ou descrição do tipo de saída principal>"
  },
  "custom_dependencies": [
    "<FQN da Dependência Customizada 1>",
    "<FQN da Dependência Customizada 2>",
    // ... outras dependências
  ]
}
```

**Exemplo Few-Shot (Genérico):**

*   **Input (Parâmetros Injetados):**
    *   `{{ NOME_COMPLETO_CLASSE_ALVO }}`: `com.example.service.SimpleDiscountCalculator`
    *   `{{ CODIGO_CLASSE_ALVO }}`:
        ```java
        package com.example.service;
        import com.example.dto.ProductDTO;
        import com.example.enums.DiscountType;
        import java.math.BigDecimal;
        // ... (imports restantes e corpo da classe SimpleDiscountCalculator)
        public class SimpleDiscountCalculator {
            public BigDecimal calculateDiscountedPrice(ProductDTO product, DiscountType type, BigDecimal value) {
                // ... (lógica)
            }
        }
        ```

*   **Output JSON Esperado:**
    ```json
    {
      "analysis": {
        "class_fqn": "com.example.service.SimpleDiscountCalculator",
        "purpose_summary": "Calcula o preço com desconto de um produto com base no tipo e valor do desconto, aplicando validações e limites.",
        "main_method_signature": "public BigDecimal calculateDiscountedPrice(ProductDTO product, DiscountType type, BigDecimal value)",
        "input_type": "com.example.dto.ProductDTO, com.example.enums.DiscountType, java.math.BigDecimal",
        "output_type": "java.math.BigDecimal"
      },
      "custom_dependencies": [
        "com.example.dto.ProductDTO",
        "com.example.enums.DiscountType"
      ]
    }
    ```

**Sua Tarefa:**

Agora, aplique esta análise à classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` com o código fornecido abaixo e gere a resposta JSON correspondente.

**Código da Classe Alvo (`{{ NOME_COMPLETO_CLASSE_ALVO }}`):**

```java
{{ CODIGO_CLASSE_ALVO }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```

