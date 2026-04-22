package com.jb.leitnerbox.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jb.leitnerbox.core.data.local.converters.RoomConverters
import com.jb.leitnerbox.core.data.local.dao.CardDao
import com.jb.leitnerbox.core.data.local.dao.DeckDao
import com.jb.leitnerbox.core.data.local.entity.CardEntity
import com.jb.leitnerbox.core.data.local.entity.DeckEntity

@Database(
    entities = [DeckEntity::class, CardEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class LeitnerDatabase : RoomDatabase() {
    abstract val deckDao: DeckDao
    abstract val cardDao: CardDao

    companion object {
        const val DATABASE_NAME = "leitner_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Étape 1 : créer la nouvelle table decks_new
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS decks_new (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name            TEXT    NOT NULL,
                        description     TEXT    NOT NULL,
                        intervals       TEXT    NOT NULL,
                        wrongAnswerRule TEXT    NOT NULL,
                        presentationOrder TEXT  NOT NULL
                    )
                """.trimIndent())

                // Étape 2 : copier les données et convertir backToFirstOnFail
                database.execSQL("""
                    INSERT INTO decks_new (
                        id, name, description, intervals,
                        wrongAnswerRule, presentationOrder
                    )
                    SELECT
                        id, name, description, intervals,
                        CASE WHEN backToFirstOnFail = 1
                             THEN 'BACK_TO_BOX_ONE'
                             ELSE 'PREVIOUS_BOX'
                        END,
                        presentationOrder
                    FROM decks
                """.trimIndent())

                // Étape 3 : supprimer l'ancienne table
                database.execSQL("DROP TABLE decks")

                // Étape 4 : renommer la nouvelle table
                database.execSQL("ALTER TABLE decks_new RENAME TO decks")

                // Ajout des colonnes à cards
                database.execSQL("""
                    ALTER TABLE cards
                    ADD COLUMN answerNormalized TEXT NOT NULL DEFAULT ''
                """.trimIndent())

                database.execSQL("""
                    ALTER TABLE cards
                    ADD COLUMN rectoNormalized TEXT NOT NULL DEFAULT ''
                """.trimIndent())

                // Ajout de l'index
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_cards_deckId_rectoNormalized
                    ON cards (deckId, rectoNormalized)
                """.trimIndent())
            }
        }
    }
}
