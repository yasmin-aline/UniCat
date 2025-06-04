package br.com.unicat.poc.prompts;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.context.RequestContextHolder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class GenerateUnitTestsPromptGenerator {

  public Prompt get() {
    RequestContext context = RequestContextHolder.getContext();

    final String scenariosJson =
"""
{
  "class_fqn": "com.deckofcards.usecases.GetWinner",
  "analysis_summary": "A classe GetWinner tem como objetivo identificar o jogador com a maior pontuação com base em uma lista de cartas representadas por Strings. Cada jogador possui um nome e uma lista de cartas (numéricas ou letras que representam cartas especiais como ACE, JACK, QUEEN, KINGS). A lógica principal consiste em iterar sobre cada jogador, converter as cartas em pontos, somar e registrar os totais. Um método auxiliar determina o jogador com a maior pontuação. Casos de borda incluem listas de jogadores vazias ou nulas, jogadores com cartas vazias ou nulas, cartas inválidas ou que não pertencem ao enum Points e empates. A ausência de tratamento explícito para entradas nulas pode gerar NullPointerException. A lógica assume que todos os valores String não numéricos são válidos enums, o que pode lançar IllegalArgumentException. A ausência de fallback para valores inválidos torna o código pouco robusto. O método getPlayerWithHighestPoints retorna null em caso de empate ou mapa vazio, o que pode causar NullPointerException ao acessar a pontuação do vencedor.",
  "test_scenarios": [
    { "id": "scenario_1", "description": "Lista com dois jogadores, ambos com cartas numéricas, um com soma maior que o outro", "expected_outcome_type": "ASSERT_EQUALS" },
    { "id": "scenario_2", "description": "Lista com jogador contendo cartas mistas (número e enum: '10', 'ACE')", "expected_outcome_type": "ASSERT_EQUALS" },
    { "id": "scenario_3", "description": "Carta inválida que não é número nem enum (e.g., 'Z') deve lançar IllegalArgumentException", "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException" },
    { "id": "scenario_4", "description": "Lista com um jogador com todas as cartas do enum (ACE, JACK, QUEEN, KINGS)", "expected_outcome_type": "ASSERT_EQUALS" },
    { "id": "scenario_5", "description": "Dois jogadores com mesma pontuação total (empate)", "expected_outcome_type": "ASSERT_NOT_NULL" },
    { "id": "scenario_6", "description": "Lista vazia de jogadores", "expected_outcome_type": "ASSERT_THROWS_NullPointerException" },
    { "id": "scenario_7", "description": "Parâmetro null como lista de jogadores", "expected_outcome_type": "ASSERT_THROWS_NullPointerException" },
    { "id": "scenario_8", "description": "Jogador com lista de cartas vazia", "expected_outcome_type": "ASSERT_EQUALS" },
    { "id": "scenario_9", "description": "Jogador com campo de nome null", "expected_outcome_type": "ASSERT_THROWS_NullPointerException" },
    { "id": "scenario_10", "description": "Jogador com lista de cartas null", "expected_outcome_type": "ASSERT_THROWS_NullPointerException" },
    { "id": "scenario_11", "description": "Jogador com carta null na lista", "expected_outcome_type": "ASSERT_THROWS_NullPointerException" },
    { "id": "scenario_12", "description": "Jogador com valor extremo em carta numérica (e.g., '999')", "expected_outcome_type": "ASSERT_EQUALS" },
    { "id": "scenario_13", "description": "Jogador com valor numérico negativo (e.g., '-5')", "expected_outcome_type": "ASSERT_EQUALS" },
    { "id": "scenario_14", "description": "Cartas em minúsculo (e.g., 'jack') devem lançar IllegalArgumentException", "expected_outcome_type": "ASSERT_THROWS_IllegalArgumentException" },
    { "id": "scenario_15", "description": "Apenas um jogador na lista", "expected_outcome_type": "ASSERT_EQUALS" }
  ]
}
""";

    final String dependencies =
"""
package com.deckofcards.adapter.http.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequestDTO {
    private String name;
    private List<String> cards;
}

package com.deckofcards.adapter.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WinnerPlayerResponseDTO {
    private String name;
    private Integer points;
}

package com.deckofcards.entities.enums;

public enum Points {
    ACE(1),
    JACK(11),
    QUEEN(12),
    KINGS(13);

    private final int point;

    Points(int point) {
        this.point = point;
    }

    public int getPoint() {
        return point;
    }
}
""";

    String targetClassName = "com.deckofcards.usecases.GetWinner";

    String targetClassCode =
"""
package com.deckofcards.usecases;

import com.deckofcards.adapter.http.dto.request.PlayerRequestDTO;
import com.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO;
import com.deckofcards.entities.enums.Points;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class GetWinner {

    public WinnerPlayerResponseDTO execute(final List<PlayerRequestDTO> players) {
        var playersPoints = new HashMap<String, Integer>();

        players.forEach(player -> {
            AtomicInteger sum = new AtomicInteger();
            player.getCards().forEach(card -> {
                var points = 0;

                try {
                    points += Integer.parseInt(card);
                } catch (Exception e) {
                    points += Points.valueOf(card).getPoint();
                }

                sum.addAndGet(points);
            });

            playersPoints.put(player.getName(), sum.get());
        });

        String winner = getPlayerWithHighestPoints(playersPoints);
        int points = playersPoints.get(winner);
        return new WinnerPlayerResponseDTO(winner, points);
    }

    private <K, V extends Comparable<V>> K getPlayerWithHighestPoints(HashMap<K, V> playersPoints) {
        Optional<Map.Entry<K, V>> winner = playersPoints.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        return winner.map(Map.Entry::getKey).orElse(null);
    }
}
""";

    String prompt =
        String.format(
"""
# Prompt para Use Case: Gerar Código de Teste Unitário (Versão 2 - Saída JSON & Few-Shot)

**Objetivo:** Gerar o código Java completo de uma classe de teste JUnit 5, implementando uma lista específica de cenários de teste, com base no código da classe alvo e suas dependências. A resposta deve ser um JSON contendo o FQN da classe de teste e o código gerado.

**Instruções:**

1.  **Entendimento:** Receba o código da classe alvo (`%s`), o código de suas dependências (`{{ CODIGO_DEPENDENCIAS }}`), e uma lista estruturada de cenários de teste (`{{ CENARIOS_TESTE_JSON }}`).
2.  **Geração de Código:** Crie uma classe de teste JUnit 5 completa no pacote correto (geralmente o mesmo da classe alvo, mas em `src/test/java`).
    *   Implemente CADA cenário de teste fornecido no JSON `{{ CENARIOS_TESTE_JSON }}` como um método `@Test` separado.
    *   Use `@DisplayName` para cada método de teste, utilizando a `description` do cenário correspondente.
    *   Siga rigorosamente a estrutura Arrange-Act-Assert.
    *   Utilize as asserções apropriadas do JUnit 5 (e.g., `assertEquals`, `assertNull`, `assertThrows`) conforme sugerido pelo `expected_outcome_type` do cenário, mas adapte conforme necessário para a verificação completa. Use `BigDecimal.valueOf()` para criar valores e `assertEquals(0, expected.compareTo(actual))` para comparar BigDecimals.
    *   Inclua um método `@BeforeEach` para inicializar a classe alvo (`%s`) se for reutilizável entre os testes.
    *   Garanta que todos os imports necessários estão presentes.
3.  **Formato JSON de Saída:** Retorne sua resposta EXCLUSIVAMENTE como um objeto JSON válido, contendo o FQN da classe de teste gerada e o código completo, conforme a estrutura abaixo.

**Estrutura JSON de Saída Esperada:**

```json
{
  "generated_test_class_fqn": "<FQN completo da classe de teste gerada, e.g., com.example.MyClassTest>",
  "generated_test_code": "<Código Java completo da classe de teste como uma string, incluindo package e imports>"
}
```

**Sua Tarefa:**

Agora, gere o código de teste JUnit 5 para a classe `%s` com base nos códigos e cenários fornecidos abaixo, retornando a resposta no formato JSON especificado.

**Código da Classe Alvo (`%s`):**

```java
%s
```

**Código das Dependências:**

```java
%s
```

**Cenários de Teste (JSON):**

```json
%s
```

**Resposta JSON:**

```json
// Sua resposta JSON aqui
```
""",
            targetClassName,
            targetClassName,
            targetClassName,
            targetClassName,
            targetClassCode,
            dependencies,
            scenariosJson);

    return new Prompt(prompt);
  }
}
