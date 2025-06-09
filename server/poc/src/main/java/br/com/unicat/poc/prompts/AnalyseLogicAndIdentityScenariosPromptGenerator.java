package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class AnalyseLogicAndIdentityScenariosPromptGenerator {

  public Prompt get(final String dependencies, final String dependenciesName) {
    RequestContext context = RequestContextHolder.getContext();
    final String targetClassName =
        context.getTargetClassPackage() + "." + context.getTargetClassName();

    String prompt =
        String.format(
            """
				# Prompt para Use Case: Analisar Lógica e Identificar Cenários de Teste (Versão 2 - Saída JSON, CoT & Few-Shot)

				**Objetivo:** Realizar uma análise profunda e passo a passo (Chain-of-Thought) da lógica de uma classe Java e suas dependências, identificar fluxos principais, casos de borda, potenciais erros, e propor uma lista abrangente de cenários de teste descritivos, retornando a análise e os cenários em formato JSON estruturado.

				**Instruções:**

				1.  **Análise Chain-of-Thought (CoT):** Antes de gerar o JSON final, realize internamente um raciocínio passo a passo detalhado sobre a lógica do método público principal da classe `%s` (código abaixo), considerando também o código das dependências (`%s`). Seu CoT deve cobrir:
				    *   **Fluxo Principal:** Descreva os passos lógicos centrais da execução.
				    *   **Estruturas de Controle:** Analise loops (`forEach`, `for`, `while`) e condicionais (`if`, `else`, `switch`, `try-catch`).
				    *   **Entradas e Estados:** Considere o impacto de entradas nulas, listas/coleções vazias, objetos com campos nulos ou vazios, valores numéricos (zero, negativos, positivos, limites BigDecimal).
				    *   **Casos de Borda:** Identifique limites (0, 1, N itens), valores mínimos/máximos, empates, descontos que zeram o preço, descontos que ultrapassam o preço.
				    *   **Tratamento de Erros:** Avalie blocos `try-catch` existentes. Antecipe exceções não tratadas (e.g., `NullPointerException`, `IllegalArgumentException` para valores inválidos).
				    *   **Robustez:** Avalie criticamente se a lógica é robusta contra os casos de borda e erros identificados.
				2.  **Geração de Cenários:** Com base na sua análise CoT, derive uma lista COMPLETA de cenários de teste descritivos que cubram todos os fluxos, casos de borda e tratamentos de erro relevantes. Para cada cenário, indique brevemente o tipo de verificação principal (e.g., igualdade, nulidade, exceção).
				3.  **Formato JSON:** Estruture sua resposta FINAL exclusivamente como um objeto JSON válido, conforme o esquema abaixo. Inclua um resumo da sua análise CoT e a lista de cenários.
					*   **Nunca acrescente qualquer outra informação no corpo de resposta além da estrutura JSON de saída esperada informada abaixo! O sistema que recebe a sua resposta espera somente um objeto JSON e se você adicionar qualquer texto antes ou depois, irá quebrar a aplicação e gerar um bug. Não faça isso. Retorne exatamente o que lhe foi pedido.**
				
				**Estrutura JSON de Saída Esperada:**

				```json
				{
				  "class_fqn": "{{ NOME_COMPLETO_CLASSE_ALVO }}",
				  "analysis_summary": "<Resumo conciso da análise CoT, destacando pontos críticos, casos de borda e potenciais problemas identificados>",
				  "test_scenarios": [
				    {
				      "id": "scenario_1",
				      "description": "<Descrição clara e concisa do cenário de teste 1>",
				      "expected_outcome_type": "<Tipo de asserção principal, e.g., ASSERT_EQUALS, ASSERT_NOT_NULL, ASSERT_NULL, ASSERT_THROWS_IllegalArgumentException, ASSERT_TRUE>"
				    },
				    // ... mais cenários
				  ]
				}
				```

				**Exemplo Few-Shot (Genérico):**

				*   **Input (Parâmetros Injetados):**
				    *   `{{ NOME_COMPLETO_CLASSE_ALVO }}`: `com.example.service.SimpleDiscountCalculator`
				    *   `{{ CODIGO_CLASSE_ALVO }}`: (Código da classe SimpleDiscountCalculator, que aplica desconto percentual com cap de 50%% e desconto fixo que não negativa o preço, lançando IAE para inputs inválidos)
				    *   `{{ CODIGO_DEPENDENCIAS }}`: (Códigos de ProductDTO [com BigDecimal price] e DiscountType [PERCENTAGE, FIXED_AMOUNT])

				*   **Output JSON Esperado:**
				    ```json
				    {
				      "class_fqn": "com.example.service.SimpleDiscountCalculator",
				      "analysis_summary": "A classe calcula preço com desconto. Valida inputs (produto, tipo, valor não nulos). Aplica desconto PERCENTAGE (valor entre 0-100, cap de 50%%) ou FIXED_AMOUNT (não negativa o preço). Lança IllegalArgumentException para inputs inválidos.",
				      "test_scenarios": [
				        {
				          "id": "scenario_1",
				          "description": "Testar desconto percentual válido abaixo do cap (e.g., 10%%)",
				          "expected_outcome_type": "ASSERT_EQUALS"
				        },
				        {
				          "id": "scenario_2",
				          "description": "Testar desconto percentual exatamente no cap (50%%)",
				          "expected_outcome_type": "ASSERT_EQUALS"
				        },
				        {
				          "id": "scenario_3",
				          "description": "Testar desconto percentual acima do cap (e.g., 60%%) deve aplicar o cap (50%%)",
				          "expected_outcome_type": "ASSERT_EQUALS"
				        },
				        {
				          "id": "scenario_4",
				          "description": "Testar desconto percentual inválido (> 100%%)",
				          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException"
				        },
				        {
				          "id": "scenario_5",
				          "description": "Testar desconto percentual inválido (< 0%%)",
				          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException"
				        },
				        {
				          "id": "scenario_6",
				          "description": "Testar desconto de valor fixo válido que não zera o preço",
				          "expected_outcome_type": "ASSERT_EQUALS"
				        },
				        {
				          "id": "scenario_7",
				          "description": "Testar desconto de valor fixo que zera exatamente o preço",
				          "expected_outcome_type": "ASSERT_EQUALS"
				        },
				        {
				          "id": "scenario_8",
				          "description": "Testar desconto de valor fixo maior que o preço (deve resultar em preço zero)",
				          "expected_outcome_type": "ASSERT_EQUALS"
				        },
				        {
				          "id": "scenario_9",
				          "description": "Testar com produto nulo",
				          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException"
				        },
				        {
				          "id": "scenario_10",
				          "description": "Testar com tipo de desconto nulo",
				          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException"
				        },
				        {
				          "id": "scenario_11",
				          "description": "Testar com valor de desconto nulo",
				          "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException"
				        }
				      ]
				    }
				    ```

				**Sua Tarefa:**

				Agora, aplique esta análise CoT e geração de cenários à classe `%s` com os códigos fornecidos abaixo e gere a resposta JSON correspondente.

				**Código da Classe Alvo (`%s`):**

				```java
				%s
				```

				**Código das Dependências:**

				```java
				%s
				```

				**Resposta JSON:**

				```json
				// Sua resposta JSON aqui
				```
				""",
            targetClassName,
            dependenciesName,
            targetClassName,
            targetClassName,
            context.getTargetClassCode(),
            dependencies);

    return new Prompt(prompt);
  }
}
