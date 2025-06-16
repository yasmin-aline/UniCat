package br.com.unicat.poc.v2.model.prompt1;

import java.util.List;

public record MethodsAnalysis(
    String name, int cyclomaticComplexity, int estimatedScenarios, List<String> conditions) {}
