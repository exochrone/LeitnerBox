package com.jb.leitnerbox.feature.decks.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DeckDateFormatter {
    /**
     * Formate une date de prochaine session selon les règles :
     * - J+0 : "aujourd'hui"
     * - J+1 : "demain"
     * - J+2 à J+6 : "mercredi prochain" (nom du jour en minuscules + "prochain")
     * - J+7 et plus : "9 juin" (jour + mois en texte, sans l'année)
     * - null : "--"
     */
    fun format(instant: Instant?, locale: Locale = Locale.getDefault()): String {
        instant ?: return "--"
        val today = LocalDate.now(ZoneId.systemDefault())
        val target = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val days = ChronoUnit.DAYS.between(today, target)

        return when {
            days <= 0 -> "aujourd'hui"
            days == 1L -> "demain"
            days in 2..6 -> {
                val dayName = target
                    .dayOfWeek
                    .getDisplayName(TextStyle.FULL, locale)
                    .lowercase()
                "$dayName prochain"
            }
            else -> {
                val day = target.dayOfMonth
                val month = target.month
                    .getDisplayName(TextStyle.FULL, locale)
                    .lowercase()
                "$day $month"
            }
        }
    }
}
