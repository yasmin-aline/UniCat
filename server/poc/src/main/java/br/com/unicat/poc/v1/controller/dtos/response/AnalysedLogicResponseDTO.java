package br.com.unicat.poc.v1.controller.dtos.response;

import java.util.List;

public record AnalysedLogicResponseDTO(
    String classFqn, String analysisSummary, List<TestScenarioResponseDTO> testScenarios) {}
