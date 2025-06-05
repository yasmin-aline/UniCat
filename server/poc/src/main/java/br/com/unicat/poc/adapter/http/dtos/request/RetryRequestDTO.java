package br.com.unicat.poc.adapter.http.dtos.request;

import java.util.List;

public record RetryRequestDTO(
  String targetClassName,
  String targetClassPackage,
  String targetClassCode,
  String testClassCode,
  String dependencies,
  String dependenciesName,
  List<FailingTestDetailsRequestDTO> failingTestDetailsRequestDTOS
) {}