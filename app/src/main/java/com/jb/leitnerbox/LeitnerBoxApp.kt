package com.jb.leitnerbox

import android.app.Application
import com.jb.leitnerbox.core.data.migration.CardMigrationHelper
import com.jb.leitnerbox.di.ApplicationScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LeitnerBoxApp : Application() {

    @Inject lateinit var cardMigrationHelper: CardMigrationHelper
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        // Recalcul de fond pour les cartes migrées (une seule fois)
        applicationScope.launch {
            cardMigrationHelper.repopulateNormalizedFields()
        }
    }
}