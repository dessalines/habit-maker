package com.dessalines.habitmaker.utils

import androidx.annotation.StringRes
import com.dessalines.habitmaker.R

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
