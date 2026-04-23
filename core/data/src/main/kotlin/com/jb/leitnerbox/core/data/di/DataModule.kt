package com.jb.leitnerbox.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.jb.leitnerbox.core.data.local.LeitnerDatabase
import com.jb.leitnerbox.core.data.local.dao.SessionDao
import com.jb.leitnerbox.core.data.repository.CardRepositoryImpl
import com.jb.leitnerbox.core.data.repository.DeckRepositoryImpl
import com.jb.leitnerbox.core.data.repository.SessionRepositoryImpl
import com.jb.leitnerbox.core.data.repository.SettingsRepositoryImpl
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideLeitnerDatabase(@ApplicationContext context: Context): LeitnerDatabase {
        return Room.databaseBuilder(
            context,
            LeitnerDatabase::class.java,
            LeitnerDatabase.DATABASE_NAME
        )
            .addMigrations(LeitnerDatabase.MIGRATION_1_2, LeitnerDatabase.MIGRATION_2_3)
            .build()
    }

    @Provides
    @Singleton
    fun provideDeckDao(db: LeitnerDatabase) = db.deckDao

    @Provides
    @Singleton
    fun provideCardDao(db: LeitnerDatabase) = db.cardDao

    @Provides
    @Singleton
    fun provideSessionDao(db: LeitnerDatabase) = db.sessionDao

    @Provides
    @Singleton
    fun provideDeckRepository(db: LeitnerDatabase): DeckRepository {
        return DeckRepositoryImpl(db.deckDao)
    }

    @Provides
    @Singleton
    fun provideCardRepository(db: LeitnerDatabase): CardRepository {
        return CardRepositoryImpl(db.cardDao)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(sessionDao: SessionDao): SessionRepository {
        return SessionRepositoryImpl(sessionDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context.dataStore)
    }
}
