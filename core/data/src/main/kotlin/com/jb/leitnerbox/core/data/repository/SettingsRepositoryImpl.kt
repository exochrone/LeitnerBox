package com.jb.leitnerbox.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jb.leitnerbox.core.domain.repository.SettingsRepository
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val EXCLUDED_DAYS = stringPreferencesKey("excluded_days")
        val NOTIFICATION_TIME = stringPreferencesKey("notification_time")
    }

    override fun getExcludedDays(): Flow<Set<DayOfWeek>> {
        return dataStore.data.map { preferences ->
            val daysString = preferences[Keys.EXCLUDED_DAYS] ?: ""
            if (daysString.isEmpty()) emptySet()
            else daysString.split(",").map { DayOfWeek.valueOf(it) }.toSet()
        }
    }

    override suspend fun setExcludedDays(days: Set<DayOfWeek>) {
        dataStore.edit { preferences ->
            preferences[Keys.EXCLUDED_DAYS] = days.joinToString(",") { it.name }
        }
    }

    override fun getNotificationTime(): Flow<LocalTime> {
        return dataStore.data.map { preferences ->
            val timeString = preferences[Keys.NOTIFICATION_TIME] ?: "08:00"
            LocalTime.parse(timeString)
        }
    }

    override suspend fun setNotificationTime(time: LocalTime) {
        dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATION_TIME] = time.toString()
        }
    }
}
