package br.com.unicat.poc.v1.prompts;

import br.com.unicat.poc.v1.controller.context.RequestContext;
import br.com.unicat.poc.v1.controller.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class AnalyseCodeAndIdentifyDependenciesPromptGenerator {

  public Prompt get() {
    RequestContext context = RequestContextHolder.getContext();
    String targetClassName = context.getTargetClassPackage() + "." + context.getTargetClassName();

    String prompt =
        String.format(
            """
              # Prompt para Use Case: Analisar Código e Identificar Dependências (Versão 2 - Saída JSON & Few-Shot)

              Persona: Você é um(a) Engenheiro(a) de Software sênior — especialista em arquitetura Java, análise estática de código e engenharia de testes — que atua como revisor(a) meticuloso(a). Domina princípios SOLID, DDD e Clean Code, identifica dependências customizadas com precisão cirúrgica, valida referências linha a linha e entrega relatórios estruturados em JSON sem adicionar nenhum texto extra.

              **Objetivo:** Analisar uma classe Java fornecida, descrever seu propósito e método principal, identificar com **precisão** suas dependências customizadas (DTOs, Entidades, Enums, Interfaces de Serviço, etc.) **que realmente impactam a lógica** e retornar a análise em formato JSON estruturado, incluindo o código‑fonte completo de cada dependência listada.

              ---

              **Instruções:**

              1.  Analise CUIDADOSAMENTE o código Java da classe `%s` fornecido abaixo.
              2.  Identifique o propósito geral da classe e seu método público principal (aquele que encapsula a lógica central do use case).
              3.  Determine os tipos de dados de entrada e saída desse método principal.
              4.  Liste os nomes totalmente qualificados (FQNs) de TODAS as classes/enums customizadas (não pertencentes ao JDK ou frameworks comuns como Spring - ignore anotações) que são diretamente referenciadas no código.
              5.  Formate sua resposta EXCLUSIVAMENTE como um objeto JSON válido, seguindo a estrutura definida no exemplo abaixo.
                  *   Nunca acrescente qualquer outra informação no corpo de resposta além da estrutura JSON de saída esperada informada abaixo! O sistema que recebe a sua resposta espera somente um objeto JSON e se você adicionar qualquer texto antes ou depois, irá quebrar a aplicação e gerar um bug. Não faça isso. Retorne exatamente o que lhe foi pedido.

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

              Agora, aplique esta análise à classe `%s` com o código fornecido abaixo e gere a resposta JSON correspondente.

              **Código da Classe Alvo (`%s`):**

              ```java
              %s
              ```

              **Resposta JSON:**

              ```json
              // Sua resposta JSON aqui
              ```
              """,
            targetClassName, targetClassName, targetClassName, context.getTargetClassCode());

    return new Prompt(prompt);
  }
}
