package br.com.unicat.poc.adapter.http.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InitResponseDTO {
  private AnalysisResponseDTO analysisResponseDTO;
  private List<String> customDependencies;
}
