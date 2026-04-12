package com.dessalines.habitmaker.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.utils.HabitFrequency

enum class ThemeMode(
    @param:StringRes val resId: Int,
) {
    System(R.string.system),
    Light(R.string.light),
    Dark(R.string.dark),
}

enum class ThemeColor(
    @param:StringRes val resId: Int,
) {
    Dynamic(R.string.dynamic),
    Green(R.string.green),
    Pink(R.string.pink),
}

enum class HabitSort(
    @param:StringRes val resId: Int,
) {
    Streak(R.string.streak),
    Points(R.string.points),
    Score(R.string.score),

    /**
     * Whether its completed or not.
     */
    Status(R.string.status),
    DateCreated(R.string.date_created),
    Name(R.string.name),
}

enum class HabitSortOrder(
    @param:StringRes val resId: Int,
) {
    Descending(R.string.descending),
    Ascending(R.string.ascending),
}

/**
 * A habit status used for coloring the streak chips
 */
enum class HabitStatus {
    Normal,
    Silver,
    Gold,
    Platinum,
}

/**
 * A frequency picker for reminders
 */
enum class HabitReminderFrequency(
    @param:StringRes val resId: Int,
) {
    NoReminders(R.string.no_reminders),
    EveryDay(R.string.remind_every_day),
    SpecificDays(R.string.remind_specific_days),
}
