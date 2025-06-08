package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.entities.StacktraceInterpreted;
import java.util.List;

public interface StacktraceInterpreterInterface {
  List<StacktraceInterpreted> execute(
      String dependenciesName, String dependenciesCode, String failedTestsDetails) throws Exception;
}
