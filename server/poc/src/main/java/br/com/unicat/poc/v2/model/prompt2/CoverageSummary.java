package br.com.unicat.poc.v2.model.prompt2;

public record CoverageSummary(int totalLines, int coveredLines, int percentage, int uncoveredLines) {
}
