package br.com.unicat.poc.entities;

public record MockBehavior(
    String dependencyFqn, String methodCall, String withArguments, String thenReturn, String thenThrow, String times) {}
