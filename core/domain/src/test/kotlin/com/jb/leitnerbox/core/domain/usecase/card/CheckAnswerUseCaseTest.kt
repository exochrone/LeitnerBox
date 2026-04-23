package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CheckAnswerUseCaseTest {

    private lateinit var useCase: CheckAnswerUseCase

    @BeforeEach
    fun setUp() {
        useCase = CheckAnswerUseCase(AnswerNormalizer())
    }

    private fun createCard(verso: String): Card {
        val normalizer = AnswerNormalizer()
        return Card(
            deckId = 1,
            recto = "Question",
            verso = verso,
            answerNormalized = normalizer.normalize(verso)
        )
    }

    @Test
    fun `P1-UT-20 isMatch avec seuil de 1 pour azote`() {
        val card = createCard("azote")
        // "azote" (longueur 5) -> seuil = floor(5/5) = 1
        assertTrue(useCase(card, "azoze"))
        assertTrue(useCase(card, "azote"))
        assertFalse(useCase(card, "azorex")) // distance 2
    }

    @Test
    fun `P1-UT-21 isMatch avec seuil de 0 pour fe`() {
        val card = createCard("fe")
        // "fe" (longueur 2) -> longueur <= 4 -> seuil = 0 (exact)
        assertTrue(useCase(card, "fe"))
        assertFalse(useCase(card, "fa"))
    }

    @Test
    fun `P1-UT-22 isMatch avec reponse attendue vide`() {
        val card = createCard("")
        assertTrue(useCase(card, "abc"))
    }
    
    @Test
    fun `isMatch avec accents et majuscules`() {
        val card = createCard("Éléphant")
        assertTrue(useCase(card, "elephant"))
    }
}
