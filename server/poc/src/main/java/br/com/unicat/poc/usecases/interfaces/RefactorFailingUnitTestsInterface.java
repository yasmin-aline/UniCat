package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.request.FailingTestDetailsRequestDTO;
import br.com.unicat.poc.adapter.http.dtos.response.RefactoredUnitTestResponseDTO;

import java.util.List;

public interface RefactorFailingUnitTestsInterface {
  RefactoredUnitTestResponseDTO execute(final String dependenciesName, final String dependencies, final String testClassCode, final List<FailingTestDetailsRequestDTO> failingTestDetailsRequestDTOS) throws Exception;
}
