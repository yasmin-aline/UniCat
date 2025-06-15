package br.com.unicat.poc.v1.controller.dtos.request;

public record RetryRequestDTO(
    String targetClassName,
    String targetClassPackage,
    String targetClassCode,
    String testClassCode,
    String dependencies,
    String dependenciesName,
    String failingTestDetailsRequestDTOS,
    String testResults,
    String coverageReport,
    String attemptNumber) {}
