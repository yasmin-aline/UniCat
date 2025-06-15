package br.com.unicat.poc.v1.adapter.http.dtos.request;

public record FailingTestDetailsRequestDTO(
    String methodName, String errorMessage, String stackTrace) {}
