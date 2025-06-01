package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.prompts.IdentifyDependenciesPromptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IdentityMethodsAndDependencies {
    private final IdentifyDependenciesPromptGenerator promptGenerator;
    private final B3GPTGateway b3gptGateway;

    // Markers expected in the mappedScenariosResponse text
    // Assuming scenarios come first, ending with a specific marker
    private static final String SCENARIOS_END_MARKER = "--- FIM DA LISTA DE CENÁRIOS ---";
    private static final String PUBLIC_METHODS_MARKER = "### Métodos Públicos";
    private static final String DEPENDENCIES_MARKER = "### Dependências que SERÃO Mockadas"; // Or a similar unique marker

    public IdentityMethodsAndDependencies(IdentifyDependenciesPromptGenerator promptGenerator, B3GPTGateway b3gptGateway) {
        this.promptGenerator = promptGenerator;
        this.b3gptGateway = b3gptGateway;
    }

    /**
     * Extracts scenarios, methods, and dependencies from the previous response,
     * generates a prompt to identify dependencies per scenario, calls the IA, and returns the result.
     *
     * @param mappedScenariosResponse The AssistantMessage containing mapped scenarios, methods, and dependencies.
     * @param targetClassName The name of the class being analyzed.
     * @param targetClassCode The source code of the class being analyzed.
     * @return AssistantMessage containing the identified dependencies per scenario or an error message.
     */
    public AssistantMessage run(final AssistantMessage mappedScenariosResponse, final String targetClassName, final String targetClassCode) {
        log.info("INIT IdentityMethodsAndDependencies run for class: {}", targetClassName);
        final String mappedScenariosResponseText = mappedScenariosResponse.getText();

        try {
            // 2. Generate the prompt using the extracted info and class details
            final Prompt prompt = this.promptGenerator.get(targetClassName, targetClassCode, mappedScenariosResponseText);
            log.info("Generated prompt for identifying dependencies per scenario.");
            log.info("Prompt text: {}", prompt.getInstructions());

            // 3. Call the B3GPTGateway to identify dependencies per scenario
            final ChatResponse response = this.b3gptGateway.callAPI(prompt);
            log.info("Received response from gateway for identifying dependencies.");

            // 4. Return the response as an AssistantMessage
            if (response != null && response.getResult() != null) {
                log.info("END IdentityMethodsAndDependencies run successfully for class: {}", response.getResult());
                return response.getResult().getOutput();
            } else {
                log.error("Received null or invalid response from gateway.");
                return new AssistantMessage("Error: Received invalid response from AI gateway while identifying dependencies per scenario.");
            }

        } catch (Exception ex) {
            log.error("ERROR during IdentityMethodsAndDependencies run for class {}: {}", targetClassName, ex.getMessage(), ex);
            return new AssistantMessage("Error occurred while identifying dependencies per scenario: " + ex.getMessage());
        }
    }

    /**
     * Extracts a section of text between a start marker and an end marker.
     * If startMarker is null, extracts from the beginning.
     * If endMarker is null, extracts until the end.
     *
     * @param text The full text to search within.
     * @param startMarker The text marking the beginning of the section (exclusive). Null to start from beginning.
     * @param endMarker The text marking the end of the section (exclusive). Null to go until end.
     * @return The extracted section text, trimmed, or an empty string if markers not found appropriately.
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        int startIndex = 0;
        if (startMarker != null) {
            startIndex = text.indexOf(startMarker);
            if (startIndex == -1) {
                return ""; // Start marker not found
            }
            startIndex += startMarker.length(); // Move past the marker
        }

        int endIndex = text.length();
        if (endMarker != null) {
            // Search for end marker *after* the start index
            endIndex = text.indexOf(endMarker, startIndex);
            if (endIndex == -1) {
                // If end marker is specified but not found after start index, 
                // maybe take till end? Or return empty? Let's take till end for now.
                endIndex = text.length();
            }
        }

        // Ensure startIndex is not beyond endIndex
        if (startIndex >= endIndex) {
            return "";
        }

        return text.substring(startIndex, endIndex).trim();
    }
}
