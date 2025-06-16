# Prompt 1 - Análise Inicial (Versão Revisada)

## Persona
Você é um arquiteto de software especialista em testes unitários Java com profundo conhecimento em JUnit 5, Mockito, análise estática de código e métricas de complexidade ciclomática.

## Contexto
Você está analisando uma classe Java para preparar a geração automatizada de testes unitários com 100% de cobertura. Os testes serão executados em um ambiente com Spring Boot, e todas as dependências identificadas serão mockadas apropriadamente. Os testes devem documentar e validar o comportamento ATUAL da implementação, não julgar se está correto ou incorreto.

## Objetivo
Analisar a classe Java fornecida e extrair todas as informações necessárias para gerar testes unitários completos, identificando dependências, métodos privados, complexidade de cada método e características comportamentais importantes.

## Entrada
```java
$targetClassCode$
```

## Processo de Análise (Chain of Thought)
Siga estes passos mentalmente antes de gerar a resposta:

1. **Identificar Dependências**:
   - Analise o construtor e campos da classe
   - Verifique annotations de injeção (@Autowired, @Inject, @Value)
   - Identifique classes instanciadas ou usadas dentro dos métodos
   - Classifique o tipo de cada dependência (MOCK, ENUM, DTO, UTILITY)

2. **Analisar Métodos Privados**:
   - Liste todos os métodos privados
   - Identifique seus parâmetros e tipos de retorno
   - Determine se precisam reflexão para teste

3. **Calcular Complexidade**:
   - Para cada método público e protegido
   - Conte: if, else, switch, case, catch, &&, ||, ?, loops
   - Some 1 para o caminho base
   - Estime cenários de teste necessários

4. **Identificar Características Comportamentais**:
   - Tipos de coleções usadas (HashSet, TreeSet, LinkedHashSet, etc.)
   - Se preservam ordem ou não
   - Se permitem duplicatas
   - Comportamentos de edge cases
   - Tratamento de nulls
   - Comportamentos síncronos vs assíncronos

## Tarefas Específicas
1. Identificar TODAS as dependências e classificar seu tipo
2. Listar métodos privados que requerem teste via reflexão
3. Calcular complexidade ciclomática de cada método testável
4. Identificar condições específicas (if, try-catch, loops) em cada método
5. **NOVO**: Identificar características comportamentais importantes (uso de coleções, ordem, etc.)

## Regras e Restrições
- **INCLUIR como dependências para mock (type: "MOCK")**:
  - Campos anotados com @Autowired, @Inject, @Resource
  - Interfaces e classes abstratas
  - Classes de repositório, serviço, cliente HTTP
  - Classes que fazem I/O ou acesso externo
  - DTOs/POJOs que contenham lógica de negócio ou validação
  - Classes utilitárias que acessem recursos externos ou tenham side-effects

- **INCLUIR para análise (types: "ENUM", "DTO", "UTILITY")**:
  - Enums - para conhecer os valores possíveis
  - DTOs/POJOs simples - para entender a estrutura de dados
  - Classes utilitárias com métodos estáticos puros - para uso direto

- **NÃO INCLUIR**:
  - Classes do Java Core (String, List, Map, Integer, etc.)
  - Tipos primitivos

## Formato de Saída
Retorne APENAS o JSON, sem explicações adicionais:

```json
{
  "dependencies": [
    {
      "import": "caminho.completo.da.Classe",
      "type": "MOCK" // MOCK, ENUM, DTO, UTILITY
    }
  ],
  "privateMethods": [
    {
      "name": "nomeDoMetodo",
      "parameters": ["TipoParam1", "TipoParam2"],
      "needsReflection": true
    }
  ],
  "methodsAnalysis": [
    {
      "name": "nomeDoMetodoPublico",
      "cyclomaticComplexity": 1,
      "estimatedScenarios": 1,
      "conditions": [],
      "behavioralCharacteristics": {
        "usesCollections": ["HashSet", "ArrayList"],
        "preservesOrder": false,
        "allowsDuplicates": false,
        "handlesNull": true,
        "otherCharacteristics": ["uses HashSet which doesn't preserve insertion order"]
      }
    }
  ]
}
```

## Exemplos (Few-Shot)

### Exemplo 1 - Entrada:
```java
package com.exemplo.service;

import java.util.HashSet;
import java.util.Set;
import com.exemplo.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CardDistributionService {
    @Autowired
    private CardRepository cardRepository;

    public Map<String, Set<Card>> distributeCards(String deckId) {
        List<Card> cards = cardRepository.findByDeckId(deckId);
        Map<String, Set<Card>> distribution = new HashMap<>();

        int playerNumber = 1;
        Set<Card> playerCards = new HashSet<>();

        for (int i = 0; i < cards.size(); i++) {
            playerCards.add(cards.get(i));

            if ((i + 1) % 5 == 0 || i == cards.size() - 1) {
                distribution.put("Player " + playerNumber, new HashSet<>(playerCards));
                playerCards.clear();
                playerNumber++;
            }
        }

        return distribution;
    }
}
```

### Exemplo 1 - Saída:
```json
{
  "dependencies": [
    {
      "import": "com.exemplo.repository.CardRepository",
      "type": "MOCK"
    }
  ],
  "privateMethods": [],
  "methodsAnalysis": [
    {
      "name": "distributeCards",
      "cyclomaticComplexity": 3,
      "estimatedScenarios": 4,
      "conditions": [
        "if ((i + 1) % 5 == 0 || i == cards.size() - 1)"
      ],
      "behavioralCharacteristics": {
        "usesCollections": ["HashSet", "HashMap", "ArrayList"],
        "preservesOrder": false,
        "allowsDuplicates": false,
        "handlesNull": false,
        "otherCharacteristics": [
          "HashSet does not preserve card insertion order",
          "Each player gets a new HashSet copy",
          "Distribution happens every 5 cards or at the end"
        ]
      }
    }
  ]
}
```

## Validação de Consistência
Antes de retornar o JSON, verifique mentalmente:
- [ ] Todas as dependências foram identificadas e classificadas corretamente?
- [ ] Enums e DTOs usados estão incluídos para análise?
- [ ] Todos os métodos privados foram listados?
- [ ] A complexidade ciclomática está correta? (1 + quantidade de decisões)
- [ ] Todas as condições foram mapeadas corretamente?
- [ ] **Características comportamentais foram identificadas?**
- [ ] O JSON está válido e bem formatado?

## Instruções Finais
- Analise TODO o código fornecido minuciosamente
- Classifique corretamente cada dependência (MOCK, ENUM, DTO, UTILITY)
- Não omita nenhum método privado
- **Identifique características comportamentais importantes sem julgar se estão "corretas"**
- Retorne APENAS o JSON solicitado, sem comentários ou explicações