package br.com.unicat.poc.adapter.http.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryRequestDTO {
  private String targetClassName;
  private String targetClassPackage;
  private String targetClassCode;
  private String testClassName;
  private String testClassCode;
  private String guidelines;
  private String dependencies;
  private String scenarios;
  private String failedTestsAndErrors;
  private String assertionLibrary;
}
