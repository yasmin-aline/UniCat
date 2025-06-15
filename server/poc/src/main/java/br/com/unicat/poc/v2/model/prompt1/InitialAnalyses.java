package br.com.unicat.poc.v2.model.prompt1;

import java.util.List;

public record InitialAnalyses(List<Dependencies> dependencies, List<PrivateMethods> privateMethods, List<MethodsAnalysis> methodsAnalyses) {
}
