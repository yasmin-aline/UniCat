package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.CompleteResponseDTO;
import br.com.unicat.poc.entities.AnalysedLogic;

public interface CompleteUnitTestsCreationInterface {
  CompleteResponseDTO execute(CompleteRequestDTO requestDTO, AnalysedLogic analysedLogic)
      throws Exception;
}
