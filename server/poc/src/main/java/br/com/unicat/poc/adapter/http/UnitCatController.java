package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.request.RetryRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.*;
import br.com.unicat.poc.usecases.AnalyseLogicAndIdentifyScenariosUseCase;
import br.com.unicat.poc.usecases.interfaces.CompleteUnitTestsCreationInterface;
import br.com.unicat.poc.usecases.interfaces.InitUnitTestsCreationInterface;
import br.com.unicat.poc.usecases.interfaces.RefactorFailingUnitTestsInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/unitcat/api")
public class UnitCatController {

  private final InitUnitTestsCreationInterface initUnitTestsCreation;
  private final CompleteUnitTestsCreationInterface completeUnitTestsCreation;
  private final RefactorFailingUnitTestsInterface refactorFailingUnitTestsCreation;
  private final AnalyseLogicAndIdentifyScenariosUseCase analyseLogicAndIdentifyScenariosUseCase;

  @PostMapping(path = "/init")
  public ResponseEntity<InitResponseDTO> init(final RequestContext requestContext) throws Exception {
    log.info("INIT init. requestContext: {}", requestContext);
    final InitResponseDTO ans = InitResponseDTO.builder()
        .analysisResponseDTO(
            AnalysisResponseDTO.builder()
                .classFqn("com.deckofcards.usecases.GetWinner")
                .purposeSummary("Determina o vencedor de um jogo de cartas com base nos pontos acumulados pelos jogadores, processando suas cartas e calculando a pontuação total.")
                .mainMethodSignature("public WinnerPlayerResponseDTO execute(final List<PlayerRequestDTO> players)")
                .inputType("java.util.List<com.deckofcards.adapter.http.dto.request.PlayerRequestDTO>")
                .outputType("com.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO>")
                .build()
        )
        .customDependencies(List.of(
            "com.deckofcards.adapter.http.dto.request.PlayerRequestDTO",
            "com.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO",
            "com.deckofcards.entities.enums.Points"))
        .build();

    log.info("END init. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }

  @PostMapping(path = "/complete")
  public ResponseEntity<CompleteResponseDTO> complete(
      @ModelAttribute final CompleteRequestDTO requestDTO,
      final RequestContext requestContext) throws Exception {
    log.info("INIT complete. requestDTO: {}", requestDTO);
    final var testClassGenerated = CompleteResponseDTO.builder()
        .generatedTestClassFqn("com.deckofcards.usecases.GetWinnerTest")
        .generatedTestCode("package com.deckofcards.usecases;\n\nimport com.deckofcards.adapter.http.dto.request.PlayerRequestDTO;\nimport com.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO;\nimport com.deckofcards.entities.enums.Points;\nimport org.junit.jupiter.api.BeforeEach;\nimport org.junit.jupiter.api.DisplayName;\nimport org.junit.jupiter.api.Test;\n\nimport java.util.Arrays;\nimport java.util.Collections;\nimport java.util.List;\n\nimport static org.junit.jupiter.api.Assertions.*;\n\nclass GetWinnerTest {\n\n    private GetWinner getWinner;\n\n    @BeforeEach\n    void setUp() {\n        getWinner = new GetWinner();\n    }\n\n    @Test\n    @DisplayName(\"Testar com uma lista de jogadores com cartas válidas e diferentes pontuações.\")\n    void execute_shouldReturnWinnerWithHighestPoints() {\n        // Arrange\n        PlayerRequestDTO player1 = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"KINGS\")).build();\n        PlayerRequestDTO player2 = PlayerRequestDTO.builder().name(\"Bob\").cards(Arrays.asList(\"QUEEN\", \"JACK\")).build();\n        List<PlayerRequestDTO> players = Arrays.asList(player1, player2);\n        WinnerPlayerResponseDTO expectedWinner = new WinnerPlayerResponseDTO(\"Bob\", 23);\n\n        // Act\n        WinnerPlayerResponseDTO actualWinner = getWinner.execute(players);\n\n        // Assert\n        assertEquals(expectedWinner, actualWinner);\n    }\n\n    @Test\n    @DisplayName(\"Testar com um jogador que não possui cartas (lista vazia).\")\n    void execute_shouldThrowNullPointerExceptionForEmptyCards() {\n        // Arrange\n        PlayerRequestDTO player = PlayerRequestDTO.builder().name(\"Alice\").cards(Collections.emptyList()).build();\n        List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n        // Act & Assert\n        assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n    }\n\n    @Test\n    @DisplayName(\"Testar com um jogador que possui cartas inválidas (não numéricas e não reconhecidas como pontos).\")\n    void execute_shouldThrowIllegalArgumentExceptionForInvalidCards() {\n        // Arrange\n        PlayerRequestDTO player = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"INVALID_CARD\")).build();\n        List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n        // Act & Assert\n        assertThrows(IllegalArgumentException.class, () -> getWinner.execute(players));\n    }\n\n    @Test\n    @DisplayName(\"Testar com dois jogadores que têm a mesma pontuação (empate).\")\n    void execute_shouldReturnFirstPlayerInCaseOfTie() {\n        // Arrange\n        PlayerRequestDTO player1 = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"KINGS\")).build();\n        PlayerRequestDTO player2 = PlayerRequestDTO.builder().name(\"Bob\").cards(Arrays.asList(\"ACE\", \"KINGS\")).build();\n        List<PlayerRequestDTO> players = Arrays.asList(player1, player2);\n        WinnerPlayerResponseDTO expectedWinner = new WinnerPlayerResponseDTO(\"Alice\", 14);\n\n        // Act\n        WinnerPlayerResponseDTO actualWinner = getWinner.execute(players);\n\n        // Assert\n        assertEquals(expectedWinner, actualWinner);\n    }\n\n    @Test\n    @DisplayName(\"Testar com uma lista de jogadores onde todos têm cartas com valores zero.\")\n    void execute_shouldReturnFirstPlayerWithZeroPoints() {\n        // Arrange\n        PlayerRequestDTO player1 = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"ACE\")).build();\n        PlayerRequestDTO player2 = PlayerRequestDTO.builder().name(\"Bob\").cards(Arrays.asList(\"ACE\", \"ACE\")).build();\n        List<PlayerRequestDTO> players = Arrays.asList(player1, player2);\n        WinnerPlayerResponseDTO expectedWinner = new WinnerPlayerResponseDTO(\"Alice\", 2);\n\n        // Act\n        WinnerPlayerResponseDTO actualWinner = getWinner.execute(players);\n\n        // Assert\n        assertEquals(expectedWinner, actualWinner);\n    }\n\n    @Test\n    @DisplayName(\"Testar com uma lista contendo um jogador com nome nulo.\")\n    void execute_shouldThrowNullPointerExceptionForNullPlayerName() {\n        // Arrange\n        PlayerRequestDTO player = PlayerRequestDTO.builder().name(null).cards(Arrays.asList(\"ACE\")).build();\n        List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n        // Act & Assert\n        assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n    }\n\n    @Test\n    @DisplayName(\"Testar com uma lista de jogadores onde todos têm cartas válidas, mas uma delas é uma string que não corresponde a uma carta ou número.\")\n    void execute_shouldThrowIllegalArgumentExceptionForNonCardString() {\n        // Arrange\n        PlayerRequestDTO player = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"NON_CARD_STRING\")).build();\n        List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n        // Act & Assert\n        assertThrows(IllegalArgumentException.class, () -> getWinner.execute(players));\n    }\n\n    @Test\n    @DisplayName(\"Testar com uma lista de jogadores onde todos têm cartas válidas, mas um jogador tem uma carta nula.\")\n    void execute_shouldThrowNullPointerExceptionForNullCard() {\n        // Arrange\n        PlayerRequestDTO player = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", null)).build();\n        List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n        // Act & Assert\n        assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n    }\n\n    @Test\n    @DisplayName(\"Testar com uma lista de jogadores vazia.\")\n    void execute_shouldThrowNullPointerExceptionForEmptyPlayersList() {\n        // Arrange\n        List<PlayerRequestDTO> players = Collections.emptyList();\n\n        // Act & Assert\n        assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n    }\n\n    @Test\n    @DisplayName(\"Testar com um jogador que tem cartas válidas, mas a lista de cartas é nula.\")\n    void execute_shouldThrowNullPointerExceptionForNullCardsList() {\n        // Arrange\n        PlayerRequestDTO player = PlayerRequestDTO.builder().name(\"Alice\").cards(null).build();\n        List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n        // Act & Assert\n        assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n    }\n}")
        .build();

    log.info("END complete. ans: {}", testClassGenerated);
    return ResponseEntity.ok().body(testClassGenerated);
  }

  @PostMapping(path = "/retry")
  public ResponseEntity<RefactoredUnitTestResponseDTO> retry(@ModelAttribute final RetryRequestDTO requestDTO, final RequestContext requestContext) throws Exception {
    log.info("INIT retry. requestDTO: {}", requestDTO);
    RefactoredUnitTestResponseDTO ans = new RefactoredUnitTestResponseDTO(
      List.of(
        new RefactoredTestCodeResponseDTO("execute_shouldReturnWinnerWithHighestPoints",
          "    @Test\n" +
          "    @DisplayName(\"Testar com uma lista de jogadores com cartas válidas e diferentes pontuações.\")\n" +
          "    void execute_shouldReturnWinnerWithHighestPoints() {\n" +
          "        // Arrange\n" +
          "        PlayerRequestDTO player1 = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"KINGS\")).build();\n" +
          "        PlayerRequestDTO player2 = PlayerRequestDTO.builder().name(\"Bob\").cards(Arrays.asList(\"QUEEN\", \"JACK\")).build();\n" +
          "        List<PlayerRequestDTO> players = Arrays.asList(player1, player2);\n" +
          "        WinnerPlayerResponseDTO expectedWinner = new WinnerPlayerResponseDTO(\"Bob\", 23);\n\n" +
          "        // Act\n" +
          "        WinnerPlayerResponseDTO actualWinner = getWinner.execute(players);\n\n" +
          "        // Assert\n" +
          "        assertEquals(expectedWinner.getName(), actualWinner.getName()); // CORREÇÃO: Comparar apenas os nomes\n" +
          "        assertEquals(expectedWinner.getPoints(), actualWinner.getPoints()); // CORREÇÃO: Comparar apenas os pontos\n" +
          "    }"
        ),
        new RefactoredTestCodeResponseDTO("execute_shouldThrowNullPointerExceptionForNullPlayerName",
          "        // TESTE COMENTADO: Espera-se que um jogador com nome nulo lance uma NullPointerException,\n" +
          "        // mas a lógica atual não está tratando isso corretamente.\n" +
          "        /*\n" +
          "        @Test\n" +
          "        @DisplayName(\"Testar com uma lista contendo um jogador com nome nulo.\")\n" +
          "        void execute_shouldThrowNullPointerExceptionForNullPlayerName() {\n" +
          "            // Arrange\n" +
          "            PlayerRequestDTO player = PlayerRequestDTO.builder().name(null).cards(Arrays.asList(\"ACE\")).build();\n" +
          "            List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n" +
          "            // Act & Assert\n" +
          "            assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n" +
          "        }\n" +
          "        */"
        ),
        new RefactoredTestCodeResponseDTO("execute_shouldReturnFirstPlayerWithZeroPoints",
          "        // TESTE COMENTADO: Espera-se que todos os jogadores com cartas que somam zero retornem o primeiro jogador,\n" +
          "        // mas a lógica atual não está tratando isso corretamente.\n" +
          "        /*\n" +
          "        @Test\n" +
          "        @DisplayName(\"Testar com uma lista de jogadores onde todos têm cartas com valores zero.\")\n" +
          "        void execute_shouldReturnFirstPlayerWithZeroPoints() {\n" +
          "            // Arrange\n" +
          "            PlayerRequestDTO player1 = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"ACE\")).build();\n" +
          "            PlayerRequestDTO player2 = PlayerRequestDTO.builder().name(\"Bob\").cards(Arrays.asList(\"ACE\", \"ACE\")).build();\n" +
          "            List<PlayerRequestDTO> players = Arrays.asList(player1, player2);\n" +
          "            WinnerPlayerResponseDTO expectedWinner = new WinnerPlayerResponseDTO(\"Alice\", 2);\n\n" +
          "            // Act\n" +
          "            WinnerPlayerResponseDTO actualWinner = getWinner.execute(players);\n\n" +
          "            // Assert\n" +
          "            assertEquals(expectedWinner, actualWinner);\n" +
          "        }\n" +
          "        */"
        ),
        new RefactoredTestCodeResponseDTO("execute_shouldThrowNullPointerExceptionForEmptyCards",
          "        // TESTE COMENTADO: Espera-se que um jogador com uma lista de cartas vazia lance uma NullPointerException,\n" +
          "        // mas a lógica atual não está tratando isso corretamente.\n" +
          "        /*\n" +
          "        @Test\n" +
          "        @DisplayName(\"Testar com um jogador que não possui cartas (lista vazia).\")\n" +
          "        void execute_shouldThrowNullPointerExceptionForEmptyCards() {\n" +
          "            // Arrange\n" +
          "            PlayerRequestDTO player = PlayerRequestDTO.builder().name(\"Alice\").cards(Collections.emptyList()).build();\n" +
          "            List<PlayerRequestDTO> players = Collections.singletonList(player);\n\n" +
          "            // Act & Assert\n" +
          "            assertThrows(NullPointerException.class, () -> getWinner.execute(players));\n" +
          "        }\n" +
          "        */"
        ),
        new RefactoredTestCodeResponseDTO("execute_shouldReturnFirstPlayerInCaseOfTie",
          "        // TESTE COMENTADO: Espera-se que em caso de empate, o primeiro jogador seja retornado,\n" +
          "        // mas a lógica atual não está tratando isso corretamente.\n" +
          "        /*\n" +
          "        @Test\n" +
          "        @DisplayName(\"Testar com dois jogadores que têm a mesma pontuação (empate).\")\n" +
          "        void execute_shouldReturnFirstPlayerInCaseOfTie() {\n" +
          "            // Arrange\n" +
          "            PlayerRequestDTO player1 = PlayerRequestDTO.builder().name(\"Alice\").cards(Arrays.asList(\"ACE\", \"KINGS\")).build();\n" +
          "            PlayerRequestDTO player2 = PlayerRequestDTO.builder().name(\"Bob\").cards(Arrays.asList(\"ACE\", \"KINGS\")).build();\n" +
          "            List<PlayerRequestDTO> players = Arrays.asList(player1, player2);\n" +
          "            WinnerPlayerResponseDTO expectedWinner = new WinnerPlayerResponseDTO(\"Alice\", 14);\n\n" +
          "            // Act\n" +
          "            WinnerPlayerResponseDTO actualWinner = getWinner.execute(players);\n\n" +
          "            // Assert\n" +
          "            assertEquals(expectedWinner, actualWinner);\n" +
          "        }\n" +
          "        */"
        )
      ),
      List.of()
    );

    log.info("END retry. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }
}
