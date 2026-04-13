package com.dessalines.habitmaker.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.utils.HabitFrequency
import com.dessalines.habitmaker.db.utils.isCompletedToday
import com.dessalines.habitmaker.db.utils.toBool
import com.dessalines.habitmaker.ui.MainActivity

/**
 * Skeleton for complication data source that returns short text.
 */
class MainComplicationService : SuspendingComplicationDataSourceService() {
    private val database by lazy { AppDB.getDatabase(this) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        when (type) {
            ComplicationType.SHORT_TEXT -> {
                shortTextComplicationData(
                    text = "4",
                    contentDescription =
                        getText(R.string.remaining_habits).toString(),
                )
            }

            else -> {
                null
            }
        }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val habits =
            habitRepository.getAllSync
                // Filter the habits by today
                .filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Daily }
                // Don't count archived in the total for progress
                .filter { !it.archived.toBool() }

        // Check the completed count
        val completed = habits.count { isCompletedToday(it.lastCompletedTime) }

        val total = habits.size

        val remainingHabits = total - completed

        return shortTextComplicationData(
            text = remainingHabits.toString(),
            contentDescription =
                getText(R.string.remaining_habits).toString(),
        )
    }

    private fun shortTextComplicationData(
        text: String,
        contentDescription: String,
    ): ShortTextComplicationData {
        val tapIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                },
                PendingIntent.FLAG_IMMUTABLE,
            )

        return ShortTextComplicationData
            .Builder(
                text = PlainComplicationText.Builder(text).build(),
                contentDescription = PlainComplicationText.Builder(contentDescription).build(),
            ).setTapAction(tapIntent)
            .build()
    }
}
