package com.jb.leitnerbox.di

import com.jb.leitnerbox.core.data.local.dao.CardDao
import com.jb.leitnerbox.core.data.migration.CardMigrationHelper
import com.jb.leitnerbox.core.domain.session.SessionStateHolder
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import com.jb.leitnerbox.core.domain.backup.BackupSerializer
import com.jb.leitnerbox.core.domain.csv.CsvExporter
import com.jb.leitnerbox.core.domain.csv.CsvParser
import com.jb.leitnerbox.core.domain.usecase.backup.ExportBackupUseCase
import com.jb.leitnerbox.core.domain.usecase.backup.RestoreBackupUseCase
import com.jb.leitnerbox.core.domain.usecase.card.*
import com.jb.leitnerbox.core.domain.usecase.deck.*
import com.jb.leitnerbox.core.domain.usecase.importexport.*
import com.jb.leitnerbox.core.domain.usecase.session.*
import com.jb.leitnerbox.core.domain.usecase.settings.*
import com.jb.leitnerbox.core.domain.usecase.stats.*
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
    fun provideUpdateDeckColorUseCase(repository: DeckRepository): UpdateDeckColorUseCase = UpdateDeckColorUseCase(repository)

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
    fun provideGetDeckSummaryUseCase(repository: CardRepository): GetDeckSummaryUseCase = GetDeckSummaryUseCase(repository)

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
        sessionRepository: SessionRepository,
        settingsRepository: SettingsRepository
    ): HandleMissedDaysUseCase = HandleMissedDaysUseCase(sessionRepository, settingsRepository)

    @Provides
    @Singleton
    fun provideGetCurrentStreakUseCase(
        sessionRepository: SessionRepository,
        settingsRepository: SettingsRepository
    ): GetCurrentStreakUseCase = GetCurrentStreakUseCase(sessionRepository, settingsRepository)

    @Provides
    @Singleton
    fun provideBuildSessionUseCase(cardRepository: CardRepository): BuildSessionUseCase = BuildSessionUseCase(cardRepository)

    @Provides
    @Singleton
    fun provideSaveSessionUseCase(repository: SessionRepository): SaveSessionUseCase = SaveSessionUseCase(repository)

    @Provides
    @Singleton
    fun provideCancelPostponeBoxUseCase(
        cardRepository: CardRepository,
        sessionRepository: SessionRepository
    ): CancelPostponeBoxUseCase = CancelPostponeBoxUseCase(cardRepository, sessionRepository)

    @Provides
    @Singleton
    fun provideSessionStateHolder(): SessionStateHolder = SessionStateHolder()

    @Provides
    @Singleton
    fun provideCardMigrationHelper(
        cardDao: CardDao,
        answerNormalizer: AnswerNormalizer
    ): CardMigrationHelper = CardMigrationHelper(cardDao, answerNormalizer)

    @Provides
    @Singleton
    fun provideGetExcludedDaysUseCase(repository: SettingsRepository) = GetExcludedDaysUseCase(repository)

    @Provides
    @Singleton
    fun provideSetExcludedDaysUseCase(repository: SettingsRepository) = SetExcludedDaysUseCase(repository)

    @Provides
    @Singleton
    fun provideGetThemeUseCase(repository: SettingsRepository) = GetThemeUseCase(repository)

    @Provides
    @Singleton
    fun provideSetThemeUseCase(repository: SettingsRepository) = SetThemeUseCase(repository)

    @Provides
    @Singleton
    fun provideGetNotificationTimeUseCase(repository: SettingsRepository) = GetNotificationTimeUseCase(repository)

    @Provides
    @Singleton
    fun provideSetNotificationTimeUseCase(repository: SettingsRepository) = SetNotificationTimeUseCase(repository)

    @Provides
    @Singleton
    fun provideCsvParser(): CsvParser = CsvParser()

    @Provides
    @Singleton
    fun provideCsvExporter(): CsvExporter = CsvExporter()

    @Provides
    @Singleton
    fun provideImportCardsUseCase(
        cardRepository: CardRepository,
        parser: CsvParser,
        answerNormalizer: AnswerNormalizer
    ): ImportCardsUseCase = ImportCardsUseCase(cardRepository, parser, answerNormalizer)

    @Provides
    @Singleton
    fun provideExportDeckUseCase(
        cardRepository: CardRepository,
        exporter: CsvExporter
    ): ExportDeckUseCase = ExportDeckUseCase(cardRepository, exporter)

    @Provides
    @Singleton
    fun provideGetGlobalSummaryUseCase(
        cardRepository: CardRepository,
        sessionRepository: SessionRepository,
        deckRepository: DeckRepository
    ): GetGlobalSummaryUseCase = GetGlobalSummaryUseCase(cardRepository, sessionRepository, deckRepository)

    @Provides
    @Singleton
    fun provideGetDeckStatsUseCase(
        cardRepository: CardRepository,
        deckRepository: DeckRepository,
        sessionRepository: SessionRepository
    ): GetDeckStatsUseCase = GetDeckStatsUseCase(cardRepository, deckRepository, sessionRepository)

    @Provides
    @Singleton
    fun provideGetSessionHistoryUseCase(
        sessionRepository: SessionRepository,
        deckRepository: DeckRepository
    ): GetSessionHistoryUseCase = GetSessionHistoryUseCase(sessionRepository, deckRepository)

    @Provides
    @Singleton
    fun provideGetMasteredCardsUseCase(
        cardRepository: CardRepository
    ): GetMasteredCardsUseCase = GetMasteredCardsUseCase(cardRepository)

    @Provides
    @Singleton
    fun provideBackupSerializer(): BackupSerializer = BackupSerializer()

    @Provides
    @Singleton
    fun provideExportBackupUseCase(
        deckRepository: DeckRepository,
        cardRepository: CardRepository,
        sessionRepository: SessionRepository,
        settingsRepository: SettingsRepository,
        serializer: BackupSerializer
    ): ExportBackupUseCase = ExportBackupUseCase(
        deckRepository, cardRepository, sessionRepository,
        settingsRepository, serializer, "1.0" // TODO: Use real BuildConfig.VERSION_NAME
    )

    @Provides
    @Singleton
    fun provideRestoreBackupUseCase(
        deckRepository: DeckRepository,
        cardRepository: CardRepository,
        sessionRepository: SessionRepository,
        settingsRepository: SettingsRepository,
        serializer: BackupSerializer
    ): RestoreBackupUseCase = RestoreBackupUseCase(
        deckRepository, cardRepository, sessionRepository, settingsRepository, serializer
    )
}
