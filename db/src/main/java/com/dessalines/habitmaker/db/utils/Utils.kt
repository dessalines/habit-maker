package com.dessalines.habitmaker.db.utils

import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitUpdateStats
import kotlinx.serialization.Serializable

fun Int.toBool() = this == 1

fun Boolean.toInt() = this.compareTo(false)

const val TAG = "com.habitmaker.db"

enum class HabitFrequency {
    Daily,
    Weekly,
    Monthly,
    Yearly,
}

enum class HabitSort {
    Streak,
    Points,
    Score,

    /**
     * Whether its completed or not.
     */
    Status,
    DateCreated,
    Name,
}

enum class HabitSortOrder {
    Descending,
    Ascending,
}

fun HabitFrequency.toDays() =
    when (this) {
        HabitFrequency.Daily -> 1
        HabitFrequency.Weekly -> 7
        HabitFrequency.Monthly -> 28
        HabitFrequency.Yearly -> 365
    }

@Serializable
data class BulkInsert(
    val habitInserts: List<Pair<HabitInsert, HabitUpdateStats>>,
    val checkInserts: List<HabitCheckInsert>,
)
