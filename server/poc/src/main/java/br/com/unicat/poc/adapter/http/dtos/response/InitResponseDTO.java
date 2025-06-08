package br.com.unicat.poc.adapter.http.dtos.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitResponseDTO {
  private AnalysisResponseDTO analysisResponseDTO;
  private List<String> customDependencies;
}
