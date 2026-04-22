package com.jb.leitnerbox.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jb.leitnerbox.core.data.local.converters.RoomConverters
import com.jb.leitnerbox.core.data.local.dao.CardDao
import com.jb.leitnerbox.core.data.local.dao.DeckDao
import com.jb.leitnerbox.core.data.local.entity.CardEntity
import com.jb.leitnerbox.core.data.local.entity.DeckEntity

@Database(
    entities = [DeckEntity::class, CardEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class LeitnerDatabase : RoomDatabase() {
    abstract val deckDao: DeckDao
    abstract val cardDao: CardDao

    companion object {
        const val DATABASE_NAME = "leitner_db"
    }
}
