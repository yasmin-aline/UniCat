package br.com.unicat.poc.usecases;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class GenerateUnitTests {
  private final AnalyseClassToTest analyseClassToTest;
  private final MapTestScenarios mapTestScenarios;
  private final IdentityMethodsAndDependencies identityMethodsAndDependencies;
  private final ImplementUnitTests implementUnitTests;
  private final RetryUnitTestsUseCase retryUnitTestsUseCase;

  public GenerateUnitTests(
          final AnalyseClassToTest analyseClassToTest,
          final MapTestScenarios mapTestScenarios,
          final IdentityMethodsAndDependencies identityMethodsAndDependencies,
          ImplementUnitTests implementUnitTests, RetryUnitTestsUseCase retryUnitTestsUseCase) {
    this.analyseClassToTest = analyseClassToTest;
    this.mapTestScenarios = mapTestScenarios;
    this.identityMethodsAndDependencies = identityMethodsAndDependencies;
    this.implementUnitTests = implementUnitTests;
    this.retryUnitTestsUseCase = retryUnitTestsUseCase;
  }

  @PostConstruct
  public void testarRetryManual() {
    String targetClassName = "Calculadora";
    String targetClassCode = "public class Calculadora { public int somar(int a, int b) { return a + b; } }";
    String targetClassPackage = "com.exemplo";
    String guidelines = "Cobrir todos os métodos.";
    String dependencies = "";
    String scenarios = "Somar, Subtrair, Multiplicar, Dividir";
    List<String> testErrors = List.of(
            "somar: 12:expected:<5> but was:<4>",
            "dividir: 22:java.lang.ArithmeticException: / by zero"
    );

    String resposta = retry(
            targetClassName,
            targetClassCode,
            targetClassPackage,
            guidelines,
            dependencies,
            scenarios,
            testErrors
    );
    log.info("RESPOSTA RETRY MANUAL: " + resposta);
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
  public String retry(
          String targetClassName,
          String targetClassCode,
          String targetClassPackage,
          String guidelines,
          String dependencies,
          String scenarios,
          List<String> testErrors
  ) {
    return retryUnitTestsUseCase.run(
            targetClassName,
            targetClassCode,
            targetClassPackage,
            guidelines,
            dependencies,
            scenarios,
            testErrors
    ).getText();
  }
}
