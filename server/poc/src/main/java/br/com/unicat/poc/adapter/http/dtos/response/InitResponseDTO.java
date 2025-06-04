package br.com.unicat.poc.adapter.http.dtos.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitResponseDTO {
  private String classFqn;
  private String analysis_summary;
  private List<TestScenarioResponseDTO> testScenarios;
}
