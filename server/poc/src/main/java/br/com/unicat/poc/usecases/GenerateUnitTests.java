package br.com.unicat.poc.usecases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
          String targetClassPackage,
          String targetClassCode,
          String testClassName,
          String testClassCode,
          String guidelines,
          String dependencies,
          String scenarios,
          String failedTestsAndErrors,
          String assertionLibrary
  ) {

//[ERROR] Tests run: 7, Failures: 3, Errors: 2, Skipped: 0, Time elapsed: 0.128 s <<< FAILURE! - in com.deckofcards.usecases.GetWinnerTest
//[ERROR] shouldThrowException_whenPlayersListIsNull  Time elapsed: 0.107 s  <<< FAILURE!
//org.opentest4j.AssertionFailedError: Unexpected exception type thrown ==> expected: <java.lang.IllegalArgumentException> but was: <java.lang.NullPointerException>
//	at com.deckofcards.usecases.GetWinnerTest.shouldThrowException_whenPlayersListIsNull(GetWinnerTest.java:111)
//Caused by: java.lang.NullPointerException: Cannot invoke "java.util.List.forEach(java.util.function.Consumer)" because "players" is null
//	at com.deckofcards.usecases.GetWinnerTest.lambda$shouldThrowException_whenPlayersListIsNull$0(GetWinnerTest.java:111)
//	at com.deckofcards.usecases.GetWinnerTest.shouldThrowException_whenPlayersListIsNull(GetWinnerTest.java:111)
//
//[ERROR] shouldReturnWinnerWithZeroPoints_whenPlayersHaveNoCards  Time elapsed: 0.003 s  <<< FAILURE!
//org.opentest4j.AssertionFailedError: expected: <Alice> but was: <Bob>
//	at com.deckofcards.usecases.GetWinnerTest.shouldReturnWinnerWithZeroPoints_whenPlayersHaveNoCards(GetWinnerTest.java:88)
//
//[ERROR] shouldReturnFirstPlayer_whenTieOnPoints  Time elapsed: 0.001 s  <<< FAILURE!
//org.opentest4j.AssertionFailedError: expected: <Alice> but was: <Bob>
//	at com.deckofcards.usecases.GetWinnerTest.shouldReturnFirstPlayer_whenTieOnPoints(GetWinnerTest.java:58)
//
//[ERROR] shouldReturnWinnerWithNulls_whenEmptyListProvided  Time elapsed: 0.002 s  <<< ERROR!
//java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.HashMap.get(Object)" is null
//	at com.deckofcards.usecases.GetWinnerTest.shouldReturnWinnerWithNulls_whenEmptyListProvided(GetWinnerTest.java:98)
//
//[ERROR] shouldReturnWinnerIgnoringInvalidCards  Time elapsed: 0.002 s  <<< ERROR!
//java.lang.IllegalArgumentException: No enum constant com.deckofcards.entities.enums.Points.invalidCard
//	at com.deckofcards.usecases.GetWinnerTest.shouldReturnWinnerIgnoringInvalidCards(GetWinnerTest.java:70)
//
//[ERROR] Failures:
//[ERROR]   GetWinnerTest.shouldReturnFirstPlayer_whenTieOnPoints:58 expected: <Alice> but was: <Bob>
//[ERROR]   GetWinnerTest.shouldReturnWinnerWithZeroPoints_whenPlayersHaveNoCards:88 expected: <Alice> but was: <Bob>
//[ERROR]   GetWinnerTest.shouldThrowException_whenPlayersListIsNull:111 Unexpected exception type thrown ==> expected: <java.lang.IllegalArgumentException> but was: <java.lang.NullPointerException>
//[ERROR] Errors:
//[ERROR]   GetWinnerTest.shouldReturnWinnerIgnoringInvalidCards:70 » IllegalArgument No e...
//[ERROR]   GetWinnerTest.shouldReturnWinnerWithNulls_whenEmptyListProvided:98 » NullPointer
//
//[ERROR] Tests run: 8, Failures: 3, Errors: 2, Skipped:

    return retryUnitTestsUseCase.run(
            targetClassName,
            targetClassPackage,
            targetClassCode,
            testClassName,
            testClassCode,
            guidelines,
            dependencies,
            scenarios,
            failedTestsAndErrors,
            assertionLibrary
    ).getText();
  }
}
