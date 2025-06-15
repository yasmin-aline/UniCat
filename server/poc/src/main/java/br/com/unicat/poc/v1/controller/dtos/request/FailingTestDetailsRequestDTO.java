package br.com.unicat.poc.v1.controller.dtos.request;

public record FailingTestDetailsRequestDTO(
    String methodName, String errorMessage, String stackTrace) {}
