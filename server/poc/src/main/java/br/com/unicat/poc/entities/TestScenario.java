package br.com.unicat.poc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestScenario {
  private String id;
  private String description;
  private String expectedOutcomeType;
}
