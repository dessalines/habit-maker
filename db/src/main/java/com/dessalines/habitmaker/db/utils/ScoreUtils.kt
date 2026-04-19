package com.dessalines.habitmaker.db.utils

import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Habit
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
    firstDayOfWeek: DayOfWeek,
): List<Streak> {
    val virtualDates = buildVirtualDates(frequency, timesPerFrequency, dates, firstDayOfWeek).sortedDescending()

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
//    Log.d(TAG, streaks.joinToString { "${it.begin} - ${it.end}" })

    return streaks
}

/**
 * For habits with weeks / months / years and times per frequency,
 * you need to create "virtual" dates.
 */
fun buildVirtualDates(
    frequency: HabitFrequency,
    timesPerFrequency: Int,
    dates: List<LocalDate>,
    firstDayOfWeek: DayOfWeek,
): List<LocalDate> =
    when (frequency) {
        HabitFrequency.Daily -> {
            dates
        }

        else -> {
            val virtualDates = mutableListOf<LocalDate>()
            val completedRanges = mutableListOf<LocalDate>()

            var rangeFirstDate =
                when (frequency) {
                    HabitFrequency.Weekly -> {
                        dates.firstOrNull()?.with(
                            TemporalAdjusters.previousOrSame(
                                firstDayOfWeek,
                            ),
                        )
                    }

                    HabitFrequency.Monthly -> {
                        dates.firstOrNull()?.withDayOfMonth(1)
                    }

                    HabitFrequency.Yearly -> {
                        dates.firstOrNull()?.withDayOfYear(1)
                    }
                }

            var count = 0

            dates.forEach { entry ->
                virtualDates.add(entry)
                val entryRange =
                    when (frequency) {
                        HabitFrequency.Weekly -> {
                            entry.with(
                                TemporalAdjusters.previousOrSame(
                                    firstDayOfWeek,
                                ),
                            )
                        }

                        HabitFrequency.Monthly -> {
                            entry.withDayOfMonth(1)
                        }

                        HabitFrequency.Yearly -> {
                            entry.withDayOfYear(1)
                        }
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
            virtualDates
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
 * Calculated using the # of times you've done it out of the past completed count.
 */
fun calculateScore(
    frequency: HabitFrequency,
    timesPerFrequency: Int,
    dates: List<LocalDate>,
    completedCount: Int,
): Int {
    // The firstCheckDate needs to be the completed count * frequency into the past from today.
    val firstCheckDate = LocalDate.now().minusDays(completedCount.times(frequency.toDays().toLong()))

    // Count the number of completed dates on or after that first check date
    val completedDaysAfterFirstCheck = dates.filter { it.isAfter(firstCheckDate) }.size

    // The number of checks should be times the frequency
    val intendedCheckCount = completedCount.times(timesPerFrequency)

    // Force a min of 100, because weekly / yearly can go way over that
    val score = minOf(100, completedDaysAfterFirstCheck.times(100).div(intendedCheckCount))

    return score
}

/**
 * Determines whether a habit is completed or not. Virtual means that entries
 * may be fake, from the streak calculations, to account for non-daily habits.
 *
 * A weekly habit might be satisfied for this week, so although it wasn't checked today,
 * it might complete for the week.
 *
 * Used for filtering out virtually completed habits.
 */
fun isCompletedLastCycle(habit: Habit): Boolean {
    val frequency = HabitFrequency.entries[habit.frequency]
    val now = LocalDate.now()

    val lastCycleCheck =
        when (frequency) {
            HabitFrequency.Daily -> now.minusDays(1)
            HabitFrequency.Weekly -> now.minusWeeks(1)
            HabitFrequency.Monthly -> now.minusMonths(1)
            HabitFrequency.Yearly -> now.minusYears(1)
        }

    // Use the last virtual completed time
    val lastDate = habit.lastCompletedTime.epochMillisToLocalDate()
    return lastDate >= lastCycleCheck
}

fun isCompletedCurrentCycle(
    habit: Habit,
    firstDayOfWeek: DayOfWeek,
): Boolean {
    val frequency = HabitFrequency.entries[habit.frequency]
    val now = LocalDate.now()

    val check =
        when (frequency) {
            HabitFrequency.Daily -> {
                now
            }

            HabitFrequency.Weekly -> {
                now.with(
                    TemporalAdjusters.previousOrSame(
                        firstDayOfWeek,
                    ),
                )
            }

            HabitFrequency.Monthly -> {
                now.withDayOfMonth(1)
            }

            HabitFrequency.Yearly -> {
                now.withDayOfYear(1)
            }
        }

    // Use the last virtual completed time
    val lastDate = habit.lastCompletedTime.epochMillisToLocalDate()
    return lastDate >= check
}

/**
 * Determines whether a habit is completed today or not.
 */
fun isCompletedToday(lastCompletedTime: Long) = lastCompletedTime == LocalDate.now().toEpochMillis()

data class HabitGroupData(
    val frequency: HabitFrequency,
    val filteredList: List<Habit>,
    val completed: Int,
    val total: Int,
)

fun filterAndSortHabits(
    habits: List<Habit>,
    settings: AppSettings?,
): List<Habit> {
    val tmp = habits.toMutableList()

    // Hide completed
    if ((settings?.hideCompleted ?: 0).toBool()) {
        tmp.removeAll { isCompletedCurrentCycle(it, settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY) }
    }

    // Hide archived
    if ((settings?.hideArchived ?: 0).toBool()) {
        tmp.removeAll { it.archived.toBool() }
    }

    // Sorting
    val sortSetting = HabitSort.entries[settings?.sort ?: 0]
    when (sortSetting) {
        HabitSort.Name -> {
            tmp.sortBy { it.name }
        }

        HabitSort.Points -> {
            tmp.sortBy { it.points }
        }

        HabitSort.Score -> {
            tmp.sortWith(compareBy({ it.score }, { it.points }))
        }

        HabitSort.Streak -> {
            tmp.sortWith(compareBy({ it.streak }, { it.points }))
        }

        HabitSort.Status -> {
            tmp.sortWith(
                compareBy({ isCompletedCurrentCycle(it, settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY) }, { it.points }),
            )
        }

        HabitSort.DateCreated -> {
            tmp.sortBy { it.id }
        }
    }
    val sortOrder = HabitSortOrder.entries[settings?.sortOrder ?: 0]
    if (sortOrder == HabitSortOrder.Descending) {
        tmp.reverse()
    }

    return tmp
}

fun buildHabitsByFrequency(
    habits: List<Habit>,
    settings: AppSettings?,
) = listOf(
    calculateHabitGroupData(
        frequency = HabitFrequency.Daily,
        habits = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Daily },
        settings,
    ),
    calculateHabitGroupData(
        frequency = HabitFrequency.Weekly,
        habits = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Weekly },
        settings,
    ),
    calculateHabitGroupData(
        frequency = HabitFrequency.Monthly,
        habits = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Monthly },
        settings,
    ),
    calculateHabitGroupData(
        frequency = HabitFrequency.Yearly,
        habits = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Yearly },
        settings,
    ),
)

fun calculateHabitGroupData(
    frequency: HabitFrequency,
    habits: List<Habit>,
    settings: AppSettings?,
) = HabitGroupData(
    frequency = frequency,
    completed = habits.count { isCompletedToday(it.lastCompletedTime) },
    // Don't count archived in the total for progress
    total = habits.filter { !it.archived.toBool() }.size,
    filteredList = filterAndSortHabits(habits, settings),
)
