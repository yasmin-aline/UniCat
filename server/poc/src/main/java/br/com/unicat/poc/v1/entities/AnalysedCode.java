package br.com.unicat.poc.v1.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysedCode {
  private Analysis analysis;
  private List<String> customDependencies;
}
