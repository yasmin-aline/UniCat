package br.com.unicat.poc.usecases;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

@Service
public class GenerateUnitTests {
    private final AnalyseClassToTest analyseClassToTest;
    private final MapTestScenarios mapTestScenarios;
    private final IdentityMethodsAndDependencies identityMethodsAndDependencies;

    public GenerateUnitTests(AnalyseClassToTest analyseClassToTest, MapTestScenarios mapTestScenarios, IdentityMethodsAndDependencies identityMethodsAndDependencies) {
        this.analyseClassToTest = analyseClassToTest;
        this.mapTestScenarios = mapTestScenarios;
        this.identityMethodsAndDependencies = identityMethodsAndDependencies;
    }

    public void run(final String targetClassName, final String targetClassCode, final String targetClassPackage) {
        // 1. [Prompt] Analisar profundamente a classe a ser testada [OK]
        // 2. [Prompt] Mapear todos Cenários de Teste [OK]
        // 3. [Prompt] Identificar Métodos e Dependências para cada Cenário de Teste
        // 4. [Plugin] Enviar as Dependencias Solicitadas
        // 5. [Prompt] Solicita as Diretrizes para Criar os Testes
        // 6. [Plugin] Enviar Diretrizes
        // 7. [Prompt] Responde com o a Implementação do Primeiro Cenário de Teste
        // 8. [Plugin] Criar a Classe de Teste, Incluir cenário implementado e Rodar
        // if erro
        //  Enviar cenário e erro
        // else

        AssistantMessage analyzedClass = this.analyseClassToTest.run(targetClassName, targetClassCode, targetClassPackage);
        AssistantMessage mappedTestScenarios = this.mapTestScenarios.run(analyzedClass, targetClassName, targetClassCode);
        AssistantMessage identifiedMethodsAndDependencies = this.identityMethodsAndDependencies.run(mappedTestScenarios, targetClassName, targetClassCode);

        // 4. Recebe lista de dependencias
        // Para cada elemento na lista, procurar o seu arquivo java no projeto
        // Copiar o código completo do arquivo
        // E enviar para a LLM
    }

}
