package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.adapter.http.dtos.response.AnalysedLogicResponseDTO;
import br.com.unicat.poc.adapter.http.dtos.response.TestScenarioResponseDTO;
import br.com.unicat.poc.entities.AnalysedLogic;
import br.com.unicat.poc.prompts.AnalyseLogicAndIdentityScenariosPromptGenerator;
import br.com.unicat.poc.usecases.interfaces.AnalyseLogicAndIdentifyScenariosInterface;
import br.com.unicat.poc.usecases.utilities.JsonLlmResponseParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyseLogicAndIdentifyScenariosUseCase implements AnalyseLogicAndIdentifyScenariosInterface {
    private final AnalyseLogicAndIdentityScenariosPromptGenerator analyseLogicAndIdentityScenariosPromptGenerator;
    private final B3GPTGateway b3gptGateway;

    @Override
    public AnalysedLogicResponseDTO execute(final String dependenciesName, final String dependencies) throws Exception {
        final var prompt = this.analyseLogicAndIdentityScenariosPromptGenerator.get(dependencies, dependenciesName);
        final var chatResponse = this.b3gptGateway.callAPI(prompt);
        final var assistantMessage = chatResponse.getResult().getOutput();

        final AnalysedLogic analysedLogic = JsonLlmResponseParser.parseLlmResponse(assistantMessage, AnalysedLogic.class);

        return AnalysedLogicResponseDTO.builder()
            .classFqn(analysedLogic.getClassFqn())
            .analysisSummary(analysedLogic.getAnalysisSummary())
            .testScenarios(
                analysedLogic.getTestScenarios().stream()
                    .map(scenario -> TestScenarioResponseDTO.builder()
                        .id(scenario.getId())
                        .description(scenario.getDescription())
                        .expectedOutcomeType(scenario.getExpectedOutcomeType())
                        .build())
                    .toList()
            )
            .build();
    }
}
