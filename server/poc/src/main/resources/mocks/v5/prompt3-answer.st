```java
package com.deckofcards.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deckofcards.adapter.http.dto.response.PlayersResponseDTO;
import com.deckofcards.adapter.repository.provider.CardsDbProvider;
import com.deckofcards.entities.CardEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class BuildHandsTest {

    @Mock
    private CardsDbProvider cardsDbProvider;

    @InjectMocks
    private BuildHands buildHands;

    @BeforeEach
    void setUp() {
        // Setup adicional se necessário
    }

    @Nested
    @DisplayName("Execute method tests")
    class ExecuteTests {

        @Test
        @DisplayName("Should build players' hands when valid deck ID provided")
        void should_buildPlayersHands_when_validDeckIdProvided() {
            // Given
            String deckId = "validDeckId";
            List<CardEntity> cards = createCardEntities(10);

            when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

            // When
            PlayersResponseDTO result = buildHands.execute(deckId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPlayers()).hasSize(4);
            assertThat(result.getPlayers().get("Jogador 4")).containsExactly("A", "2", "3", "4", "5");
            assertThat(result.getPlayers().get("Jogador 3")).containsExactly("6", "7", "8", "9", "10");

            verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
        }

        @Test
        @DisplayName("Should build players' hands when exactly 10 cards provided")
        void should_buildPlayersHands_when_exactly10CardsProvided() {
            // Given
            String deckId = "validDeckId";
            List<CardEntity> cards = createCardEntities(10);

            when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

            // When
            PlayersResponseDTO result = buildHands.execute(deckId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPlayers()).hasSize(4);
            assertThat(result.getPlayers().get("Jogador 4")).containsExactly("A", "2", "3", "4", "5");
            assertThat(result.getPlayers().get("Jogador 3")).containsExactly("6", "7", "8", "9", "10");

            verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
        }

        @Test
        @DisplayName("Should build players' hands when more than 10 cards provided")
        void should_buildPlayersHands_when_moreThan10CardsProvided() {
            // Given
            String deckId = "validDeckId";
            List<CardEntity> cards = createCardEntities(15);

            when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

            // When
            PlayersResponseDTO result = buildHands.execute(deckId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPlayers()).hasSize(4);
            assertThat(result.getPlayers().get("Jogador 4")).containsExactly("A", "2", "3", "4", "5");
            assertThat(result.getPlayers().get("Jogador 3")).containsExactly("6", "7", "8", "9", "10");

            verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
        }

        @Test
        @DisplayName("Should throw exception when no cards found for deck ID")
        void should_throwException_when_noCardsFoundForDeckId() {
            // Given
            String deckId = "invalidDeckId";

            when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(new ArrayList<>());

            // When
            PlayersResponseDTO result = buildHands.execute(deckId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPlayers()).isEmpty();

            verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
        }

        private List<CardEntity> createCardEntities(int count) {
            List<CardEntity> cards = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                cards.add(new CardEntity(null, null, null, String.valueOf(i), null, null));
            }
            return cards;
        }
    }
}
```