package com.jb.leitnerbox

import android.app.Application
import com.jb.leitnerbox.core.data.di.ApplicationScope
import com.jb.leitnerbox.core.data.migration.CardMigrationHelper
import com.jb.leitnerbox.core.domain.usecase.session.HandleMissedDaysUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LeitnerBoxApp : Application() {

    @Inject lateinit var cardMigrationHelper: CardMigrationHelper
    @Inject lateinit var handleMissedDays: HandleMissedDaysUseCase
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        // Recalcul de fond pour les cartes migrées (une seule fois)
        applicationScope.launch {
            launch { cardMigrationHelper.repopulateNormalizedFields() }
            launch { handleMissedDays() }
        }
    }
}