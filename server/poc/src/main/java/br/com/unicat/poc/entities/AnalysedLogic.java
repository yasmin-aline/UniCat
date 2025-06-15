package br.com.unicat.poc.entities;

import java.util.List;

public record AnalysedLogic(
    String classFqn, String analysisSummary, List<TestScenario> testScenarios) {}
