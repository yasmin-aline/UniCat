package br.com.unicat.poc.adapter.http;

import br.com.unicat.poc.usecases.GenerateUnitTests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UniCatController {
    private final GenerateUnitTests generateUnitTests;

    public UniCatController(GenerateUnitTests generateUnitTests) {
        this.generateUnitTests = generateUnitTests;
    }

    @GetMapping(path = "/unicat")
    String generation() {
        return this.generateUnitTests.run("GetWinnerTest" , "import com.deckofcards.adapter.http.dto.request.PlayerRequestDTO;\n" +
                "import com.deckofcards.adapter.http.dto.response.WinnerPlayerResponseDTO;\n" +
                "import com.deckofcards.entities.enums.Points;\n" +
                "import lombok.AllArgsConstructor;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "import java.util.*;\n" +
                "import java.util.concurrent.atomic.AtomicInteger;\n" +
                "\n" +
                "@Service\n" +
                "@AllArgsConstructor\n" +
                "public class GetWinner {\n" +
                "\n" +
                "\n" +
                "    public WinnerPlayerResponseDTO execute(final List<PlayerRequestDTO> players) {\n" +
                "        var playersPoints = new HashMap<String, Integer>();\n" +
                "\n" +
                "        players.forEach(player -> {\n" +
                "            AtomicInteger sum = new AtomicInteger();\n" +
                "            player.getCards().forEach(card ->{\n" +
                "                var points = 0;\n" +
                "\n" +
                "                try {\n" +
                "                    points += Integer.parseInt(card);\n" +
                "                } catch (Exception e) {\n" +
                "                    points += Points.valueOf(card).getPoint();\n" +
                "                }\n" +
                "\n" +
                "                sum.addAndGet(points);\n" +
                "            });\n" +
                "\n" +
                "            playersPoints.put(player.getName(), sum.get());\n" +
                "        });\n" +
                "\n" +
                "        String winner = getPlayerWithHighestPoints(playersPoints);\n" +
                "        int points = playersPoints.get(winner);\n" +
                "        return new WinnerPlayerResponseDTO(winner, points);\n" +
                "    }\n" +
                "\n" +
                "    private <K, V extends Comparable<V>> K getPlayerWithHighestPoints(HashMap<K, V> playersPoints) {\n" +
                "        Optional<Map.Entry<K, V>> winner = playersPoints.entrySet()\n" +
                "                .stream()\n" +
                "                .max(Map.Entry.comparingByValue());\n" +
                "\n" +
                "        return winner.map(Map.Entry::getKey).orElse(null);\n" +
                "    }\n" +
                "}", "com.deckofcards.usecases" );
    }
}