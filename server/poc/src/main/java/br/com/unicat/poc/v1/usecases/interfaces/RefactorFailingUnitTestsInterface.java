package br.com.unicat.poc.v1.usecases.interfaces;

import br.com.unicat.poc.v1.controller.dtos.response.RefactoredUnitTestResponseDTO;
import br.com.unicat.poc.v1.entities.TestResults;

public interface RefactorFailingUnitTestsInterface {
  RefactoredUnitTestResponseDTO execute(
      final String dependenciesName,
      final String dependencies,
      final String testClassCode,
      final TestResults testResults,
      final String attemptNumber)
      throws Exception;
}
