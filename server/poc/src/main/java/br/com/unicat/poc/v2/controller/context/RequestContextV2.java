package br.com.unicat.poc.v2.controller.context;

public record RequestContextV2(
    String targetClassCode,
    String dependenciesCode,
    String guidelines,
    String testClassCode,
    String errors) {}
