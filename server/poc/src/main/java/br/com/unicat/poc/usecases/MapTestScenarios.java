package br.com.unicat.poc.usecases;

import br.com.unicat.poc.adapter.gateway.B3GPTGateway;
import br.com.unicat.poc.prompts.MapTestScenariosPromptGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MapTestScenarios {
    private static final String PUBLIC_METHODS_MARKER = "Métodos Públicos";
    private static final String DEPENDENCIES_MARKER = "Dependências Potenciais para Mock";
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("^\s*-\s+`(.*)`", Pattern.MULTILINE);

    private final MapTestScenariosPromptGenerator promptGenerator;
    private final B3GPTGateway b3GPTGateway;

    public MapTestScenarios(MapTestScenariosPromptGenerator promptGenerator, B3GPTGateway b3GPTGateway) {
        this.promptGenerator = promptGenerator;
        this.b3GPTGateway = b3GPTGateway;
    }

    /**
     * Extracts methods/dependencies from the analysis, generates a prompt, calls the IA to map test scenarios, and returns the result.
     *
     * @param analyzedClass The AssistantMessage containing the previous analysis text.
     * @param targetClassName The name of the class being analyzed.
     * @param targetClassCode The source code of the class being analyzed.
     * @return AssistantMessage containing the mapped test scenarios or an error message.
     */
    public AssistantMessage run(AssistantMessage analyzedClass, String targetClassName, String targetClassCode) {
        log.info("INIT MapTestScenarios run for class: {}", targetClassName);
        final String analysisText = analyzedClass.getText(); // Use getContent() for AssistantMessage

        // 1. Extract Methods and Dependencies from analysisText
        assert analysisText != null;
        String publicMethodsSection = extractSection(analysisText, PUBLIC_METHODS_MARKER, DEPENDENCIES_MARKER);
        String dependenciesSection = extractSection(analysisText, DEPENDENCIES_MARKER, null); // Goes until the end or next major section

        log.info("Extracted Public Methods:\n{}", publicMethodsSection);
        log.info("Extracted Dependencies:\n{}", dependenciesSection);

        try {
            // 2. Generate the prompt using the extracted info and class details
            final Prompt prompt = this.promptGenerator.get(targetClassName, targetClassCode, publicMethodsSection, dependenciesSection);
            log.info("Generated prompt for mapping test scenarios.");
            log.info("Prompt text: {}", prompt.getInstructions());

            // 3. Call the B3GPTGateway (or similar) to get the test scenarios
            final ChatResponse response = this.b3GPTGateway.callAPI(prompt);
            log.info("Received response from gateway for test scenarios mapping.");

            // 4. Return the response as an AssistantMessage
            // Assuming the gateway returns a valid ChatResponse
            if (response != null && response.getResult() != null) {
                log.info("END MapTestScenarios run successfully for class: {}", response.getResult().getOutput());
                // Return the AssistantMessage from the response
                return response.getResult().getOutput();
            } else {
                log.error("Received null or invalid response from gateway.");
                return new AssistantMessage("Error: Received invalid response from AI gateway while mapping test scenarios.");
            }

        } catch (Exception ex) {
            log.error("ERROR during MapTestScenarios run for class {}: {}", targetClassName, ex.getMessage(), ex);
            return new AssistantMessage("Error occurred while mapping test scenarios: " + ex.getMessage());
        }
    }

    /**
     * Extracts a section of text between a start marker and an end marker.
     *
     * @param text The full text to search within.
     * @param startMarker The text marking the beginning of the section.
     * @param endMarker The text marking the end of the section (optional, goes to end if null).
     * @return The extracted section text, or an empty string if not found.
     */
    private String extractSection(String text, String startMarker, String endMarker) {
        int startIndex = text.indexOf(startMarker);
        if (startIndex == -1) {
            return ""; // Start marker not found
        }
        startIndex += startMarker.length(); // Move past the marker

        int endIndex = -1;
        if (endMarker != null) {
            endIndex = text.indexOf(endMarker, startIndex);
        }

        if (endIndex == -1) {
            // If end marker is null or not found, take text until the end
            return text.substring(startIndex).trim();
        } else {
            return text.substring(startIndex, endIndex).trim();
        }
    }
}
