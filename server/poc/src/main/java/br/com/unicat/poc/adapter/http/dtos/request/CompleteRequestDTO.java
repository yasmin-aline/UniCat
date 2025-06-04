package br.com.unicat.poc.adapter.http.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompleteRequestDTO {
  private final String guidelines;
  private final String dependencies;
  private final String dependenciesName;
  private final String scenarios;
}
