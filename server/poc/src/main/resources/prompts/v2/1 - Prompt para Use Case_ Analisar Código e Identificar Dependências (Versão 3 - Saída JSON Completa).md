# Prompt para Use Case: Analisar Código e Identificar Dependências (Versão 3 - Saída JSON Completa)

**Objetivo:** Analisar uma classe Java fornecida, descrever seu propósito e método principal, identificar suas dependências customizadas (DTOs, Entidades, Enums, Interfaces de Serviço, etc.), e retornar a análise em formato JSON estruturado, **incluindo o código-fonte completo de cada dependência identificada**.

**Instruções:**

1.  **Análise Detalhada:** Analise CUIDADOSAMENTE o código Java da classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` fornecido abaixo. Entenda seu propósito, o fluxo de execução do método público principal e como ele interage com outras classes.
2.  **Identificação de Dependências Customizadas:** Liste os nomes totalmente qualificados (FQNs) de TODAS as classes/interfaces/enums customizadas (não pertencentes ao JDK ou frameworks comuns como Spring, Jakarta EE, etc. - ignore anotações) que são diretamente referenciadas no código da classe alvo. Isso inclui DTOs, Entidades, Interfaces de Serviço, Repositórios, etc. **A responsabilidade de identificar corretamente quais dependências são necessárias para mockar para cada cenário de teste é totalmente sua.** Considere DTOs ou modelos que são parte de estruturas de entrada/saída complexas ou que contêm objetos/coleções aninhadas como potenciais mocks. Erre pelo excesso, listando todas as dependências externas não-primitivas/wrappers, mesmo que sejam DTOs. A exclusão de 'DTOs/Modelos simples' deve ser aplicada com cautela, apenas para objetos que não influenciam o comportamento de mocks ou que são puramente dados sem lógica complexa.
3.  **Extração de Código das Dependências:** Para cada dependência customizada identificada no passo 2, você DEVE fornecer o código-fonte completo. Se o código de uma dependência não for fornecido nos parâmetros de entrada, você deve indicar isso no JSON.
4.  **Formato JSON de Saída:** Formate sua resposta EXCLUSIVAMENTE como um objeto JSON válido, seguindo a estrutura definida no exemplo abaixo.

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
    {
      "fqn": "<FQN da Dependência Customizada 1>",
      "code": "<Código Java completo da Dependência Customizada 1 como uma string, incluindo package e imports>"
    },
    {
      "fqn": "<FQN da Dependência Customizada 2>",
      "code": "<Código Java completo da Dependência Customizada 2 como uma string, incluindo package e imports>"
    }
    // ... outras dependências com seus códigos
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
        import com.example.repository.ProductRepository;
        import java.math.BigDecimal;
        
        public class SimpleDiscountCalculator {
            private ProductRepository productRepository;

            public SimpleDiscountCalculator(ProductRepository productRepository) {
                this.productRepository = productRepository;
            }

            public BigDecimal calculateDiscountedPrice(ProductDTO product, DiscountType type, BigDecimal value) {
                // ... (lógica que pode usar productRepository.findById())
                return BigDecimal.ZERO;
            }
        }
        ```
    *   `{{ CODIGO_DEPENDENCIAS_FORNECIDAS }}`: (Este é um novo parâmetro de entrada que o usuário deve fornecer, contendo um JSON com os códigos das dependências que ele já possui. Ex: `{"com.example.dto.ProductDTO": "package com.example.dto;\npublic class ProductDTO {\n    public BigDecimal price;\n}", "com.example.enums.DiscountType": "package com.example.enums;\npublic enum DiscountType { PERCENTAGE, FIXED_AMOUNT }"}`)

*   **Output JSON Esperado:**
    ```json
    {
      "analysis": {
        "class_fqn": "com.example.service.SimpleDiscountCalculator",
        "purpose_summary": "Calcula o preço com desconto de um produto, interagindo com um repositório para buscar dados adicionais.",
        "main_method_signature": "public BigDecimal calculateDiscountedPrice(ProductDTO product, DiscountType type, BigDecimal value)",
        "input_type": "com.example.dto.ProductDTO, com.example.enums.DiscountType, java.math.BigDecimal",
        "output_type": "java.math.BigDecimal"
      },
      "custom_dependencies": [
        {
          "fqn": "com.example.dto.ProductDTO",
          "code": "package com.example.dto;\npublic class ProductDTO {\n    public BigDecimal price;\n}"
        },
        {
          "fqn": "com.example.enums.DiscountType",
          "code": "package com.example.enums;\npublic enum DiscountType { PERCENTAGE, FIXED_AMOUNT }"
        },
        {
          "fqn": "com.example.repository.ProductRepository",
          "code": "// Código não fornecido para com.example.repository.ProductRepository"
        }
      ]
    }
    ```

**Sua Tarefa:**

Agora, aplique esta análise à classe `{{ NOME_COMPLETO_CLASSE_ALVO }}` com o código fornecido abaixo e gere a resposta JSON correspondente. Considere também o `{{ CODIGO_DEPENDENCIAS_FORNECIDAS }}` para incluir os códigos das dependências.

**Código da Classe Alvo (`{{ NOME_COMPLETO_CLASSE_ALVO }}`):**

```java
{{ CODIGO_CLASSE_ALVO }}
```

**Códigos das Dependências Fornecidas (JSON):**

```json
{{ CODIGO_DEPENDENCIAS_FORNECIDAS }}
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```


