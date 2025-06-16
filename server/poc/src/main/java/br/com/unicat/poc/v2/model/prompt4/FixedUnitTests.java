package br.com.unicat.poc.v2.model.prompt4;

import java.util.List;

public record FixedUnitTests(
    List<ModifiedTestMethods> modifiedTestMethods, List<String> requiredNewImports) {}
