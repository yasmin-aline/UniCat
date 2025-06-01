package br.com.unicat.poc.usecases;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

@Service
public class MapTestScenarios {
    public AssistantMessage run(AssistantMessage analyzedClass) {
        return analyzedClass;
    }

    // 2. [Prompt] Mapear todos Cenários de Teste

}
