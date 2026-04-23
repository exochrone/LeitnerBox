package com.jb.leitnerbox.di

import com.jb.leitnerbox.core.data.local.dao.CardDao
import com.jb.leitnerbox.core.data.migration.CardMigrationHelper
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import com.jb.leitnerbox.core.domain.usecase.card.*
import com.jb.leitnerbox.core.domain.usecase.deck.*
import com.jb.leitnerbox.core.domain.usecase.session.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideAnswerNormalizer(): AnswerNormalizer = AnswerNormalizer()

    @Provides
    @Singleton
    fun provideNextSessionDateCalculator(): NextSessionDateCalculator = NextSessionDateCalculator()

    @Provides
    @Singleton
    fun provideGetDecksUseCase(repository: DeckRepository): GetDecksUseCase = GetDecksUseCase(repository)

    @Provides
    @Singleton
    fun provideGetDeckByIdUseCase(repository: DeckRepository): GetDeckByIdUseCase = GetDeckByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideAddDeckUseCase(repository: DeckRepository): AddDeckUseCase = AddDeckUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateDeckUseCase(repository: DeckRepository): UpdateDeckUseCase = UpdateDeckUseCase(repository)

    @Provides
    @Singleton
    fun provideDeleteDeckUseCase(repository: DeckRepository): DeleteDeckUseCase = DeleteDeckUseCase(repository)

    @Provides
    @Singleton
    fun provideAddCardUseCase(
        repository: CardRepository,
        answerNormalizer: AnswerNormalizer
    ): AddCardUseCase = AddCardUseCase(repository, answerNormalizer)

    @Provides
    @Singleton
    fun provideGetCardsUseCase(repository: CardRepository): GetCardsUseCase = GetCardsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCardByIdUseCase(repository: CardRepository): GetCardByIdUseCase = GetCardByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateCardUseCase(
        repository: CardRepository,
        answerNormalizer: AnswerNormalizer
    ): UpdateCardUseCase = UpdateCardUseCase(repository, answerNormalizer)

    @Provides
    @Singleton
    fun provideDeleteCardUseCase(repository: CardRepository): DeleteCardUseCase = DeleteCardUseCase(repository)

    @Provides
    @Singleton
    fun provideCheckAnswerUseCase(answerNormalizer: AnswerNormalizer): CheckAnswerUseCase = CheckAnswerUseCase(answerNormalizer)

    @Provides
    @Singleton
    fun provideEvaluateCardUseCase(
        nextSessionDateCalculator: NextSessionDateCalculator,
        cardRepository: CardRepository,
        settingsRepository: SettingsRepository
    ): EvaluateCardUseCase = EvaluateCardUseCase(cardRepository, settingsRepository, nextSessionDateCalculator)

    @Provides
    @Singleton
    fun provideGetDailySessionPlanUseCase(
        deckRepository: DeckRepository,
        cardRepository: CardRepository
    ): GetDailySessionPlanUseCase = GetDailySessionPlanUseCase(deckRepository, cardRepository)

    @Provides
    @Singleton
    fun providePostponeBoxSessionUseCase(
        deckRepository: DeckRepository,
        cardRepository: CardRepository,
        settingsRepository: SettingsRepository,
        nextSessionDateCalculator: NextSessionDateCalculator
    ): PostponeBoxSessionUseCase = PostponeBoxSessionUseCase(deckRepository, cardRepository, settingsRepository, nextSessionDateCalculator)

    @Provides
    @Singleton
    fun provideGetStatisticsUseCase(
        deckRepository: DeckRepository,
        cardRepository: CardRepository
    ): GetStatisticsUseCase = GetStatisticsUseCase(deckRepository, cardRepository)

    @Provides
    @Singleton
    fun provideHandleMissedDaysUseCase(
        sessionRepository: SessionRepository
    ): HandleMissedDaysUseCase = HandleMissedDaysUseCase(sessionRepository)

    @Provides
    @Singleton
    fun provideCardMigrationHelper(
        cardDao: CardDao,
        answerNormalizer: AnswerNormalizer
    ): CardMigrationHelper = CardMigrationHelper(cardDao, answerNormalizer)
}
