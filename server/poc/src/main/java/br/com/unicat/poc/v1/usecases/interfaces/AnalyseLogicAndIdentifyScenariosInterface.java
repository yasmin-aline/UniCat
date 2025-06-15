package br.com.unicat.poc.v1.usecases.interfaces;

import br.com.unicat.poc.v1.entities.AnalysedLogic;

public interface AnalyseLogicAndIdentifyScenariosInterface {
  AnalysedLogic execute(final String dependenciesName, final String dependencies) throws Exception;
}
