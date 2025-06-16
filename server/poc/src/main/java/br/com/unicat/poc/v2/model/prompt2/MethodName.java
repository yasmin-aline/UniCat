package br.com.unicat.poc.v2.model.prompt2;

import java.util.List;

public record MethodName(
    String method,
    List<String> privateMethods,
    Scenarios scenarios,
    List<String> edgeCases,
    List<String> exceptionCases) {}
