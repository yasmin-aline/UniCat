package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.response.InitResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface InitUnitTestsCreationInterface {
  InitResponseDTO execute() throws JsonProcessingException;
}
