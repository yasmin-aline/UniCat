package br.com.unicat.poc.v1.usecases.interfaces;

import br.com.unicat.poc.v1.adapter.http.dtos.request.RetryRequestDTO;
import br.com.unicat.poc.v1.entities.TestResults;

public interface StacktraceInterpreterInterface {
  TestResults execute(final RetryRequestDTO requestDTO) throws Exception;
}
