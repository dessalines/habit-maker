package com.dessalines.habitmaker.notifications

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dessalines.habitmaker.db.HabitReminder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

fun cancelReminders(ctx: Context) {
    val workManager = WorkManager.getInstance(ctx)
    workManager.cancelAllWork()
}

fun scheduleRemindersForHabit(
    ctx: Context,
    reminders: List<HabitReminder>,
    habitName: String,
    habitId: Int,
    skipToday: Boolean,
) {
    deleteRemindersForHabit(ctx, habitId)

    // Schedule them again
    reminders.forEach { reminder ->
        scheduleReminderForHabit(ctx, reminder, habitName, habitId, skipToday)
    }
}

private fun scheduleReminderForHabit(
    ctx: Context,
    reminder: HabitReminder,
    habitName: String,
    habitId: Int,
    skipToday: Boolean,
) {
    val workManager = WorkManager.getInstance(ctx)

    val myWorkRequestBuilder = OneTimeWorkRequestBuilder<ReminderWorker>()

    val adjuster =
        if (skipToday) {
            TemporalAdjusters.next(reminder.day)
        } else {
            TemporalAdjusters.nextOrSame(reminder.day)
        }

    // Work manager cant handle specific times, so you have to use diffs from now.
    val nextDate = LocalDate.now().with(adjuster)

    val scheduledTime = reminder.time.atDate(nextDate)
    val diff =
        scheduledTime.toEpochSecond(ZoneOffset.UTC) -
            LocalDateTime
                .now()
                .toEpochSecond(ZoneOffset.UTC)

    // Only schedule it if the diff > 0
    if (diff > 0) {
        myWorkRequestBuilder
            .setInputData(workDataOf(HABIT_TITLE_KEY to habitName, HABIT_ID_KEY to habitId))
            // Only milliseconds seems to work here
            .setInitialDelay(diff * 1000, TimeUnit.MILLISECONDS)
            // Add the habit id as the tag, so they can all be canceled at once
            .addTag(habitId.toString())
        workManager.enqueue(myWorkRequestBuilder.build())
    }
}

fun deleteRemindersForHabit(
    ctx: Context,
    habitId: Int,
) {
    val workManager = WorkManager.getInstance(ctx)

    // Cancel work for the current habit
    workManager.cancelAllWorkByTag(habitId.toString())
}
