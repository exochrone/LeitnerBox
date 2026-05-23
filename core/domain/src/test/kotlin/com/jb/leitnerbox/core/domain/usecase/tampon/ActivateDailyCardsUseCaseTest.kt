package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.model.AppSettings
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ActivateDailyCardsUseCaseTest {
    private val cardRepository: CardRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val useCase = ActivateDailyCardsUseCase(cardRepository, settingsRepository)

    private fun createSettings(
        maxDaily: Int = 25,
        activatedToday: Int = 0,
        lastDate: String? = LocalDate.now().toString()
    ) = AppSettings(
        excludedDays = emptySet(),
        notificationTime = LocalTime.NOON,
        theme = AppTheme.SYSTEM,
        maxDailyNewCards = maxDaily,
        cardsActivatedToday = activatedToday,
        lastActivationDateIso = lastDate
    )

    private fun createCard(id: Long, deckId: Long) = Card(
        id = id,
        deckId = deckId,
        recto = "R$id",
        verso = "V$id",
        box = 1,
        isActive = false
    )

    @Test
    fun `nouveau jour remet le compteur a zero avant activation`() = runTest {
        val yesterday = LocalDate.now().minusDays(1).toString()
        coEvery { settingsRepository.settings } returns flowOf(createSettings(activatedToday = 10, lastDate = yesterday))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns emptyList()

        useCase()

        coVerify { settingsRepository.updateCardsActivatedToday(0) }
        coVerify { settingsRepository.updateLastActivationDate(LocalDate.now().toString()) }
    }

    @Test
    fun `quota atteint le meme jour ne declenche aucune activation`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 10, activatedToday = 10))

        useCase()

        coVerify(exactly = 0) { cardRepository.getDeckIdsWithInactiveCards() }
    }

    @Test
    fun `la rotation distribue les cartes entre les decks`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 4, activatedToday = 0))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns listOf(1L, 2L)
        
        val deck1Cards = listOf(createCard(1, 1), createCard(2, 1))
        val deck2Cards = listOf(createCard(3, 2), createCard(4, 2))
        
        coEvery { cardRepository.getOldestInactiveCards(1L, 4) } returns deck1Cards
        coEvery { cardRepository.getOldestInactiveCards(2L, 4) } returns deck2Cards

        val capturedCards = mutableListOf<List<Card>>()
        coEvery { cardRepository.updateCards(capture(capturedCards)) } returns Unit

        useCase()

        val activated = capturedCards.first()
        assertEquals(4, activated.size)
        // Round robin: Deck 1 (Card 1), Deck 2 (Card 3), Deck 1 (Card 2), Deck 2 (Card 4)
        assertEquals(1L, activated[0].id)
        assertEquals(3L, activated[1].id)
        assertEquals(2L, activated[2].id)
        assertEquals(4L, activated[3].id)
    }

    @Test
    fun `un deck epuise est retire de la rotation`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 3, activatedToday = 0))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns listOf(1L, 2L)
        
        val deck1Cards = listOf(createCard(1, 1)) // Un seul candidat
        val deck2Cards = listOf(createCard(2, 2), createCard(3, 2))
        
        coEvery { cardRepository.getOldestInactiveCards(1L, 3) } returns deck1Cards
        coEvery { cardRepository.getOldestInactiveCards(2L, 3) } returns deck2Cards

        val capturedCards = mutableListOf<List<Card>>()
        coEvery { cardRepository.updateCards(capture(capturedCards)) } returns Unit

        useCase()

        val activated = capturedCards.first()
        assertEquals(3, activated.size)
        // Rotation: Deck 1 (Card 1), Deck 2 (Card 2), Deck 1 est vide -> Deck 2 (Card 3)
        assertEquals(1L, activated[0].id)
        assertEquals(2L, activated[1].id)
        assertEquals(3L, activated[2].id)
    }

    @Test
    fun `quota partiel active exactement les cartes manquantes`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 10, activatedToday = 7))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns listOf(1L)
        coEvery { cardRepository.getOldestInactiveCards(1L, 3) } returns listOf(createCard(1, 1), createCard(2, 1), createCard(3, 1), createCard(4, 1))

        val capturedCards = mutableListOf<List<Card>>()
        coEvery { cardRepository.updateCards(capture(capturedCards)) } returns Unit

        useCase()

        assertEquals(3, capturedCards.first().size)
    }

    @Test
    fun `pas de cartes inactives ne declenche aucune mise a jour`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 10, activatedToday = 0))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns emptyList()

        useCase()

        coVerify(exactly = 0) { cardRepository.updateCards(any()) }
    }

    @Test
    fun `activation partielle si moins de cartes inactives que le quota`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 10, activatedToday = 0))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns listOf(1L)
        coEvery { cardRepository.getOldestInactiveCards(1L, 10) } returns listOf(createCard(1, 1), createCard(2, 1))

        val capturedCards = mutableListOf<List<Card>>()
        coEvery { cardRepository.updateCards(capture(capturedCards)) } returns Unit

        useCase()

        assertEquals(2, capturedCards.first().size)
        coVerify { settingsRepository.updateCardsActivatedToday(2) }
    }

    @Test
    fun `cardsActivatedToday est incremente du nombre de cartes activees`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(maxDaily = 10, activatedToday = 2))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns listOf(1L)
        coEvery { cardRepository.getOldestInactiveCards(1L, 8) } returns listOf(createCard(1, 1), createCard(2, 1), createCard(3, 1))

        useCase()

        coVerify { settingsRepository.updateCardsActivatedToday(5) } // 2 initial + 3 nouvelles
    }

    @Test
    fun `lastActivationDateIso vide est traite comme nouveau jour`() = runTest {
        coEvery { settingsRepository.settings } returns flowOf(createSettings(activatedToday = 10, lastDate = null))
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns emptyList()

        useCase()

        coVerify { settingsRepository.updateCardsActivatedToday(0) }
        coVerify { settingsRepository.updateLastActivationDate(LocalDate.now().toString()) }
    }
}
