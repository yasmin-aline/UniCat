package br.com.unicat.poc.usecases.interfaces;

import br.com.unicat.poc.adapter.http.dtos.request.RetryRequestDTO;
import br.com.unicat.poc.entities.TestResults;

public interface StacktraceInterpreterInterface {
  TestResults execute(final RetryRequestDTO requestDTO) throws Exception;
}
