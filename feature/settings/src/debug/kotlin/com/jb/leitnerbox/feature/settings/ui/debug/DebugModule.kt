package com.jb.leitnerbox.feature.settings.ui.debug

import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DebugModule {

    @Provides
    @Singleton
    fun provideDebugDataSeeder(
        deckRepository: DeckRepository,
        cardRepository: CardRepository
    ): DebugDataSeeder = DebugDataSeeder(deckRepository, cardRepository)
}
