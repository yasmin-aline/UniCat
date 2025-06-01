package br.com.unicat.poc.usecases;

import org.springframework.stereotype.Service;

@Service
public class GenerateUnitTests {
    private final AnalyseAndMapClassUnitTestScenarios analyseAndMapClassUnitTestScenarios;

    public GenerateUnitTests(AnalyseAndMapClassUnitTestScenarios analyseAndMapClassUnitTestScenarios) {
        this.analyseAndMapClassUnitTestScenarios = analyseAndMapClassUnitTestScenarios;
    }

    public void run() {
        // 1. [Prompt] Analisar profundamente a classe a ser testada && Mapear todos Cenários de Teste
        // 3. [Prompt] Identificar Métodos e Dependências para cada Cenário de Teste
        // 4. [Plugin] Enviar os Métodos e Dependencias
        // 5. [Prompt] Solicita as Diretrizes para Criar os Testes
        // 6. [Plugin] Enviar Diretrizes
        // 7. [Prompt] Responde com o a Implementação do Primeiro Cenário de Teste
        // 8. [Plugin] Criar a Classe de Teste, Incluir cenário implementado e Rodar
        // if erro
        //  Enviar cenário e erro
        // else

        this.analyseAndMapClassUnitTestScenarios.run("", "");
    }

}
