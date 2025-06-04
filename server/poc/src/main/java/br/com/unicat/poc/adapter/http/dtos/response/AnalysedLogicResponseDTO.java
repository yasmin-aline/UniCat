package br.com.unicat.poc.adapter.http.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalysedLogicResponseDTO {
    private String classFqn;
    private String analysisSummary;
    private List<TestScenarioResponseDTO> testScenarios;
}
