package com.jb.leitnerbox.core.domain.usecase.tampon

import com.jb.leitnerbox.core.domain.model.AppSettings
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class ActivateDailyCardsUseCaseTest {
    private val cardRepository: CardRepository = mockk(relaxed = true)
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val useCase = ActivateDailyCardsUseCase(cardRepository, settingsRepository)

    @Test
    fun `lorsque la date a change le compteur est remis a zero`() = runTest {
        // Arrange
        val yesterday = LocalDate.now().minusDays(1).toString()
        coEvery { settingsRepository.settings } returns flowOf(
            AppSettings(
                excludedDays = emptySet(),
                notificationTime = LocalTime.NOON,
                theme = AppTheme.SYSTEM,
                maxDailyNewCards = 20,
                cardsActivatedToday = 15,
                lastActivationDateIso = yesterday
            )
        )
        coEvery { cardRepository.getDeckIdsWithInactiveCards() } returns emptyList()

        // Act
        useCase()

        // Assert
        coVerify(exactly = 1) { settingsRepository.updateCardsActivatedToday(0) }
        coVerify(exactly = 1) { settingsRepository.updateLastActivationDate(any()) }
    }

    @Test
    fun `lorsque le quota est atteint le use case s arrete immediatement`() = runTest {
        // Arrange
        val today = LocalDate.now().toString()
        coEvery { settingsRepository.settings } returns flowOf(
            AppSettings(
                excludedDays = emptySet(),
                notificationTime = LocalTime.NOON,
                theme = AppTheme.SYSTEM,
                maxDailyNewCards = 20,
                cardsActivatedToday = 20,
                lastActivationDateIso = today
            )
        )

        // Act
        useCase()

        // Assert
        coVerify(exactly = 0) { cardRepository.getDeckIdsWithInactiveCards() }
    }
}
