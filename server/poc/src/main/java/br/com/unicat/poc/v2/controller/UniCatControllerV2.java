package br.com.unicat.poc.v2.controller;

import br.com.unicat.poc.v2.controller.context.RequestContextV2;
import br.com.unicat.poc.v2.model.prompt4.FixedUnitTests;
import br.com.unicat.poc.v2.service.usecase.FixUnitTestsUseCase;
import br.com.unicat.poc.v2.service.usecase.GenerateUnitTestsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v2/unicat")
public class UniCatControllerV2 {

  private final GenerateUnitTestsUseCase generateUnitTestsUseCase;
  private final FixUnitTestsUseCase fixUnitTestsUseCase;

  @PostMapping(path = "/generate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  ResponseEntity<String> generate(RequestContextV2 requestContextV2) throws Exception {
    log.info("INIT generate.");
    final var ans = this.generateUnitTestsUseCase.execute();

    log.info("FINISH generate.");
    return ResponseEntity.status(HttpStatus.OK).body(ans);
  }

  @PostMapping(path = "/fix", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  ResponseEntity<FixedUnitTests> fix(RequestContextV2 requestContextV2) throws Exception {
    log.info("INIT fix.");
    final var ans = this.fixUnitTestsUseCase.execute();

    log.info("FINISH fix.");
    return ResponseEntity.status(HttpStatus.OK).body(ans);
  }
}
