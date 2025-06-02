package br.com.unicat.poc.prompts;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class IdentifyDependenciesPromptGenerator {
    public Prompt get(final String targetClassName, final String targetClassCode, final String lastAnswer) {
        String prompt = String.format("""
            Tarefa: Analisar os cenários de teste mapeados e identificar os mocks relevantes para cada um.
            
            Contexto:
            Classe Original: `%s`
            Código Fonte (para referência):
            ```java
            %s
            ```
            
            %s
            
            Instruções:
            Analise a lista de "Cenários de Teste Mapeados". Para CADA cenário individual listado:
            
            1. Identifique o Método Sob Teste (o método da classe %s que este cenário visa testar).
            
            2. Identifique quais das Dependências Confirmadas para Mocking precisariam ter seu comportamento configurado especificamente (usando when().thenReturn(), doThrow(), verify(), etc.) para implementar e verificar este cenário em particular.
            
            3. Ao finalizar a análise de mocks por cenário, crie uma lista de <key, value> onde a key será a entidade (classe) a ser mockada e o value o package que está pertence.
            
            Formato da Saída:
            Liste os resultados agrupados por cenário. Para cada cenário, indique o método sob teste e os mocks relevantes para a configuração daquele cenário.
            
            Exemplo de Saída Esperada:
            
            Análise de Mocks por Cenário
            
            Cenário: (Happy Path - findUserById) ID válido fornecido, mock userRepository.findById(id) retorna Optional.of(user). Esperado: Retorna Optional.of(user).
            
            • Método Sob Teste: findUserById(Long id)
            • Mocks Relevantes para Configurar: userRepository (precisa configurar when(findById).thenReturn(Optional.of(user)) e verify(findById)).
            
            Cenário: (Não Encontrado - findUserById) ID válido fornecido, mock userRepository.findById(id) retorna Optional.empty(). Esperado: Retorna Optional.empty().
            
            • Método Sob Teste: findUserById(Long id)
            • Mocks Relevantes para Configurar: userRepository (precisa configurar when(findById).thenReturn(Optional.empty()) e verify(findById)).
            
            Cenário: (Erro - ID Nulo - findUserById) ID é null. Esperado: Lança IllegalArgumentException.
            
            • Método Sob Teste: findUserById(Long id)
            • Mocks Relevantes para Configurar: Nenhum (o erro ocorre antes da interação com mocks).
            
            Cenário: (Happy Path - saveUser) User válido fornecido. Mock userRepository.save(user) retorna o mesmo user. Esperado: Retorna o user salvo, userRepository.save é chamado uma vez.
            
            • Método Sob Teste: saveUser(User user)
            • Mocks Relevantes para Configurar: userRepository (precisa configurar when(save).thenReturn(user) e verify(save)).
            
            Cenário: (Erro - Mock Lança Exceção - saveUser) Mock userRepository.save(user) configurado para lançar DataAccessException. Esperado: Propaga DataAccessException.
            
            • Método Sob Teste: saveUser(User user)
            
            • Mocks Relevantes para Configurar: userRepository (precisa configurar when(save).thenThrow(DataAccessException.class)).
            
            ... (Finalize para todos os cenários)
            
            --- FIM DA ANÁLISE DE MOCKS POR CENÁRIO ---
            
            Lista de Mocks
            - UserRepository: br.com.example.repository.UserRepository
            - User: br.com.example.model.User
            - DataAccessException: br.com.example.exception.DataAccessException
            
            """, targetClassName, targetClassCode, lastAnswer, targetClassName);

        return new Prompt(prompt);
    }

}
