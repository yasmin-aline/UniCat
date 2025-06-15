package br.com.unicat.poc.v1.entities;

import java.util.List;

public record AnalysedLogic(
    String classFqn, String analysisSummary, List<TestScenario> testScenarios) {}
