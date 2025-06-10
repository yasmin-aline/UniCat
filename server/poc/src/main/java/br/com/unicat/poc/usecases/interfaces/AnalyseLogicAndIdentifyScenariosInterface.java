package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.entities.AnalysedLogic;

public interface AnalyseLogicAndIdentifyScenariosInterface {
  AnalysedLogic execute(final String dependenciesName, final String dependencies) throws Exception;
}
