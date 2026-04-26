package com.jb.leitnerbox.feature.settings.ui.debug

import com.jb.leitnerbox.core.domain.model.*
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import com.jb.leitnerbox.core.domain.repository.SessionRepository
import com.jb.leitnerbox.core.domain.utils.AnswerNormalizer
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.temporal.ChronoUnit

class DebugDataSeeder(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository
) {
    private val normalizer = AnswerNormalizer()

    companion object {
        const val TEST_PREFIX = "[TEST]"
    }

    suspend fun seedLongNames() {
        val deckId = deckRepository.insertDeck(
            Deck(
                name = "$TEST_PREFIX Deck avec un nom extrêmement long pour tester les affichages et vérifier que l'UI ne déborde pas",
                description = "Description également très longue pour tester le rendu dans l'écran de détail du deck et s'assurer que le texte est correctement tronqué ou mis en forme sur plusieurs lignes sans casser la mise en page",
                intervals = listOf(1, 3, 5, 7, 14),
                wrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
                presentationOrder = PresentationOrder.BY_BOX
            )
        )

        insertCard(deckId, box = 1,
            recto = "Q?",
            verso = "Réponse extrêmement longue qui dépasse largement la largeur normale d'une carte et qui devrait forcer le composant FlipCard à scroller ou à adapter sa mise en page pour rester lisible sur tous les types d'écrans Android."
        )

        insertCard(deckId, box = 1,
            recto = "Question très longue qui occupe plusieurs lignes et qui teste la capacité du composant recto de la carte à afficher correctement un texte de grande taille sans déborder de son conteneur ni masquer les boutons d'évaluation situés en dessous ?",
            verso = "Court."
        )

        insertCard(deckId, box = 1,
            recto = "Question longue : Quelle est la différence fondamentale entre une architecture en couches stricte où chaque couche ne communique qu'avec la couche adjacente et une architecture hexagonale où le domaine est totalement isolé des détails techniques d'infrastructure ?",
            verso = "En architecture hexagonale, le domaine ne dépend d'aucune couche technique. Les adaptateurs (UI, base de données, API) dépendent du domaine via des ports (interfaces). En architecture en couches stricte, chaque couche dépend de la couche du dessous, ce qui peut créer des couplages indirects."
        )

        insertCard(deckId, box = 2,
            recto = "Complétez : L'injection de dépendances consiste à...",
            verso = "fournir les dépendances d'une classe depuis l'extérieur plutôt que de les instancier elle-même, ce qui facilite les tests et réduit le couplage",
            needsInput = true
        )
    }

    suspend fun seedIntervalTest() {
        val deckId = deckRepository.insertDeck(
            Deck(
                name = "$TEST_PREFIX Test intervalles par boîte",
                description = "3 cartes par boîte (1 à 4), toutes dues aujourd'hui. " +
                    "Réviser boîte par boîte pour observer le décalage de chaque intervalle. " +
                    "Règle : retour à la boîte précédente en cas d'erreur.",
                intervals = listOf(1, 3, 5, 7, 14),
                wrongAnswerRule = WrongAnswerRule.PREVIOUS_BOX,
                presentationOrder = PresentationOrder.BY_BOX
            )
        )

        val now = Instant.now()

        // Boîte 4 — intervalle 7 jours (index 3)
        // Après une bonne réponse → boîte 5, prochaine session dans 14 jours
        // Après une mauvaise réponse → boîte 3, prochaine session dans 5 jours
        repeat(3) { i ->
            insertCard(
                deckId = deckId, box = 4,
                recto = "Boîte 4 — Q${i + 1} : Quel est le résultat de ${(i + 1) * 8} × 2 ?",
                verso = "${(i + 1) * 8 * 2}",
                nextReviewDate = now
            )
        }

        // Boîte 3 — intervalle 5 jours (index 2)
        // Après une bonne réponse → boîte 4, prochaine session dans 7 jours
        // Après une mauvaise réponse → boîte 2, prochaine session dans 3 jours
        repeat(3) { i ->
            insertCard(
                deckId = deckId, box = 3,
                recto = "Boîte 3 — Q${i + 1} : Quel est le résultat de ${(i + 1) * 4} × 2 ?",
                verso = "${(i + 1) * 4 * 2}",
                nextReviewDate = now
            )
        }

        // Boîte 2 — intervalle 3 jours (index 1)
        // Après une bonne réponse → boîte 3, prochaine session dans 5 jours
        // Après une mauvaise réponse → boîte 1, prochaine session dans 1 jour
        repeat(3) { i ->
            insertCard(
                deckId = deckId, box = 2,
                recto = "Boîte 2 — Q${i + 1} : Quel est le résultat de ${(i + 1) * 2} × 2 ?",
                verso = "${(i + 1) * 2 * 2}",
                nextReviewDate = now
            )
        }

        // Boîte 1 — intervalle 1 jour (index 0)
        // Après une bonne réponse → boîte 2, prochaine session dans 3 jours
        // Après une mauvaise réponse → reste en boîte 1, prochaine session dans 1 jour
        repeat(3) { i ->
            insertCard(
                deckId = deckId, box = 1,
                recto = "Boîte 1 — Q${i + 1} : Quel est le résultat de ${i + 1} × 2 ?",
                verso = "${(i + 1) * 2}",
                nextReviewDate = now
            )
        }
    }

    suspend fun seedBoxCirculation() {
        val deckId = deckRepository.insertDeck(
            Deck(
                name = "$TEST_PREFIX Circulation des boîtes",
                description = "3 cartes simples pour tester manuellement le passage entre les boîtes",
                intervals = listOf(1, 3, 5, 7, 14),
                wrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
                presentationOrder = PresentationOrder.BY_BOX
            )
        )

        insertCard(deckId, box = 1, recto = "Capitale de la France ?", verso = "Paris")
        insertCard(deckId, box = 1, recto = "2 + 2 = ?", verso = "4")
        insertCard(deckId, box = 1, recto = "Couleur du ciel par beau temps ?", verso = "Bleu",
            needsInput = true)
    }

    suspend fun seedMasteryTest() {
        val deckId = deckRepository.insertDeck(
            Deck(
                name = "$TEST_PREFIX Test de maîtrise",
                description = "30 cartes réparties sur 5 boîtes — 5 cartes en boîte 5 dues aujourd'hui pour tester la maîtrise",
                intervals = listOf(1, 3, 5, 7, 14),
                wrongAnswerRule = WrongAnswerRule.PREVIOUS_BOX,
                presentationOrder = PresentationOrder.RANDOM
            )
        )

        val now = Instant.now()
        val farFuture = now.plus(30, ChronoUnit.DAYS)

        repeat(5) { i ->
            insertCard(deckId, box = 5,
                recto = "Boîte 5 — Carte ${i + 1} (due aujourd'hui)",
                verso = "Réponse ${i + 1}",
                nextReviewDate = now
            )
        }

        repeat(5) { i ->
            insertCard(deckId, box = 4,
                recto = "Boîte 4 — Carte ${i + 1}",
                verso = "Réponse ${i + 1}",
                nextReviewDate = farFuture
            )
        }

        repeat(7) { i ->
            insertCard(deckId, box = 3,
                recto = "Boîte 3 — Carte ${i + 1}",
                verso = "Réponse ${i + 1}",
                nextReviewDate = farFuture
            )
        }

        repeat(8) { i ->
            insertCard(deckId, box = 2,
                recto = "Boîte 2 — Carte ${i + 1}",
                verso = "Réponse ${i + 1}",
                nextReviewDate = farFuture
            )
        }

        repeat(5) { i ->
            insertCard(deckId, box = 1,
                recto = "Boîte 1 — Carte ${i + 1}",
                verso = "Réponse ${i + 1}",
                nextReviewDate = farFuture
            )
        }
    }

    suspend fun seedLatexCards() {
        val deckId = deckRepository.insertDeck(
            Deck(
                name = "$TEST_PREFIX Formules LaTeX",
                description = "10 cartes pour tester le rendu LaTeX : formules en question, en réponse et dans les deux.",
                intervals = listOf(1, 3, 5, 7, 14),
                wrongAnswerRule = WrongAnswerRule.BACK_TO_BOX_ONE,
                presentationOrder = PresentationOrder.RANDOM
            )
        )

        // ── Formule uniquement dans la RÉPONSE ─────────────────────────────
        insertCard(deckId, box = 1,
            recto = "Quelle est la formule quadratique ?",
            verso = "\$\$x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}\$\$"
        )
        insertCard(deckId, box = 1,
            recto = "Quel est le théorème de Pythagore ?",
            verso = "\$\$a^2 + b^2 = c^2\$\$"
        )
        insertCard(deckId, box = 2,
            recto = "Quelle est l'identité d'Euler ?",
            verso = "\$\$e^{i\\pi} + 1 = 0\$\$"
        )

        // ── Formule uniquement dans la QUESTION ────────────────────────────
        insertCard(deckId, box = 1,
            recto = "Que représente \$E = mc^2\$ ?",
            verso = "L'équivalence masse-énergie (Einstein)"
        )
        insertCard(deckId, box = 2,
            recto = "Quelle est la valeur de \$\\int_0^1 x^2 \\, dx\$ ?",
            verso = "1/3"
        )
        insertCard(deckId, box = 3,
            recto = "Si \$f(x) = x^3\$, quelle est \$f'(x)\$ ?",
            verso = "3x²"
        )

        // ── Formule dans la QUESTION ET dans la RÉPONSE ────────────────────
        insertCard(deckId, box = 1,
            recto = "Quelle est la dérivée de \$f(x) = \\sin(x)\$ ?",
            verso = "\$f'(x) = \\cos(x)\$"
        )
        insertCard(deckId, box = 2,
            recto = "Si \$A = \\pi r^2\$, quelle est la formule du périmètre d'un cercle ?",
            verso = "\$P = 2\\pi r\$"
        )
        insertCard(deckId, box = 3,
            recto = "Développe \$(a + b)^2\$",
            verso = "\$\$a^2 + 2ab + b^2\$\$"
        )
        insertCard(deckId, box = 4,
            recto = "Quelle est la somme \$\\sum_{k=1}^{n} k\$ ?",
            verso = "\$\$\\frac{n(n+1)}{2}\$\$"
        )
    }

    suspend fun advanceTime(days: Int) {
        val testDecks = deckRepository.getDecks()
            .first()
            .filter { it.name.startsWith(TEST_PREFIX) }

        testDecks.forEach { deck ->
            cardRepository.getCardsByDeckId(deck.id)
                .first()
                .filter { !it.isLearned }
                .forEach { card ->
                    val currentDate = card.nextReviewDate ?: Instant.now()
                    cardRepository.updateCard(
                        card.copy(
                            nextReviewDate = currentDate.minus(
                                days.toLong(), ChronoUnit.DAYS
                            )
                        )
                    )
                }
        }
    }

    suspend fun cleanAllTestData() {
        deckRepository.getDecks()
            .first()
            .filter { it.name.startsWith(TEST_PREFIX) }
            .forEach { deck -> deckRepository.deleteDeck(deck) }
    }

    suspend fun clearAllSessions() {
        sessionRepository.deleteAllSessions()
    }

    private suspend fun insertCard(
        deckId: Long,
        box: Int,
        recto: String,
        verso: String,
        needsInput: Boolean = false,
        nextReviewDate: Instant = Instant.now()
    ) {
        cardRepository.insertCard(
            Card(
                deckId = deckId,
                recto = recto,
                verso = verso,
                box = box,
                needsInput = needsInput,
                nextReviewDate = nextReviewDate,
                rectoNormalized = normalizer.normalize(recto),
                answerNormalized = normalizer.normalize(verso),
                isLearned = false
            )
        )
    }
}
