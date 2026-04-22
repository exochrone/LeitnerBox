package com.jb.leitnerbox.core.domain.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AnswerNormalizerTest {

    @Test
    fun `Normaliser Elephant`() {
        assertEquals("elephant", AnswerNormalizer.normalize("Éléphant"))
    }

    @Test
    fun `Normaliser avec espaces`() {
        assertEquals("bonjour monde", AnswerNormalizer.normalize("  Bonjour  Monde  "))
    }

    @Test
    fun `Normaliser avec caracteres speciaux`() {
        assertEquals("cest lete", AnswerNormalizer.normalize("C'est l'été !"))
    }

    @Test
    fun `Normaliser chiffres`() {
        assertEquals("42", AnswerNormalizer.normalize("42"))
    }

    @Test
    fun `Normaliser chaine vide`() {
        assertEquals("", AnswerNormalizer.normalize(""))
    }

    @Test
    fun `Normaliser nono`() {
        assertEquals("nono", AnswerNormalizer.normalize("ñoño"))
    }
}
