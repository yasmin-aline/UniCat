package br.com.unicat.poc.adapter.http.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestScenarioResponseDTO {
  private String id;
  private String description;
  private String expectedOutcomeType;
}
