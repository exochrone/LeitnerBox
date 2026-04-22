package com.jb.leitnerbox.core.domain.service

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class SchedulingService {

    /**
     * Calcule la date de la prochaine session en ajoutant l'intervalle et en sautant
     * les jours exclus de la semaine.
     */
    fun calculateNextReviewDate(
        fromDate: LocalDate,
        intervalInDays: Int,
        excludedDays: Set<DayOfWeek>
    ): LocalDate {
        var resultDate = fromDate
        var addedDays = 0
        
        while (addedDays < intervalInDays) {
            resultDate = resultDate.plusDays(1)
            if (!excludedDays.contains(resultDate.dayOfWeek)) {
                addedDays++
            }
        }
        
        return resultDate
    }
}