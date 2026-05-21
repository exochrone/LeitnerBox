package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.AppTheme
import com.jb.leitnerbox.core.domain.model.AppSettings
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    fun getExcludedDays(): Flow<Set<DayOfWeek>>
    suspend fun setExcludedDays(days: Set<DayOfWeek>)
    
    fun getNotificationTime(): Flow<LocalTime>
    suspend fun setNotificationTime(time: LocalTime)

    fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)

    fun getNewCardsPerDay(): Flow<Int>
    suspend fun setNewCardsPerDay(count: Int)

    suspend fun updateCardsActivatedToday(count: Int)
    suspend fun updateLastActivationDate(dateIso: String)

    fun getBufferSize(): Flow<Int> = getNewCardsPerDay()
}
