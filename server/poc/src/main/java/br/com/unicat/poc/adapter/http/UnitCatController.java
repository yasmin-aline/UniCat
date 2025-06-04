package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.request.RetryRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.AnalysedLogicResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;
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
  public ResponseEntity<InitResponseDTO> init(RequestContext requestContext) throws Exception {
    log.info("INIT init. requestContext: {}", requestContext);
    final InitResponseDTO ans = this.initUnitTestsCreation.execute();

    log.info("END init. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }

  @PostMapping(path = "/complete")
  public ResponseEntity<AnalysedLogicResponseDTO> complete(
      @ModelAttribute final CompleteRequestDTO requestDTO,
      RequestContext requestContext) throws Exception {
    log.info("INIT complete. requestDTO: {}", requestDTO);
    final var ans = this.analyseLogicAndIdentifyScenariosUseCase.execute(requestDTO.getDependenciesName(), requestDTO.getDependencies());

//    final CompleteResponseDTO ans = this.completeUnitTestsCreation.execute();

    log.info("END complete. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }

  @PostMapping(path = "/retry")
  public ResponseEntity<String> retry(@ModelAttribute final RetryRequestDTO requestDTO) {
    log.info("INIT retry. requestDTO: {}", requestDTO);
    final String ans = this.refactorFailingUnitTestsCreation.execute();

    log.info("END retry. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }
}
