package br.com.unicat.poc.usecases;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

@Service
public class GenerateUnitTests {
  private final AnalyseClassToTest analyseClassToTest;
  private final MapTestScenarios mapTestScenarios;
  private final IdentityMethodsAndDependencies identityMethodsAndDependencies;
  private final ImplementUnitTests implementUnitTests;

  public GenerateUnitTests(
      final AnalyseClassToTest analyseClassToTest,
      final MapTestScenarios mapTestScenarios,
      final IdentityMethodsAndDependencies identityMethodsAndDependencies,
      ImplementUnitTests implementUnitTests) {
    this.analyseClassToTest = analyseClassToTest;
    this.mapTestScenarios = mapTestScenarios;
    this.identityMethodsAndDependencies = identityMethodsAndDependencies;
    this.implementUnitTests = implementUnitTests;
  }

  public String run(
      final String targetClassName, final String targetClassCode, final String targetClassPackage) {
    // 1. [Prompt] Analisar profundamente a classe a ser testada [OK]
    final AssistantMessage analyzedClass =
        this.analyseClassToTest.run(targetClassName, targetClassCode, targetClassPackage);

    // 2. [Prompt] Mapear todos Cenários de Teste [OK]
    final AssistantMessage mappedTestScenarios =
        this.mapTestScenarios.run(analyzedClass, targetClassName, targetClassCode);

    // 3. [Prompt] Identificar Métodos e Dependências para cada Cenário de Teste [OK]
    final AssistantMessage identifiedMethodsAndDependencies =
        this.identityMethodsAndDependencies.run(
            mappedTestScenarios, targetClassName, targetClassCode);

    return identifiedMethodsAndDependencies.getText();
  }

  public String complete(
      final String targetClassName,
      final String targetClassCode,
      final String targetClassPackage,
      final String guidelines,
      final String dependencies,
      final String scenarios) {
    // 7. [Prompt] Responde com o a Implementação do Primeiro Cenário de Teste
    AssistantMessage ans =
        this.implementUnitTests.run(
            targetClassName,
            targetClassCode,
            targetClassPackage,
            guidelines,
            dependencies,
            scenarios);
    return ans.getText();
  }
}
