package com.jb.leitnerbox.core.domain.repository

import com.jb.leitnerbox.core.domain.model.AppTheme
import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getExcludedDays(): Flow<Set<DayOfWeek>>
    suspend fun setExcludedDays(days: Set<DayOfWeek>)
    
    fun getNotificationTime(): Flow<LocalTime>
    suspend fun setNotificationTime(time: LocalTime)

    fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
}