package br.com.unicat.poc.v2.model.prompt1;

import java.util.List;

public record PrivateMethods(String name, List<String> parameters, boolean needsReflection) {
}
