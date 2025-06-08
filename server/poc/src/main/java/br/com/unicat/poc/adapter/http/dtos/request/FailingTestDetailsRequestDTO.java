package br.com.unicat.poc.adapter.http.dtos.request;

public record FailingTestDetailsRequestDTO(
    String methodName, String errorMessage, String stackTrace) {}
