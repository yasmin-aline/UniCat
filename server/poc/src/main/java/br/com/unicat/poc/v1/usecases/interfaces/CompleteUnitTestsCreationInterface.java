package br.com.unicat.poc.v1.usecases.interfaces;

import br.com.unicat.poc.v1.controller.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.v1.controller.dtos.response.CompleteResponseDTO;
import br.com.unicat.poc.v1.entities.AnalysedLogic;

public interface CompleteUnitTestsCreationInterface {
  CompleteResponseDTO execute(CompleteRequestDTO requestDTO, AnalysedLogic analysedLogic)
      throws Exception;
}
