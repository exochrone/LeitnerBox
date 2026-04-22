package com.jb.leitnerbox.core.domain.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class SchedulingServiceTest {

    private lateinit var service: SchedulingService

    @BeforeEach
    fun setUp() {
        service = SchedulingService()
    }

    @Test
    fun `P1-UT-30 Intervalle 1 jour, aucun jour exclu, depuis un lundi`() {
        val start = LocalDate.of(2023, 10, 2) // Lundi
        val next = service.calculateNextReviewDate(start, 1, emptySet())
        assertEquals(LocalDate.of(2023, 10, 3), next) // Mardi
    }

    @Test
    fun `P1-UT-31 Intervalle 3 jours, aucun jour exclu, depuis un lundi`() {
        val start = LocalDate.of(2023, 10, 2) // Lundi
        val next = service.calculateNextReviewDate(start, 3, emptySet())
        assertEquals(LocalDate.of(2023, 10, 5), next) // Jeudi
    }

    @Test
    fun `P1-UT-32 Intervalle 3 jours, samedi-dimanche exclus, depuis un vendredi`() {
        val start = LocalDate.of(2023, 10, 6) // Vendredi
        // +3 jours -> Lundi 9, Mardi 10, Mercredi 11 ? 
        // En fait la spec dit "sautés dans tous les calculs de dates".
        // Si on ajoute 3 jours "ouvrés" : 
        // 1. Ven -> Lun (1 jour ouvré)
        // 2. Lun -> Mar (2 jours ouvrés)
        // 3. Mar -> Mer (3 jours ouvrés)
        // Mais ma version actuelle fait simple : +intervalle, puis ajuste si c'est un jour exclu.
        // Relisons la spec : "Les jours exclus sont systématiquement sautés dans tous les calculs de dates"
        // Exemple spec : Ven + 3 jours (samedi/dimanche exclus) -> Mercredi.
        // Mon implémentation actuelle : 6 oct (Ven) + 3 jours = 9 oct (Lun). Lun n'est pas exclu.
        // Donc mon implémentation actuelle retournerait Lundi, pas Mercredi.
        // Il faut corriger l'algorithme pour sauter les jours PENDANT l'incrémentation.
        
        val next = service.calculateNextReviewDate(start, 3, setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        assertEquals(LocalDate.of(2023, 10, 11), next) 
    }

    @Test
    fun `P1-UT-33 Intervalle 1 jour, samedi-dimanche exclus, depuis un vendredi`() {
        val start = LocalDate.of(2023, 10, 6) // Vendredi
        val next = service.calculateNextReviewDate(start, 1, setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        assertEquals(LocalDate.of(2023, 10, 9), next) // Lundi
    }
}