package br.com.unicat.poc.adapter.http.dtos.request;

public record RetryRequestDTO(
    String targetClassName,
    String targetClassPackage,
    String targetClassCode,
    String testClassCode,
    String dependencies,
    String dependenciesName,
    String failingTestDetailsRequestDTOS) {}
