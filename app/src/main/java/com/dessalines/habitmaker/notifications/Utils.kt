package com.dessalines.habitmaker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.dessalines.habitmaker.utils.TAG

const val CHECK_HABIT_INTENT_ACTION = "check-habit"
const val CHECK_HABIT_INTENT_HABIT_ID = "check-habit-id"
const val CANCEL_HABIT_INTENT_ACTION = "cancel-habit"
const val CANCEL_HABIT_INTENT_HABIT_ID = "cancel-habit-id"
const val HABIT_TITLE_KEY = "habit-title"
const val HABIT_ID_KEY = "habit-id"

fun createNotificationChannel(ctx: Context) {
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(TAG, TAG, importance)
    // Register the channel with the system
    val notificationManager = ctx.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

@Composable
fun SystemBroadcastReceiver(
    systemAction: String,
    onSystemEvent: (intent: Intent?) -> Unit,
) {
    val context = LocalContext.current
    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    DisposableEffect(context, systemAction) {
        val intentFilter = IntentFilter(systemAction)

        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    currentOnSystemEvent(intent)
                }
            }

        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED,
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}
