package br.com.unicat.poc.v2.model.prompt2;

public record DesignedTestScenarios(
    TestClass testClass, TestScenarios testScenarios, CoverageSummary coverageSummary) {}
