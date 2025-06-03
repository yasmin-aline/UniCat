package br.com.unicat.poc.usecases;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenerateUnitTests {
  private final AnalyseClassToTest analyseClassToTest;
  private final MapTestScenarios mapTestScenarios;
  private final IdentityMethodsAndDependencies identityMethodsAndDependencies;
  private final ImplementUnitTests implementUnitTests;
  private final RetryUnitTestsUseCase retryUnitTestsUseCase;

  public GenerateUnitTests(
          final AnalyseClassToTest analyseClassToTest,
          final MapTestScenarios mapTestScenarios,
          final IdentityMethodsAndDependencies identityMethodsAndDependencies,
          ImplementUnitTests implementUnitTests, RetryUnitTestsUseCase retryUnitTestsUseCase) {
    this.analyseClassToTest = analyseClassToTest;
    this.mapTestScenarios = mapTestScenarios;
    this.identityMethodsAndDependencies = identityMethodsAndDependencies;
    this.implementUnitTests = implementUnitTests;
    this.retryUnitTestsUseCase = retryUnitTestsUseCase;
  }

  @PostConstruct
  public void testarRetryManual() {
    log.info("Iniciando testarRetryManual");

    String targetClassName = "GetWinner";
    String targetClassCode = """
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
                player.getCards().forEach(card ->{
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
    String targetClassPackage = "com.deckofcards.usecases";
    String guidelines = "Cobrir todos os métodos, tratar divisões por zero, validar cartas inválidas, garantir que sempre haja um vencedor.";
    String dependencies = "PlayerRequestDTO, WinnerPlayerResponseDTO, Points";
    String scenarios = "Jogador com maior pontuação vence; Cartas inválidas são tratadas; Empate entre jogadores; Nenhum jogador; Jogador com cartas nulas.";
    List<String> testErrors = List.of(
            "execute: 25:expected:<João> but was:<Maria>",
            "execute: 27:expected:<21> but was:<20>",
            "execute: 30:java.lang.NullPointerException",
            "getPlayerWithHighestPoints: 40:expected:<Pedro> but was:<null>",
            "execute: 28:java.lang.IllegalArgumentException: Card value invalid",
            "execute: 32:expected:<10> but was:<0>",
            "getPlayerWithHighestPoints: 42:java.util.NoSuchElementException"
    );

    String testClassName = "GetWinnerTest";
    String testClassCode = "// código da classe de teste atual aqui";
    String assertionLibrary = "JUnit 5 Assertions";

    List<Map<String, String>> failedTestsAndErrors = testErrors.stream()
            .map(error -> {
              Map<String, String> map = new HashMap<>();
              int idx = error.indexOf(':');
              String methodName = idx > 0 ? error.substring(0, idx) : "unknown";
              map.put("methodName", methodName);
              map.put("errorMessage", error);
              return map;
            })
            .collect(Collectors.toList());

    log.info("Parâmetros enviados para retry: className={}, package={}, guidelines={}, dependencies={}, scenarios={}, testErrors={}",
            targetClassName, targetClassPackage, guidelines, dependencies, scenarios, testErrors);

    String resposta = retry(
            targetClassName,
            targetClassPackage,
            targetClassCode,
            testClassName,
            testClassCode,
            guidelines,
            dependencies,
            scenarios,
            failedTestsAndErrors,
            assertionLibrary
    );

    log.info("RESPOSTA RETRY MANUAL: {}", resposta);

    log.info("Finalizou testarRetryManual com sucesso");
  }

  public String run(
      final String targetClassName, final String targetClassCode, final String targetClassPackage) {
    // 1. [Prompt] Analisar profundamente a classe a ser testada [OK]
    final AssistantMessage analyzedClass =
        this.analyseClassToTest.run(targetClassName, targetClassCode, targetClassPackage);

    // 2. [Prompt] Mapear todos Cenários de Teste [OK]
    final AssistantMessage mappedTestScenarios =
        this.mapTestScenarios.run(analyzedClass, targetClassName, targetClassCode);

    // 3. [Prompt] Identificar Métodos e Dependências para cada Cenário de Teste [OK]
    final AssistantMessage identifiedMethodsAndDependencies =
        this.identityMethodsAndDependencies.run(
            mappedTestScenarios, targetClassName, targetClassCode);

    return identifiedMethodsAndDependencies.getText();
  }

  public String complete(
      final String targetClassName,
      final String targetClassCode,
      final String targetClassPackage,
      final String guidelines,
      final String dependencies,
      final String scenarios) {
    // 7. [Prompt] Responde com o a Implementação do Primeiro Cenário de Teste
    AssistantMessage ans =
        this.implementUnitTests.run(
            targetClassName,
            targetClassCode,
            targetClassPackage,
            guidelines,
            dependencies,
            scenarios);
    return ans.getText();
  }
  public String retry(
          String targetClassName,
          String targetClassPackage,
          String targetClassCode,
          String testClassName,
          String testClassCode,
          String guidelines,
          String dependencies,
          String scenarios,
          List<Map<String, String>> failedTestsAndErrors,
          String assertionLibrary
  ) {
    return retryUnitTestsUseCase.run(
            targetClassName,
            targetClassPackage,
            targetClassCode,
            testClassName,
            testClassCode,
            guidelines,
            dependencies,
            scenarios,
            failedTestsAndErrors,
            assertionLibrary
    ).getText();
  }
}
