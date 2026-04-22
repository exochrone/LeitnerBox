package com.jb.leitnerbox.core.domain.usecase.session

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class NextSessionDateCalculatorTest {

    private lateinit var calculator: NextSessionDateCalculator

    @BeforeEach
    fun setUp() {
        calculator = NextSessionDateCalculator()
    }

    @Test
    fun `P1-UT-30 Intervalle 1 jour, aucun jour exclu, depuis un lundi`() {
        val start = LocalDate.of(2023, 10, 2) // Lundi
        val next = calculator.calculateNextReviewDate(start, 1, emptySet())
        assertEquals(LocalDate.of(2023, 10, 3), next) // Mardi
    }

    @Test
    fun `P1-UT-31 Intervalle 3 jours, aucun jour exclu, depuis un lundi`() {
        val start = LocalDate.of(2023, 10, 2) // Lundi
        val next = calculator.calculateNextReviewDate(start, 3, emptySet())
        assertEquals(LocalDate.of(2023, 10, 5), next) // Jeudi
    }

    @Test
    fun `P1-UT-32 Intervalle 3 jours, samedi-dimanche exclus, depuis un vendredi`() {
        val start = LocalDate.of(2023, 10, 6) // Vendredi
        val next = calculator.calculateNextReviewDate(start, 3, setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        assertEquals(LocalDate.of(2023, 10, 11), next) 
    }

    @Test
    fun `P1-UT-33 Intervalle 1 jour, samedi-dimanche exclus, depuis un vendredi`() {
        val start = LocalDate.of(2023, 10, 6) // Vendredi
        val next = calculator.calculateNextReviewDate(start, 1, setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        assertEquals(LocalDate.of(2023, 10, 9), next) // Lundi
    }
}
