package br.com.unicat.poc.v1.entities;

import java.util.List;

public record TestScenario(
    String id, String description, String expectedOutcomeType, List<MockBehavior> mockBehavior, List<String> inputParameters) {}
