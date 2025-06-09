package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.response.RefactoredUnitTestResponseDTO;
import br.com.unicat.poc.entities.TestResults;

public interface RefactorFailingUnitTestsInterface {
  RefactoredUnitTestResponseDTO execute(
      final String dependenciesName,
      final String dependencies,
      final String testClassCode,
      final TestResults testResults)
      throws Exception;
}
