package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;

public interface InitUnitTestsCreationInterface {
  InitResponseDTO execute() throws Exception;
}
