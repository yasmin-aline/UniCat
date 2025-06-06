package br.com.unicat.poc.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedClass {
  private String generatedTestClassFqn;
  private String generatedTestCode;
}
