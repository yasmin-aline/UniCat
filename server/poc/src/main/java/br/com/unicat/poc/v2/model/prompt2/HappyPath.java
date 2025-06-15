package br.com.unicat.poc.v2.model.prompt2;

import java.util.List;

public record HappyPath(String testName, String description, Given given, List<MockSetup> mockSetup, ExpectedBehavior expectedBehavior) {
}
