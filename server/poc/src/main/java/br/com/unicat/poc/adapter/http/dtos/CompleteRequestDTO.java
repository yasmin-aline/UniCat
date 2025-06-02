package br.com.unicat.poc.adapter.http.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompleteRequestDTO {
  private final String targetClassName;
  private final String targetClassCode;
  private final String targetClassPackage;
  private final String guidelines;
  private final String dependencies;
  private final String scenarios;
}
