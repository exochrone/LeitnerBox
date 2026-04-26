package com.jb.leitnerbox.core.domain.usecase.deck

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetDeckProgressUseCaseTest {

    private val cardRepository = mockk<CardRepository>()
    private val useCase = GetDeckProgressUseCase(cardRepository)

    @Test
    fun `deck vide retourne 0`() = runTest {
        every { cardRepository.getCardsByDeckId(any()) } returns flowOf(emptyList())
        useCase(deckId = 1L, boxCount = 5).first().let {
            assertEquals(0f, it)
        }
    }

    @Test
    fun `toutes les cartes en boite 1 retourne 0`() = runTest {
        val cards = List(5) { createCard(box = 1) }
        every { cardRepository.getCardsByDeckId(any()) } returns flowOf(cards)
        useCase(1L, 5).first().let {
            assertEquals(0f, it)
        }
    }

    @Test
    fun `toutes les cartes maitrisees retourne 1`() = runTest {
        val cards = List(5) { createCard(isLearned = true) }
        every { cardRepository.getCardsByDeckId(any()) } returns flowOf(cards)
        useCase(1L, 5).first().let {
            assertEquals(1f, it)
        }
    }

    @Test
    fun `calcul correct avec repartition mixte`() = runTest {
        // 10 boîte 1 (score 0) + 5 boîte 2 (score 1) + 4 maîtrisées (score 5)
        // total = 0 + 5 + 20 = 25, max = 19 * 5 = 95
        // progression = 25/95 ≈ 0.2631579
        val cards = List(10) { createCard(box = 1) } +
                    List(5)  { createCard(box = 2) } +
                    List(4)  { createCard(isLearned = true) }
        every { cardRepository.getCardsByDeckId(any()) } returns flowOf(cards)
        val result = useCase(1L, 5).first()
        assertEquals(25f / 95f, result, 0.001f)
    }

    @Test
    fun `recalcul automatique quand une carte change de boite`() = runTest {
        val flow = MutableStateFlow(listOf(createCard(box = 1)))
        every { cardRepository.getCardsByDeckId(any()) } returns flow

        val results = mutableListOf<Float>()
        val job = launch(UnconfinedTestDispatcher()) {
            useCase(1L, 5).collect { results.add(it) }
        }

        flow.value = listOf(createCard(box = 2))

        assertEquals(0f,   results[0])
        assertEquals(0.2f, results[1], 0.001f)
        job.cancel()
    }

    private fun createCard(box: Int = 1, isLearned: Boolean = false) = Card(
        id = 0, deckId = 1,
        recto = "Q", verso = "A",
        rectoNormalized = "q", answerNormalized = "a",
        box = box, isLearned = isLearned,
        needsInput = false
    )
}
