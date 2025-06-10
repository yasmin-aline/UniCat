package com.deckofcards.usecases;

import com.deckofcards.adapter.http.dto.response.PlayersResponseDTO;
import com.deckofcards.adapter.repository.provider.CardsDbProvider;
import com.deckofcards.entities.CardEntity;
import com.deckofcards.entities.enums.CardSuit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuildHandsTest {

    @Mock
    private CardsDbProvider cardsDbProvider;

    @InjectMocks
    private BuildHands buildHands;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Construir mãos de jogadores com um deck válido contendo 20 cartas")
    void deveConstruirMaosDeJogadores_quandoDeckValidoCom20Cartas() {
        // Arrange
        String deckId = "deck123";
        List<CardEntity> cards = Arrays.asList(
            new CardEntity(UUID.randomUUID(), "code1", "image1", "value1", CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code2", "image2", "value2", CardSuit.HEARTS, null),
            new CardEntity(UUID.randomUUID(), "code3", "image3", "value3", CardSuit.CLUBS, null),
            new CardEntity(UUID.randomUUID(), "code4", "image4", "value4", CardSuit.DIAMONDS, null),
            new CardEntity(UUID.randomUUID(), "code5", "image5", "value5", CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code6", "image6", "value6", CardSuit.HEARTS, null),
            new CardEntity(UUID.randomUUID(), "code7", "image7", "value7", CardSuit.CLUBS, null),
            new CardEntity(UUID.randomUUID(), "code8", "image8", "value8", CardSuit.DIAMONDS, null),
            new CardEntity(UUID.randomUUID(), "code9", "image9", "value9", CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code10", "image10", "value10", CardSuit.HEARTS, null),
            new CardEntity(UUID.randomUUID(), "code11", "image11", "value11", CardSuit.CLUBS, null),
            new CardEntity(UUID.randomUUID(), "code12", "image12", "value12", CardSuit.DIAMONDS, null),
            new CardEntity(UUID.randomUUID(), "code13", "image13", "value13", CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code14", "image14", "value14", CardSuit.HEARTS, null),
            new CardEntity(UUID.randomUUID(), "code15", "image15", "value15", CardSuit.CLUBS, null),
            new CardEntity(UUID.randomUUID(), "code16", "image16", "value16", CardSuit.DIAMONDS, null),
            new CardEntity(UUID.randomUUID(), "code17", "image17", "value17", CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code18", "image18", "value18", CardSuit.HEARTS, null),
            new CardEntity(UUID.randomUUID(), "code19", "image19", "value19", CardSuit.CLUBS, null),
            new CardEntity(UUID.randomUUID(), "code20", "image20", "value20", CardSuit.DIAMONDS, null)
        );

        when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

        // Act
        PlayersResponseDTO atual_Response = buildHands.execute(deckId);

        // Assert
        assertNotNull(atual_Response);
        assertEquals(4, atual_Response.getPlayers().size());
        verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
    }

    @Test
    @DisplayName("Construir mãos de jogadores com um deck vazio")
    void deveConstruirMaosDeJogadores_quandoDeckVazio() {
        // Arrange
        String deckId = "deck123";
        List<CardEntity> cards = Collections.emptyList();

        when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

        // Act
        PlayersResponseDTO atual_Response = buildHands.execute(deckId);

        // Assert
        assertNotNull(atual_Response);
        assertTrue(atual_Response.getPlayers().isEmpty());
        verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
    }

    @Test
    @DisplayName("Construir mãos de jogadores com um deck contendo menos de 20 cartas")
    void deveConstruirMaosDeJogadores_quandoDeckComMenosDe20Cartas() {
        // Arrange
        String deckId = "deck123";
        List<CardEntity> cards = Arrays.asList(
            new CardEntity(UUID.randomUUID(), "code1", "image1", "value1", CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code2", "image2", "value2", CardSuit.HEARTS, null),
            new CardEntity(UUID.randomUUID(), "code3", "image3", "value3", CardSuit.CLUBS, null)
        );

        when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

        // Act
        PlayersResponseDTO atual_Response = buildHands.execute(deckId);

        // Assert
        assertNotNull(atual_Response);
        assertEquals(0, atual_Response.getPlayers().size());
        verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
    }

    @Test
    @DisplayName("Construir mãos de jogadores com cartas contendo valores nulos")
    void deveConstruirMaosDeJogadores_quandoCartasComValoresNulos() {
        // Arrange
        String deckId = "deck123";
        List<CardEntity> cards = Arrays.asList(
            new CardEntity(UUID.randomUUID(), "code1", "image1", null, CardSuit.SPADES, null),
            new CardEntity(UUID.randomUUID(), "code2", "image2", null, CardSuit.HEARTS, null)
        );

        when(cardsDbProvider.findCardsByDeckId(deckId)).thenReturn(cards);

        // Act
        PlayersResponseDTO atual_Response = buildHands.execute(deckId);

        // Assert
        assertNotNull(atual_Response);
        assertTrue(atual_Response.getPlayers().isEmpty());
        verify(cardsDbProvider, times(1)).findCardsByDeckId(deckId);
    }
}