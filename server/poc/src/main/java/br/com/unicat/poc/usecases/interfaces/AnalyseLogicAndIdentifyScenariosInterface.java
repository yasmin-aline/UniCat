package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.response.AnalysedLogicResponseDTO;

public interface AnalyseLogicAndIdentifyScenariosInterface {
    AnalysedLogicResponseDTO execute(final String dependenciesName, final String dependencies) throws Exception;
}
