package br.com.unicat.poc.v1.controller.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalysisResponseDTO {
  private String classFqn;
  private String purposeSummary;
  private String mainMethodSignature;
  private String inputType;
  private String outputType;
}
