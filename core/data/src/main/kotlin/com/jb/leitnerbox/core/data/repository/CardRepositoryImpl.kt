package com.jb.leitnerbox.core.data.repository

import com.jb.leitnerbox.core.data.local.dao.CardDao
import com.jb.leitnerbox.core.data.local.entity.CardEntity
import com.jb.leitnerbox.core.domain.model.Card
import com.jb.leitnerbox.core.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class CardRepositoryImpl(
    private val dao: CardDao
) : CardRepository {
    override fun getCardsByDeckId(deckId: Long): Flow<List<Card>> {
        return dao.getCardsByDeckId(deckId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCards(): Flow<List<Card>> {
        return dao.getAllCards().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMasteredCards(): Flow<List<Card>> {
        return dao.getMasteredCards().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCardById(id: Long): Flow<Card?> {
        return dao.getCardById(id).map { it?.toDomain() }
    }

    override fun getCardsToReview(deckId: Long): Flow<List<Card>> {
        return dao.getCardsToReview(deckId, Instant.now().toEpochMilli()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertCard(card: Card): Long {
        return dao.insertCard(CardEntity.fromDomain(card))
    }

    override suspend fun updateCard(card: Card) {
        dao.updateCard(CardEntity.fromDomain(card))
    }

    override suspend fun deleteCard(card: Card) {
        dao.deleteCard(CardEntity.fromDomain(card))
    }

    override suspend fun getCardByRectoNormalized(deckId: Long, rectoNormalized: String): Card? {
        return dao.getCardByRectoNormalized(deckId, rectoNormalized)?.toDomain()
    }
}
