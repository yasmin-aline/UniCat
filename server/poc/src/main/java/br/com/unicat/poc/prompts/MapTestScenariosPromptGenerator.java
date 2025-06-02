package br.com.unicat.poc.prompts;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class MapTestScenariosPromptGenerator {

  public Prompt get(
      final String targetClassName,
      final String targetClassCode,
      final String publicMethodList,
      final String dependenciesList) {
    String prompt =
        String.format(
            """
            Tarefa: Mapear cenários de teste detalhados para os métodos públicos da classe Java fornecida, considerando as dependências que serão mockadas.

            Contexto:
            Classe: `%s`
            Código Fonte:
            ```java
            %s

            Métodos Públicos a Detalhar:

            %s


            Dependências que SERÃO Mockadas (Confirmado):

            %s


            Instruções:

            Para CADA método público listado acima, identifique e liste todos os cenários de teste relevantes que devem ser cobertos. Considere o comportamento esperado quando as dependências confirmadas são mockadas (ex: o que acontece se o mock de userRepository.findById retornar Optional.empty()? O que acontece se o mock de externalService.callApi lançar uma exceção?).

            Seja exaustivo e cubra:

            1. Caminho Feliz (Happy Path): Incluindo o comportamento esperado dos mocks para o sucesso.

            2.Cenários de Erro/Exceção:
                • Erros originados na própria lógica do método (ex: validação de entrada falha).
                • Erros/Exceções originadas pelo comportamento dos mocks (ex: mock lança exceção, mock retorna nulo/vazio quando não esperado).

            3. Edge Cases (Casos de Borda):
                • Entradas null, strings/coleções vazias, valores numéricos limite, etc.
                • Comportamento dos mocks em casos de borda (ex: mock retorna lista vazia).

            Mencione explicitamente o comportamento esperado dos mocks quando relevante para o cenário.

            **Após listar todos os cenários, inclua novamente as seções "### Métodos Públicos" e "### Dependências que SERÃO Mockadas", copiando exatamente as listas fornecidas no contexto.**

            Formato da Saída:
            Liste os cenários agrupados por método. Use uma formatação clara e consistente. Mencione explicitamente o comportamento esperado dos mocks quando relevante para o cenário.

            Exemplo de Saída Esperada:

            Método: findUserById(Long id)

            • Cenário 1 (Happy Path): ID válido fornecido, mock userRepository.findById(id) retorna Optional.of(user). Esperado: Retorna Optional.of(user).

            • Cenário 2 (Não Encontrado): ID válido fornecido, mock userRepository.findById(id) retorna Optional.empty(). Esperado: Retorna Optional.empty().

            • Cenário 3 (Erro - ID Nulo): ID é null. Esperado: Lança IllegalArgumentException.

            • Cenário 4 (Erro - ID Negativo): ID é -1L. Esperado: Lança IllegalArgumentException.

            • Cenário 5 (Erro - Mock Lança Exceção): Mock userRepository.findById(id) configurado para lançar DataAccessException. Esperado: Propaga DataAccessException (ou a trata, dependendo da lógica do método).

            Método: saveUser(User user)

            • Cenário 1 (Happy Path): User válido fornecido. Mock userRepository.save(user) retorna o mesmo user. Esperado: Retorna o user salvo, userRepository.save é chamado uma vez.

            • Cenário 2 (Erro - User Nulo): user é null. Esperado: Lança IllegalArgumentException.

            • ... (Finalize todos os cenários para cada método)

            --- FIM DA LISTA DE CENÁRIOS ---

            Métodos Públicos

            •public Optional<User> findUserById(Long id)
            •public User saveUser(User user)
            •public void someOtherMethod(com.example.dto.RequestData data, com.example.service.ExternalService service)

            Dependências que SERÃO Mockadas:

            • Campo: com.example.repository.UserRepository userRepository
            • Parâmetro (Construtor): com.example.config.AppConfig config
            • Parâmetro (Método someOtherMethod): com.example.service.ExternalService service
            • (Não listar Long id ou User user ou com.example.dto.RequestData data se forem considerados Modelos simples)

            """,
            targetClassName, targetClassCode, publicMethodList, dependenciesList);

    return new Prompt(prompt);
  }
}
