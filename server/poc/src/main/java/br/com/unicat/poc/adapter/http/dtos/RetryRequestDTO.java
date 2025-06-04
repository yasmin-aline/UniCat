package br.com.unicat.poc.adapter.http.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

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