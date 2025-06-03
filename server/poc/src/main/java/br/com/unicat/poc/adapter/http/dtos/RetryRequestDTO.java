// server/poc/src/main/java/br/com/unicat/poc/adapter/http/dtos/RetryRequestDTO.java
package br.com.unicat.poc.adapter.http.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetryRequestDTO {
    private String targetClassName;
    private String targetClassCode;
    private String targetClassPackage;
    private String guidelines;
    private String dependencies;
    private String scenarios;
    private List<String> testErrors;
}