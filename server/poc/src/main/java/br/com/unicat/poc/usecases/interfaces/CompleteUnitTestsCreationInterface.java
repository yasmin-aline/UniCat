package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.request.CompleteRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.AnalysedLogicResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.CompleteResponseDTO;

public interface CompleteUnitTestsCreationInterface {
  CompleteResponseDTO execute(CompleteRequestDTO requestDTO, AnalysedLogicResponseDTO analysedLogicResponseDTO) throws Exception;
}
