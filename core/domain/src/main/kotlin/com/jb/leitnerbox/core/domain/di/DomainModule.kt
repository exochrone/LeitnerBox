package com.jb.leitnerbox.core.domain.di

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.service.SchedulingService
import com.jb.leitnerbox.core.domain.usecase.card.AddCardUseCase
import com.jb.leitnerbox.core.domain.usecase.card.EvaluateCardUseCase
import com.jb.leitnerbox.core.domain.usecase.card.GetCardByIdUseCase
import com.jb.leitnerbox.core.domain.usecase.card.GetCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.card.UpdateCardUseCase
import com.jb.leitnerbox.core.domain.usecase.card.ValidateResponseUseCase
import com.jb.leitnerbox.core.domain.usecase.deck.*
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import com.jb.leitnerbox.core.domain.usecase.session.GetStatisticsUseCase
import com.jb.leitnerbox.core.domain.usecase.session.PostponeBoxSessionUseCase
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
    fun provideSchedulingService(): SchedulingService = SchedulingService()

    @Provides
    @Singleton
    fun provideGetDecksUseCase(repository: DeckRepository): GetDecksUseCase = GetDecksUseCase(repository)

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
    fun provideAddCardUseCase(repository: CardRepository): AddCardUseCase = AddCardUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCardsUseCase(repository: CardRepository): GetCardsUseCase = GetCardsUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCardByIdUseCase(repository: CardRepository): GetCardByIdUseCase = GetCardByIdUseCase(repository)

    @Provides
    @Singleton
    fun provideUpdateCardUseCase(repository: CardRepository): UpdateCardUseCase = UpdateCardUseCase(repository)

    @Provides
    @Singleton
    fun provideValidateResponseUseCase(): ValidateResponseUseCase = ValidateResponseUseCase()

    @Provides
    @Singleton
    fun provideEvaluateCardUseCase(
        schedulingService: SchedulingService,
        cardRepository: CardRepository,
        settingsRepository: SettingsRepository
    ): EvaluateCardUseCase = EvaluateCardUseCase(cardRepository, settingsRepository, schedulingService)

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
        schedulingService: SchedulingService
    ): PostponeBoxSessionUseCase = PostponeBoxSessionUseCase(deckRepository, cardRepository, schedulingService)

    @Provides
    @Singleton
    fun provideGetStatisticsUseCase(
        deckRepository: DeckRepository,
        cardRepository: CardRepository
    ): GetStatisticsUseCase = GetStatisticsUseCase(deckRepository, cardRepository)
}
