package br.com.unicat.poc.v1.controller.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompleteResponseDTO {
  private String generatedTestClassFqn;
  private String generatedTestCode;
}
