package br.com.unicat.poc.v1.controller.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RequestContext {
  private String targetClassName;
  private String targetClassCode;
  private String targetClassPackage;
}
