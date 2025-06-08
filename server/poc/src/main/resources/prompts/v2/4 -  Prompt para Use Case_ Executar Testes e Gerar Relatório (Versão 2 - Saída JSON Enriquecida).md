# Prompt para Use Case: Executar Testes e Gerar Relatório (Versão 2 - Saída JSON Enriquecida)

**Objetivo:** Este prompt descreve a ação que o sistema (ou o agente) deve realizar para executar os testes unitários gerados e coletar os resultados. Se houver testes falhos, o sistema deverá utilizar o Prompt 5 (StackTrace Interpreter) para enriquecer os detalhes das falhas antes de gerar o relatório final. A saída deste "prompt" é um JSON que será consumido pelo Prompt 6 (Correção de Testes Falhos).

**Instruções (para o Sistema/Agente):**

1.  **Compilação:** Compile a classe alvo, suas dependências e a classe de teste gerada.
2.  **Execução de Testes:** Execute todos os testes JUnit 5 presentes na classe de teste gerada.
3.  **Coleta de Resultados Preliminares:** Registre os resultados de cada teste:
    *   Testes que passaram.
    *   Testes que falharam (com nome do método, mensagem de erro e stack trace).
    *   Testes que resultaram em erro (exceções não capturadas).
4.  **Enriquecimento de Detalhes de Falha (se houver):**
    *   Se houver testes falhos, colete os `failed_details` preliminares (nome do método, mensagem de erro, stack trace).
    *   **Chame o Prompt 5 (StackTrace Interpreter)**, passando o `CODIGO_CLASSE_ALVO`, `CODIGOS_DEPENDENCIAS_JSON` e os `DETALHES_TESTES_FALHOS_JSON` preliminares.
    *   A saída do Prompt 5 será uma lista de objetos JSON enriquecidos para cada teste falho. Utilize esta saída para preencher o campo `failed_details` do relatório final.
5.  **Geração de Relatório de Cobertura:** Utilize uma ferramenta de cobertura de código (e.g., JaCoCo) para gerar um relatório de cobertura de linha para a classe alvo, com base na execução dos testes.
6.  **Formato JSON de Saída:** Estruture os resultados da execução e o relatório de cobertura EXCLUSIVAMENTE como um objeto JSON válido, conforme o esquema abaixo. Este JSON será a entrada para o Prompt 6.

**Estrutura JSON de Saída Esperada:**

```json
{
  "test_results": {
    "total_tests": <Número total de testes executados>,
    "passed_tests": <Número de testes que passaram>,
    "failed_tests": <Número de testes que falharam>,
    "error_tests": <Número de testes que resultaram em erro (exceção não capturada)>,
    "failed_details": [
      {
        "method_name": "<Nome do método de teste falho>",
        "error_summary": "<Resumo conciso do erro e sua causa raiz (vindo do Prompt 5)>",
        "detailed_analysis": "<Análise detalhada da lógica da classe alvo em relação ao cenário falho (vindo do Prompt 5)>",
        "stack_trace_explanation": "<Explicação detalhada dos erros do StackTrace (vindo do Prompt 5)>",
        "solution_steps": [
          "<Passo 1 para solucionar o problema (vindo do Prompt 5)>",
          "<Passo 2 para solucionar o problema (vindo do Prompt 5)>"
          // ... mais passos
        ]
      }
      // ... mais detalhes de testes falhos enriquecidos
    ]
  },
  "coverage_report": {
    "class_fqn": "<FQN da classe alvo>",
    "lines_total": <Número total de linhas executáveis na classe alvo>,
    "lines_covered": <Número de linhas cobertas pelos testes>,
    "lines_missed": [
      {"line": <Número da linha não coberta>, "reason": "<Breve descrição do porquê a linha não foi coberta>"}
      // ... mais linhas não cobertas
    ],
    "coverage_percentage": <Porcentagem de cobertura de linha>
  }
}
```


