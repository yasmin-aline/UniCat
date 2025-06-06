package br.com.unicat.poc.adapter.http.dtos.response;

import java.util.List;

public record RefactoredUnitTestResponseDTO (List<RefactoredTestCodeResponseDTO> modifiedTestMethods, List<String> requiredNewImports) {}
