package br.com.unicat.poc.v1.usecases.interfaces;

import br.com.unicat.poc.v1.controller.dtos.response.InitResponseDTO;

public interface InitUnitTestsCreationInterface {
  InitResponseDTO execute() throws Exception;
}
