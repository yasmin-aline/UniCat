package br.com.unicat.poc.v1.controller.dtos.response;

import java.util.List;

public record RefactoredUnitTestResponseDTO(
    List<RefactoredTestCodeResponseDTO> modifiedTestMethods, List<String> requiredNewImports) {}
