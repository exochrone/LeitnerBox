package com.jb.leitnerbox.core.data.local.converters

import androidx.room.TypeConverter
import com.jb.leitnerbox.core.domain.model.PresentationOrder
import com.jb.leitnerbox.core.domain.model.WrongAnswerRule
import java.time.DayOfWeek
import java.time.Instant

class RoomConverters {
    @TypeConverter
    fun fromInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? = date?.toEpochMilli()

    @TypeConverter
    fun fromPresentationOrder(value: PresentationOrder): String = value.name

    @TypeConverter
    fun toPresentationOrder(value: String): PresentationOrder = PresentationOrder.valueOf(value)

    @TypeConverter
    fun fromWrongAnswerRule(value: WrongAnswerRule): String = value.name

    @TypeConverter
    fun toWrongAnswerRule(value: String): WrongAnswerRule = WrongAnswerRule.valueOf(value)

    @TypeConverter
    fun fromDayOfWeekSet(value: Set<DayOfWeek>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toDayOfWeekSet(value: String): Set<DayOfWeek> {
        if (value.isEmpty()) return emptySet()
        return value.split(",").map { DayOfWeek.valueOf(it) }.toSet()
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String = value.joinToString(",")

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.toLong() }
    }
}
