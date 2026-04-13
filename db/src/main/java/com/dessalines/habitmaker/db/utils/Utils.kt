package com.dessalines.habitmaker.db.utils

import androidx.annotation.StringRes
import java.time.LocalDate
import java.time.ZoneId

const val TAG = "com.habitmaker.db"

enum class HabitFrequency {
    Daily,
    Weekly,
    Monthly,
    Yearly,
}

fun HabitFrequency.toDays() =
    when (this) {
        HabitFrequency.Daily -> 1
        HabitFrequency.Weekly -> 7
        HabitFrequency.Monthly -> 28
        HabitFrequency.Yearly -> 365
    }
