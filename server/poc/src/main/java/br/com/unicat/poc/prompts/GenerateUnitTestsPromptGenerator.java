package br.com.unicat.poc.prompts;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class GenerateUnitTestsPromptGenerator {

	public Prompt get() {
		String prompt = String.format("""
				# Prompt para Use Case: Gerar Código de Teste Unitário (Versão 2 - Saída JSON & Few-Shot)

**Objetivo:** Gerar o código Java completo de uma classe de teste JUnit 5, implementando uma lista específica de cenários de teste, com base no código da classe alvo e suas dependências.
A resposta deve ser um JSON contendo o FQN da classe de teste e o código gerado.

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
				
				
				""");

		return new Prompt(prompt);
	}
}
