package br.com.unicat.poc.prompts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class RetryUnitTestsPromptGenerator {

    public Prompt get(
            String targetClassName,
            String targetClassCode,
            String targetClassPackage,
            String guidelines,
            String dependencies,
            String scenarios,
            List<String> testErrors
    ) {
        StringBuilder errorsSection = new StringBuilder();
        if (testErrors != null && !testErrors.isEmpty()) {
            errorsSection.append("Erros encontrados nos testes:\n");
            for (String error : testErrors) {
                errorsSection.append("- ").append(error).append("\n");
            }
        }

        String prompt = String.format(
                """
                Contexto:
                Classe: %s
                Pacote: %s
                Código:
                %s

                Diretrizes: %s
                Dependências: %s
                Cenários: %s

                %s
                Tarefa:

1.Analisar a Classe: Receba a classe gerada e os resultados detalhados dos testes unitarios executados sobre ela, com foco especifico nos testes que falharam.

2.Identificar Causas Raizes: Investigue profundamente as falhas nos testes unitarios. Determine as causas raizes dos erros, que podem incluir logica incorreta na classe gerada, problemas de integracao com dependencias, casos de borda nao tratados, ou configuracoes inadequadas nos proprios testes.

3.Refatorar e Corrigir: Refatore a classe fornecida aplicando as melhores praticas de desenvolvimento. Corrija todos os erros que levaram as falhas nos testes unitarios. A refatoracao deve visar nao apenas a correcao dos bugs, mas tambem a melhoria da clareza, eficiencia e manutenibilidade do codigo. Garanta que a logica da classe permaneca consistente com seu proposito original, a menos que a correcao da falha exija uma modificacaoo logica justificada.

4.Garantir a Passagem dos Testes: Assegure-se de que, apos suas modificacoes, todos os testes unitarios originalmente fornecidos (incluindo os que falharam e os que passaram inicialmente) agora executem com sucesso. Se necessario, ajuste ligeiramente os testes para refletir a refatoracaoo (por exemplo, nomes de metodos atualizados), mas nao remova a logica de teste essencial.

5.Retornar o Codigo Refatorado: Apresente a versao final da classe, completamente refatorada e com todos os testes unitarios passando. O codigo retornado deve estar pronto para ser integrado ao sistema. Inclua comentarios concisos apenas onde for estritamente necessário para explicar lógicas complexas ou decisões de refatoração não óbvias.

Formato de Saida:\s
Retorne apenas o codigo Java completo para a classe de teste %sTest.java. Nao inclua nenhuma explicacao ou texto antes ou depois do bloco de codigo Java. O codigo deve estar pronto para ser compilado e executado.
                """,
                targetClassName,
                targetClassPackage,
                targetClassCode,
                guidelines,
                dependencies,
                scenarios,
                errorsSection.toString(),
                targetClassName
        );

        log.info("PROCESSING RetryUnitTestsPromptGenerator. prompt: {}", prompt);
        return new Prompt(prompt);
    }
}