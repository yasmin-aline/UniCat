package br.com.unicat.poc.v1.controller.dtos.request;

public record TestResultsRequestDTO(
    String totalTests, String passedTests, String failedTests, String errorTests) {}
