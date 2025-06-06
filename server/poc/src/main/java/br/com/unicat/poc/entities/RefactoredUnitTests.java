package br.com.unicat.poc.entities;

import java.util.List;

public record RefactoredUnitTests (List<RefactoredTestCode> modifiedTestMethods, List<String> requiredNewImports) {}