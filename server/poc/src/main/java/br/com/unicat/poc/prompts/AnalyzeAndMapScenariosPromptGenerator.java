package br.com.unicat.poc.prompts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnalyzeAndMapScenariosPromptGenerator {

    public Prompt get(final String targetClassName, final String targetClassPackage, final String targetClassToTest) {
        final String prompt = String.format("""
            Tarefa: Analisar a classe Java fornecida, identificar seus métodos públicos, listar dependências potenciais para testes unitários.
            
            Contexto:
            Classe: `%s`
            Pacote: `%s`
            Código Fonte:
            ```java
            %s
            
            Instruções:
            
            1.Identifique e liste TODOS os métodos públicos desta classe, incluindo suas assinaturas completas (modificadores, tipo de retorno, nome, parâmetros com tipos).
            
            2.Identifique e liste TODAS as dependências externas potenciais que esta classe utiliza e que provavelmente precisariam ser mockadas em testes unitários. Considere:
            •Campos (fields) da classe (especialmente os injetados via construtor ou anotações).
            •Tipos de parâmetros nos construtores.
            •Tipos de parâmetros em métodos públicos que não sejam tipos primitivos, wrappers comuns (String, Integer, etc.) ou coleções de tipos primitivos/wrappers.
            •Tipos de retorno complexos que possam indicar interação com outros componentes.
            •Ignore tipos do próprio JDK (java.lang., java.util. etc.) a menos que sejam interfaces complexas como DataSource.
            •Liste cada dependência potencial com seu tipo completo (incluindo pacote) e o nome do campo/parâmetro onde ela aparece.
            
            Formato da Saída:
            Estruture a resposta claramente em duas seções: "Métodos Públicos" e "Dependências Potenciais para Mock".
            
            Exemplo de Saída Esperada:
            
            Métodos Públicos
            
            •public Optional<User> findUserById(Long id)
            
            •public User saveUser(User user)
            
            •public void someOtherMethod(com.example.dto.RequestData data, com.example.service.ExternalService service)
            
            Dependências Potenciais para Mock
            
            • Campo: com.example.repository.UserRepository userRepository
            • Parâmetro (Construtor): com.example.config.AppConfig config
            • Parâmetro (Método someOtherMethod): com.example.service.ExternalService service
            • (Não listar Long id ou User user ou com.example.dto.RequestData data se forem considerados DTOs/Modelos simples)
            
            --- FIM DA ANÁLISE ---
            """, targetClassName, targetClassPackage, targetClassToTest);

        log.info("PROCESSING promptGenerator. prompt: {}", prompt);
        return new Prompt(prompt);
    }

}
