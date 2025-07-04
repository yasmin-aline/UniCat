package br.com.unicat.poc.entities;

import java.util.List;

public record FailedDetails(
    String methodName,
    String errorSummary,
    String detailedAnalysis,
    String stackTraceExplanation,
    List<String> solutionSteps) {}
