package com.jb.leitnerbox.core.domain.repository

import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getExcludedDays(): Flow<Set<DayOfWeek>>
    suspend fun setExcludedDays(days: Set<DayOfWeek>)
    
    fun getNotificationTime(): Flow<LocalTime>
    suspend fun setNotificationTime(time: LocalTime)
}