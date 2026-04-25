package com.jb.leitnerbox

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jb.leitnerbox.core.data.di.ApplicationScope
import com.jb.leitnerbox.core.data.migration.CardMigrationHelper
import com.jb.leitnerbox.core.domain.usecase.session.HandleMissedDaysUseCase
import com.jb.leitnerbox.core.domain.usecase.settings.GetNotificationTimeUseCase
import com.jb.leitnerbox.worker.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LeitnerBoxApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var cardMigrationHelper: CardMigrationHelper
    @Inject lateinit var handleMissedDays: HandleMissedDaysUseCase
    @Inject lateinit var getNotificationTime: GetNotificationTimeUseCase
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Recalcul de fond pour les cartes migrées (une seule fois)
        applicationScope.launch {
            launch { cardMigrationHelper.repopulateNormalizedFields() }
            launch { handleMissedDays() }
            launch {
                val time = getNotificationTime().first()
                NotificationScheduler.schedule(applicationContext, time)
            }
        }
    }
}
