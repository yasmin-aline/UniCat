package br.com.unicat.poc.adapter.http.dtos.request;

public record TestResultsRequestDTO(String totalTests, String passedTests, String failedTests, String errorTests) {
}
