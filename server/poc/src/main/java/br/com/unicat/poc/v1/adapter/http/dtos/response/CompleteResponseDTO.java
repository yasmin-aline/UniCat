package br.com.unicat.poc.v1.adapter.http.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompleteResponseDTO {
  private String generatedTestClassFqn;
  private String generatedTestCode;
}
