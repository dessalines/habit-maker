package com.dessalines.habitmaker.db

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun toLocalTime(time: Long?): LocalTime? =
        time?.let {
            LocalTime.ofSecondOfDay(it)
        }

    @TypeConverter
    fun toTimestamp(time: LocalTime?): Long? = time?.toSecondOfDay()?.toLong()

    @TypeConverter
    fun toDayOfWeek(day: Int?): DayOfWeek? =
        day?.let {
            DayOfWeek.entries[day]
        }

    @TypeConverter
    fun toDay(day: DayOfWeek?): Int? = day?.ordinal
}
