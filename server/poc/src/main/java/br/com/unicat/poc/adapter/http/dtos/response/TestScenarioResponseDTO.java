package br.com.unicat.poc.adapter.http.dtos.response;

public record TestScenarioResponseDTO(
        String id,
        String description,
        String expectedOutcomeType
) {}
