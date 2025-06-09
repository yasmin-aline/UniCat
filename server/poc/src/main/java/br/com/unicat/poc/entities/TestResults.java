package br.com.unicat.poc.entities;

import java.util.List;

public record TestResults(
        String totalTestes,
        String passedTests,
        String failedTests,
        String errorTests,
        List<FailedDetails> failedDetails,
        CoverageReport coverageReport
){}
