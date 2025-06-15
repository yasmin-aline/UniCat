package br.com.unicat.poc.v1.entities;

import java.util.List;

public record RefactoredUnitTests(
    List<RefactoredTestCode> modifiedTestMethods, List<String> requiredNewImports) {}
