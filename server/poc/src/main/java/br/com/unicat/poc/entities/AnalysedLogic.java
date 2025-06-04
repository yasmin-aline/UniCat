package br.com.unicat.poc.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysedLogic {
  private String classFqn;
  private String analysis_summary;
  private List<TestScenario> testScenarios;
}
