package com.jb.leitnerbox.core.domain.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnswerNormalizerTest {

    private val normalizer = AnswerNormalizer()

    @Test
    fun `Normaliser Elephant`() {
        assertEquals("elephant", normalizer.normalize("Éléphant"))
    }

    @Test
    fun `Normaliser avec espaces`() {
        assertEquals("bonjour monde", normalizer.normalize("  Bonjour  Monde  "))
    }

    @Test
    fun `Normaliser avec caracteres speciaux`() {
        assertEquals("cest lete", normalizer.normalize("C'est l'été !"))
    }

    @Test
    fun `Normaliser chiffres`() {
        assertEquals("42", normalizer.normalize("42"))
    }

    @Test
    fun `Normaliser chaine vide`() {
        assertEquals("", normalizer.normalize(""))
    }

    @Test
    fun `Normaliser nono`() {
        assertEquals("nono", normalizer.normalize("ñoño"))
    }
}
