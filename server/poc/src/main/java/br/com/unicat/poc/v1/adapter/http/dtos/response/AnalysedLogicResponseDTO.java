package br.com.unicat.poc.v1.adapter.http.dtos.response;

import java.util.List;

public record AnalysedLogicResponseDTO(
    String classFqn, String analysisSummary, List<TestScenarioResponseDTO> testScenarios) {}
