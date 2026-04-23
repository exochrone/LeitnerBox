package com.jb.leitnerbox.core.domain.usecase.session

import com.jb.leitnerbox.core.domain.model.SessionPlan
import com.jb.leitnerbox.core.domain.model.SessionPlanItem
import com.jb.leitnerbox.core.domain.repository.CardRepository
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetDailySessionPlanUseCase(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(date: Instant = Instant.now()): Flow<SessionPlan> {
        return deckRepository.getDecks().flatMapLatest { decks ->
            if (decks.isEmpty()) {
                return@flatMapLatest flowOf(SessionPlan(date, emptyList()))
            }
            
            val today = date.atZone(ZoneId.systemDefault()).toLocalDate()
            
            val flows = decks.map { deck ->
                cardRepository.getCardsByDeckId(deck.id).map { cards ->
                    val cardsDueToday = cards.filter { card ->
                        if (card.isLearned) return@filter false

                        val nextReview = card.nextReviewDate?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        nextReview != null && (nextReview.isBefore(today) || nextReview.isEqual(today))
                    }
                    
                    cardsDueToday.groupBy { it.box }.map { (box, cards) ->
                        SessionPlanItem(deck, box, cards.size)
                    }
                }
            }
            
            combine(flows) { itemsLists ->
                SessionPlan(date, itemsLists.flatMap { it })
            }
        }
    }
}