package br.com.unicat.poc.v2.model.prompt2;

import java.util.List;

public record ExpectedBehavior(
    String returning, List<String> verifications, List<String> coverage) {}
