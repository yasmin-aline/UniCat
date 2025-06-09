package br.com.unicat.poc.entities;

import java.util.List;

public record CoverageReport(String classFqn, String linesTotal, String linesCovered, List<LinesMissed> linesMissed) {
}
