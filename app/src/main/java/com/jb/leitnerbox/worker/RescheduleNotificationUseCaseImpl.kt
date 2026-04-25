package com.jb.leitnerbox.worker

import android.content.Context
import com.jb.leitnerbox.core.domain.usecase.settings.RescheduleNotificationUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import javax.inject.Inject

class RescheduleNotificationUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RescheduleNotificationUseCase {
    override fun invoke(time: LocalTime) {
        NotificationScheduler.schedule(context, time)
    }
}
