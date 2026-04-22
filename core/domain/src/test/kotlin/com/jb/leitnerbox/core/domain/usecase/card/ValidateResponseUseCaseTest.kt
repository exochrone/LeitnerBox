package com.jb.leitnerbox.core.domain.usecase.card

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateResponseUseCaseTest {

    private lateinit var useCase: ValidateResponseUseCase

    @BeforeEach
    fun setUp() {
        useCase = ValidateResponseUseCase()
    }

    @Test
    fun `P1-UT-01 Normaliser Elephant`() {
        assertEquals("elephant", useCase.normalize("Éléphant"))
    }

    @Test
    fun `P1-UT-02 Normaliser avec espaces`() {
        assertEquals("bonjour monde", useCase.normalize("  Bonjour  Monde  "))
    }

    @Test
    fun `P1-UT-03 Normaliser avec caracteres speciaux`() {
        assertEquals("cest lete", useCase.normalize("C'est l'été !"))
    }

    @Test
    fun `P1-UT-04 Normaliser chiffres`() {
        assertEquals("42", useCase.normalize("42"))
    }

    @Test
    fun `P1-UT-05 Normaliser chaine vide`() {
        assertEquals("", useCase.normalize(""))
    }

    @Test
    fun `P1-UT-06 Normaliser nono`() {
        assertEquals("nono", useCase.normalize("ñoño"))
    }

    @Test
    fun `P1-UT-20 isMatch avec seuil de 1 pour azote`() {
        // "azote" (longueur 5) -> seuil = floor(5/5) = 1
        assertTrue(useCase("azote", "azoze"))
        assertTrue(useCase("azote", "azote"))
        assertFalse(useCase("azote", "azorex")) // distance 2
    }

    @Test
    fun `P1-UT-21 isMatch avec seuil de 0 pour fe`() {
        // "fe" (longueur 2) -> longueur <= 4 -> seuil = 0 (exact)
        assertTrue(useCase("fe", "fe"))
        assertFalse(useCase("fe", "fa"))
    }

    @Test
    fun `P1-UT-22 isMatch avec reponse attendue vide`() {
        // Cas particulier : si attendu est vide, on retourne true (desactivation)
        // Mais techniquement, si on appelle le use case c'est qu'on attend une saisie
        assertTrue(useCase("", "abc"))
    }
    
    @Test
    fun `isMatch avec accents et majuscules`() {
        assertTrue(useCase("Éléphant", "elephant"))
    }
}