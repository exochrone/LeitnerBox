package com.jb.leitnerbox.core.data.repository

import com.jb.leitnerbox.core.data.local.dao.DeckDao
import com.jb.leitnerbox.core.data.local.entity.DeckEntity
import com.jb.leitnerbox.core.data.local.entity.DeckWithCount
import com.jb.leitnerbox.core.domain.model.Deck
import com.jb.leitnerbox.core.domain.model.DeckWithCardCount
import com.jb.leitnerbox.core.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckRepositoryImpl(
    private val dao: DeckDao
) : DeckRepository {
    override fun getDecks(): Flow<List<Deck>> {
        return dao.getDecks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDecksWithCardCount(): Flow<List<DeckWithCardCount>> {
        return dao.getDecksWithCardCount().map { list ->
            list.map { item ->
                DeckWithCardCount(
                    deck = item.deck.toDomain(),
                    cardCount = item.cardCount
                )
            }
        }
    }

    override fun getDeckById(id: Long): Flow<Deck?> {
        return dao.getDeckById(id).map { it?.toDomain() }
    }

    override suspend fun insertDeck(deck: Deck): Long {
        return dao.insertDeck(DeckEntity.fromDomain(deck))
    }

    override suspend fun updateDeck(deck: Deck) {
        dao.updateDeck(DeckEntity.fromDomain(deck))
    }

    override suspend fun deleteDeck(deck: Deck) {
        dao.deleteDeck(DeckEntity.fromDomain(deck))
    }
}
