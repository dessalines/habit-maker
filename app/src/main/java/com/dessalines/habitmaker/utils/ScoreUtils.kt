package com.dessalines.habitmaker.utils

import android.util.Log
import com.dessalines.habitmaker.db.HabitCheck
import okhttp3.internal.toImmutableList
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class Streak(
    val begin: LocalDate,
    val end: LocalDate,
)

/**
 * Gives the length of a streak.
 */
fun Streak.duration(frequency: HabitFrequency): Long =
    Duration
        .between(
            this.begin.atStartOfDay(),
            this.end.atStartOfDay(),
        ).toDays()
        .plus(1)
        .div(frequency.toDays())

/**
 * Gives the length of the current streak.
 */
fun todayStreak(
    frequency: HabitFrequency,
    lastStreak: Streak?,
): Long {
    val todayStreak =
        lastStreak?.let {
            if (it.end >= LocalDate.now()) {
                it.duration(frequency)
            } else {
                0
            }
        } ?: 0
    return todayStreak
}

fun calculateStreaks(
    frequency: HabitFrequency,
    timesPerFrequency: Int,
    dates: List<LocalDate>,
): List<Streak> {
    val virtualDates = buildVirtualDates(frequency, timesPerFrequency, dates).sortedDescending()

    if (virtualDates.isEmpty()) {
        return emptyList()
    }

    var begin = virtualDates[0]
    var end = virtualDates[0]

    val streaks = mutableListOf<Streak>()
    for (i in 1 until virtualDates.size) {
        val current = virtualDates[i]
        if (current == begin.minusDays(1)) {
            begin = current
        } else {
            streaks.add(Streak(begin, end))
            begin = current
            end = current
        }
    }
    streaks.add(Streak(begin, end))
    streaks.reverse()
    Log.d(TAG, streaks.joinToString { "${it.begin} - ${it.end}" })

    return streaks.toImmutableList()
}

/**
 * For habits with weeks / months / years and times per frequency,
 * you need to create "virtual" dates.
 */
fun buildVirtualDates(
    frequency: HabitFrequency,
    timesPerFrequency: Int,
    dates: List<LocalDate>,
): List<LocalDate> =
    when (frequency) {
        HabitFrequency.Daily -> dates
        else -> {
            val virtualDates = mutableListOf<LocalDate>()
            val completedRanges = mutableListOf<LocalDate>()

            var rangeFirstDate =
                when (frequency) {
                    HabitFrequency.Weekly ->
                        dates.firstOrNull()?.with(
                            TemporalAdjusters.previousOrSame(
                                DayOfWeek.SUNDAY,
                            ),
                        )
                    HabitFrequency.Monthly -> dates.firstOrNull()?.withDayOfMonth(1)
                    HabitFrequency.Yearly -> dates.firstOrNull()?.withDayOfYear(1)
                    else -> null
                }

            var count = 0

            dates.forEach { entry ->
                virtualDates.add(entry)
                val entryRange =
                    when (frequency) {
                        HabitFrequency.Weekly ->
                            entry.with(
                                TemporalAdjusters.previousOrSame(
                                    DayOfWeek.SUNDAY,
                                ),
                            )
                        HabitFrequency.Monthly -> entry.withDayOfMonth(1)
                        HabitFrequency.Yearly -> entry.withDayOfYear(1)
                        else -> entry
                    }
                if (entryRange == rangeFirstDate && !completedRanges.contains(entryRange)) {
                    count++
                } else {
                    rangeFirstDate = entryRange
                    count = 1
                }
                if (count >= timesPerFrequency) completedRanges.add(entryRange)
            }

            // Months have a special case where it should use the max days possible in a month,
            // not 28.
            val maxDays =
                when (frequency) {
                    HabitFrequency.Monthly -> 31
                    else -> frequency.toDays()
                }.minus(1)

            completedRanges.forEach { start ->
                (0..maxDays).forEach { offset ->
                    val date = start.plusDays(offset.toLong())
                    if (!virtualDates.any { it == date }) {
                        virtualDates.add(date)
                    }
                }
            }
            virtualDates.toImmutableList()
        }
    }

/**
 * Get a bonus points for each day that the streak is long.
 *
 * Called nth triangle number:
 * https://math.stackexchange.com/a/593320
 */
fun calculatePoints(
    frequency: HabitFrequency,
    streaks: List<Streak>,
): Long {
    var points = 0L

    streaks.forEach {
        val duration = it.duration(frequency)
        points += duration.nthTriangle()
    }
    return points
}

fun Long.nthTriangle() = (this * this + this) / 2

/**
 * The percent complete score.
 *
 * Calculated using the # of times you've done it.
 */
fun calculateScore(
    habitChecks: List<HabitCheck>,
    completedCount: Int,
): Int = (100 * habitChecks.size).div(completedCount)

/**
 * Determines whether a habit is completed or not. Virtual means that entries
 * may be fake, from the streak calculations, to account for non-daily habits.
 *
 * A weekly habit might be satisfied for this week, so although it wasn't checked today,
 * it might complete for the week.
 *
 * Used for filtering out virtually completed habits.
 */
fun isVirtualCompleted(lastStreakTime: Long) = lastStreakTime >= LocalDate.now().toEpochMillis()

/**
 * Determines whether a habit is completed today or not.
 */
fun isCompletedToday(lastCompletedTime: Long) = lastCompletedTime == LocalDate.now().toEpochMillis()

/**
 * Determines whether a habit is completed yesterday.
 */
fun isCompletedYesterday(lastCompletedTime: Long) = lastCompletedTime == LocalDate.now().minusDays(1).toEpochMillis()
