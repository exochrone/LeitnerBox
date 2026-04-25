package com.jb.leitnerbox.core.domain.usecase.settings

import java.time.LocalTime

interface RescheduleNotificationUseCase {
    operator fun invoke(time: LocalTime)
}
