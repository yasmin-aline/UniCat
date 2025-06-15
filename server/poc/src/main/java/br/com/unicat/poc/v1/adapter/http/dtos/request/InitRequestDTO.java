package br.com.unicat.poc.v1.adapter.http.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InitRequestDTO {
  private final String targetClassName;
  private final String targetClassCode;
  private final String targetClassPackage;
}
