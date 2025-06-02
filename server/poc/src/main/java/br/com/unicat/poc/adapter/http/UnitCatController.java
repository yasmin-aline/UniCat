package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.adapter.http.dtos.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.InitRequestDTO;
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
    log.info("INIT init. requestDTO: {}", requestDTO);
    final String ans =
        this.generateUnitTests.run(
            requestDTO.getTargetClassName(),
            requestDTO.getTargetClassCode(),
            requestDTO.getTargetClassPackage());

    log.info("END init. ans: {}", ans);
    return ResponseEntity.ok().body(ans);
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
}
