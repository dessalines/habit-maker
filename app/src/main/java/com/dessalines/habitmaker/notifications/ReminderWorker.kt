package com.dessalines.habitmaker.notifications

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dessalines.habitmaker.MainActivity
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.utils.TAG

class ReminderWorker(
    ctx: Context,
    workerParams: WorkerParameters,
) : Worker(ctx, workerParams) {
    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        val habitTitle = inputData.getString(HABIT_TITLE_KEY)
        val habitId = inputData.getInt(HABIT_ID_KEY, 0)

        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val mainIntent =
            Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val mainPI: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                mainIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )
        val checkHabitIntent =
            Intent(CHECK_HABIT_INTENT_ACTION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(CHECK_HABIT_INTENT_HABIT_ID, habitId)
            }
        val checkHabitPI: PendingIntent =
            PendingIntent.getBroadcast(applicationContext, habitId, checkHabitIntent, PendingIntent.FLAG_IMMUTABLE)

        val cancelHabitIntent =
            Intent(CANCEL_HABIT_INTENT_ACTION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(CANCEL_HABIT_INTENT_HABIT_ID, habitId)
                putExtra("clap", "42")
                putExtra("num", 43)
            }
        val cancelHabitPI: PendingIntent =
            PendingIntent.getBroadcast(applicationContext, habitId, cancelHabitIntent, PendingIntent.FLAG_IMMUTABLE)

        val body = applicationContext.getString(R.string.did_you_complete)
        val builder =
            NotificationCompat
                .Builder(applicationContext, TAG)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(habitTitle)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(mainPI)
                .setAutoCancel(true)
                // The yes and no actions
                .addAction(0, applicationContext.getString(R.string.yes), checkHabitPI)
                .addAction(0, applicationContext.getString(R.string.no), cancelHabitPI)

        notificationManager.notify(habitId, builder.build())

        return Result.success()
    }
}
