package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.adapter.http.dtos.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.InitRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.RetryRequestDTO;
import br.com.unicat.poc.usecases.GenerateUnitTests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = "/unitcat/api")
public class UnitCatController {
  private final GenerateUnitTests generateUnitTests;

  public UnitCatController(GenerateUnitTests generateUnitTests) {
    this.generateUnitTests = generateUnitTests;
  }

  @PostMapping(path = "/init")
  public ResponseEntity<String> init(@ModelAttribute final InitRequestDTO requestDTO) {
//    log.info("INIT init. requestDTO: {}", requestDTO);
//    final String ans =
//        this.generateUnitTests.run(
//            requestDTO.getTargetClassName(),
//            requestDTO.getTargetClassCode(),
//            requestDTO.getTargetClassPackage());
//
//    log.info("END init. ans: {}", ans);
    return ResponseEntity.ok().body("""
            ### Análise de Mocks por Cenário
            
            Cenário 1 (Happy Path): Lista de jogadores válida fornecida, onde cada jogador tem cartas que podem ser convertidas em pontos inteiros. Esperado: Retorna um `WinnerPlayerResponseDTO` com o nome do jogador que tem mais pontos e a contagem de pontos correta.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa configurar quando a lista de jogadores é passada para o método e verificar a chamada do método).
            
            Cenário 2 (Empate): Lista de jogadores válida fornecida, onde dois jogadores têm a mesma quantidade de pontos. Esperado: Retorna um `WinnerPlayerResponseDTO` com o nome de um dos jogadores empatados.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa configurar a lista de jogadores com pontos iguais e verificar a chamada do método).
            
            Cenário 3 (Cartas com Enum): Lista de jogadores onde as cartas são representadas por valores do enum `Points`. Esperado: Retorna um `WinnerPlayerResponseDTO` com o nome do jogador que tem mais pontos, calculados corretamente a partir dos valores do enum.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa configurar a lista de jogadores com cartas que correspondem aos valores do enum Points e verificar a chamada do método).
            
            Cenário 4 (Erro - Lista Vazia): Lista de jogadores vazia fornecida. Esperado: Retorna um `WinnerPlayerResponseDTO` com valores nulos ou zero.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa passar uma lista vazia e verificar a chamada do método).
            
            Cenário 5 (Erro - Jogador sem Cartas): Lista de jogadores onde um ou mais jogadores não têm cartas. Esperado: Retorna um `WinnerPlayerResponseDTO` com o nome do jogador que tem mais pontos, considerando apenas os jogadores que têm cartas.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa configurar a lista de jogadores, onde um ou mais jogadores têm cartas vazias e verificar a chamada do método).
            
            Cenário 6 (Erro - Cartas Inválidas): Lista de jogadores onde um jogador tem uma carta que não pode ser convertida em um inteiro ou enum. Esperado: Lança uma exceção (por exemplo, `IllegalArgumentException`), ou trata o erro e continua com os outros jogadores.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa configurar a lista de jogadores com cartas inválidas e verificar a chamada do método).
            
            Cenário 7 (Erro - Mock Lança Exceção): Mock da lista de jogadores configurado para lançar uma exceção ao tentar acessar os jogadores. Esperado: Propaga a exceção lançada.
            
            - **Método Sob Teste:** execute(final List<PlayerRequestDTO> players)
            - **Mocks Relevantes para Configurar:** players (precisa configurar o mock para lançar uma exceção ao acessar a lista de jogadores).
            
            ---
            
            ### Lista de Mocks
            - PlayerRequestDTO: com.deckofcards.adapter.http.dto.request.PlayerRequestDTO
            - WinnerPlayerResponseDTO: com.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO
            - Points: com.deckofcards.entities.enums.Points
            """);
  }

  @PostMapping(path = "/complete")
  public ResponseEntity<String> complete(@ModelAttribute final CompleteRequestDTO requestDTO) {
    log.info("INIT complete. requestDTO: {}", requestDTO);
    final String ans =
        this.generateUnitTests.complete(
            requestDTO.getTargetClassName(),
            requestDTO.getTargetClassCode(),
            requestDTO.getTargetClassPackage(),
            requestDTO.getGuidelines(),
            requestDTO.getDependencies(),
            requestDTO.getScenarios());

    log.info("END complete. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }
  @PostMapping(path = "/retry")
  public ResponseEntity<String> retry(@ModelAttribute final RetryRequestDTO requestDTO) {
    log.info("INIT retry. requestDTO: {}", requestDTO);
    final String ans = generateUnitTests.retry(
            requestDTO.getTargetClassName(),
            requestDTO.getTargetClassCode(),
            requestDTO.getTestClassName(),
            requestDTO.getTestClassCode(),
            requestDTO.getTargetClassPackage(),
            requestDTO.getGuidelines(),
            requestDTO.getDependencies(),
            requestDTO.getScenarios(),
            requestDTO.getFailedTestsAndErrors(),
            requestDTO.getAssertionLibrary()
    );
    log.info("END retry. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }
}
