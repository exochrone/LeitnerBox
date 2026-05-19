package com.jb.leitnerbox.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jb.leitnerbox.core.domain.usecase.card.ActivateAllDecksCardsUseCase
import com.jb.leitnerbox.core.domain.usecase.session.GetDailySessionPlanUseCase
import com.jb.leitnerbox.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SessionReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getDailySessionPlan: GetDailySessionPlanUseCase,
    private val activateAllDecksCards: ActivateAllDecksCardsUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Activer les cartes inactives en premier
        activateAllDecksCards()

        // 2. Vérifier les cartes à réviser (incluant celles nouvellement activées)
        val plan = getDailySessionPlan().first()
        val totalCards = plan.items.sumOf { it.cardCount }

        if (totalCards > 0) {
            NotificationHelper.createChannel(applicationContext)
            val notification = NotificationHelper.buildNotification(
                applicationContext, totalCards
            )
            try {
                NotificationManagerCompat.from(applicationContext)
                    .notify(NotificationHelper.NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Ignore if permission was revoked after check
            }
        }

        return Result.success()
    }
}
