package com.jb.leitnerbox.core.domain.usecase.card

import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.service.SchedulingService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class EvaluateCardUseCaseTest {

    private lateinit var useCase: EvaluateCardUseCase
    private val cardRepository = mockk<CardRepository>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>()
    private val schedulingService = SchedulingService()

    private val defaultDeck = Deck(
        id = 1,
        name = "Test Deck",
        intervals = listOf(1, 3, 5, 7, 14),
        backToFirstOnFail = true
    )

    @BeforeEach
    fun setUp() {
        useCase = EvaluateCardUseCase(cardRepository, settingsRepository, schedulingService)
        coEvery { settingsRepository.getExcludedDays() } returns flowOf(emptySet())
    }

    @Test
    fun `P1-UT-50 Bonne reponse, carte en boite 1`() = runTest {
        val card = Card(id = 1, deckId = 1, recto = "Q", verso = "A", box = 1)
        
        useCase(card, defaultDeck, true)

        coVerify {
            cardRepository.updateCard(withArg {
                assertEquals(2, it.box)
                assertFalse(it.isLearned)
                assertNotNull(it.nextReviewDate)
            })
        }
    }

    @Test
    fun `P1-UT-52 Bonne reponse, derniere boite devient maitrisee`() = runTest {
        val card = Card(id = 1, deckId = 1, recto = "Q", verso = "A", box = 5)
        
        useCase(card, defaultDeck, true)

        coVerify {
            cardRepository.updateCard(withArg {
                assertTrue(it.isLearned)
                assertNull(it.nextReviewDate)
            })
        }
    }

    @Test
    fun `P1-UT-53 Mauvaise reponse, regle BACK_TO_BOX_ONE`() = runTest {
        val card = Card(id = 1, deckId = 1, recto = "Q", verso = "A", box = 3)
        
        useCase(card, defaultDeck, false)

        coVerify {
            cardRepository.updateCard(withArg {
                assertEquals(1, it.box)
            })
        }
    }

    @Test
    fun `P1-UT-54 Mauvaise reponse, regle PREVIOUS_BOX`() = runTest {
        val card = Card(id = 1, deckId = 1, recto = "Q", verso = "A", box = 3)
        val customDeck = defaultDeck.copy(backToFirstOnFail = false)
        
        useCase(card, customDeck, false)

        coVerify {
            cardRepository.updateCard(withArg {
                assertEquals(2, it.box)
            })
        }
    }

    @Test
    fun `P1-UT-56 Bonne reponse avec jours exclus respectes`() = runTest {
        // Vendredi 6 oct 2023
        val referenceInstant = LocalDate.of(2023, 10, 6)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()
        
        // Boite 1 -> Bonne rep -> Boite 2 (intervalle 3 jours dans defaultDeck)
        // Samedi/Dimanche exclus
        // +1 jour ouvré : Lundi 9
        // +2 jours ouvrés : Mardi 10
        // +3 jours ouvrés : Mercredi 11
        coEvery { settingsRepository.getExcludedDays() } returns flowOf(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        
        val card = Card(id = 1, deckId = 1, recto = "Q", verso = "A", box = 1)
        
        useCase(card, defaultDeck, true, referenceInstant)

        coVerify {
            cardRepository.updateCard(withArg {
                assertEquals(2, it.box)
                val expectedDate = LocalDate.of(2023, 10, 11)
                val actualDate = it.nextReviewDate?.atZone(ZoneId.systemDefault())?.toLocalDate()
                assertEquals(expectedDate, actualDate)
            })
        }
    }
}