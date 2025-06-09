package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.adapter.http.context.RequestContext;
import br.com.unicat.poc.adapter.http.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.request.RetryRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.CompleteResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.RefactoredUnitTestResponseDTO;
import br.com.unicat.poc.usecases.AnalyseLogicAndIdentifyScenariosUseCase;
import br.com.unicat.poc.usecases.StacktraceInterpreterUseCase;
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
  private final StacktraceInterpreterUseCase stacktraceInterpreterUseCase;
  private final CompleteUnitTestsCreationInterface completeUnitTestsCreation;
  private final RefactorFailingUnitTestsInterface refactorFailingUnitTestsCreation;
  private final AnalyseLogicAndIdentifyScenariosUseCase analyseLogicAndIdentifyScenariosUseCase;

  @PostMapping(path = "/init")
  public ResponseEntity<InitResponseDTO> init(final RequestContext requestContext)
      throws Exception {
    log.info("INIT init. requestContext: {}", requestContext);
    final InitResponseDTO ans = this.initUnitTestsCreation.execute();

    log.info("END init. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }

  @PostMapping(path = "/complete")
  public ResponseEntity<CompleteResponseDTO> complete(
      @ModelAttribute final CompleteRequestDTO requestDTO, final RequestContext requestContext)
      throws Exception {
    log.info("INIT complete. requestDTO: {}", requestDTO);
    final var analysedLogicResponseDTO =
        this.analyseLogicAndIdentifyScenariosUseCase.execute(
            requestDTO.dependenciesName(), requestDTO.dependencies());
    final var testClassGenerated =
        this.completeUnitTestsCreation.execute(requestDTO, analysedLogicResponseDTO);

    log.info("END complete. ans: {}", testClassGenerated);
    return ResponseEntity.ok().body(testClassGenerated);
  }

  @PostMapping(path = "/retry")
  public ResponseEntity<RefactoredUnitTestResponseDTO> retry(
      @ModelAttribute final RetryRequestDTO requestDTO, final RequestContext requestContext)
      throws Exception {
    log.info("INIT retry. requestDTO: {}", requestDTO);
    final var testResults = this.stacktraceInterpreterUseCase.execute(requestDTO);

    final RefactoredUnitTestResponseDTO ans =
        this.refactorFailingUnitTestsCreation.execute(
            requestDTO.dependenciesName(),
            requestDTO.dependencies(),
            requestDTO.testClassCode(),
                testResults);

    log.info("END retry. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
  }
}
