package com.jb.leitnerbox.core.data.repository

import androidx.datastore.core.DataStore
import com.jb.leitnerbox.core.data.AppSettingsProto
import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.model.AppSettings
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<AppSettingsProto>
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { proto ->
        AppSettings(
            excludedDays = proto.excludedDaysList.map { DayOfWeek.of(it) }.toSet(),
            notificationTime = LocalTime.of(proto.notificationHour, proto.notificationMinute),
            theme = when (proto.theme) {
                1 -> AppTheme.LIGHT
                2 -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            },
            maxDailyNewCards = if (proto.newCardsPerDay == 0) 20 else proto.newCardsPerDay,
            cardsActivatedToday = proto.cardsActivatedToday,
            lastActivationDateIso = proto.lastActivationDateIso.takeIf { it.isNotEmpty() }
        )
    }

    override fun getExcludedDays(): Flow<Set<DayOfWeek>> {
        return dataStore.data.map { proto ->
            proto.excludedDaysList.map { DayOfWeek.of(it) }.toSet()
        }
    }

    override suspend fun setExcludedDays(days: Set<DayOfWeek>) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearExcludedDays()
                .addAllExcludedDays(days.map { it.value })
                .build()
        }
    }

    override fun getNotificationTime(): Flow<LocalTime> {
        return dataStore.data.map { proto ->
            LocalTime.of(proto.notificationHour, proto.notificationMinute)
        }
    }

    override suspend fun setNotificationTime(time: LocalTime) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setNotificationHour(time.hour)
                .setNotificationMinute(time.minute)
                .build()
        }
    }

    override fun getTheme(): Flow<AppTheme> {
        return dataStore.data.map { proto ->
            when (proto.theme) {
                1 -> AppTheme.LIGHT
                2 -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setTheme(
                    when (theme) {
                        AppTheme.SYSTEM -> 0
                        AppTheme.LIGHT -> 1
                        AppTheme.DARK -> 2
                    }
                )
                .build()
        }
    }

    override fun getNewCardsPerDay(): Flow<Int> {
        return dataStore.data.map { proto ->
            if (proto.newCardsPerDay == 0) 25 else proto.newCardsPerDay
        }
    }

    override suspend fun setNewCardsPerDay(count: Int) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setNewCardsPerDay(count)
                .build()
        }
    }

    override suspend fun updateCardsActivatedToday(count: Int) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setCardsActivatedToday(count)
                .build()
        }
    }

    override suspend fun updateLastActivationDate(dateIso: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setLastActivationDateIso(dateIso)
                .build()
        }
    }

    override suspend fun resetActivationDate() {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setLastActivationDateIso("")
                .setCardsActivatedToday(0)
                .build()
        }
    }
}
