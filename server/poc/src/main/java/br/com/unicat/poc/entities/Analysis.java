package br.com.unicat.poc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {
  private String classFqn;
  private String purposeSummary;
  private String mainMethodSignature;
  private String inputType;
  private String outputType;
}
