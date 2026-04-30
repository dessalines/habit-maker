package com.dessalines.habitmaker

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.dessalines.habitmaker.datalayer.syncDBtoOtherDevices
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.EncouragementRepository
import com.dessalines.habitmaker.db.HabitCheckDelete
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitReminderRepository
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.utils.isCompletedLastCycle
import com.dessalines.habitmaker.db.utils.isCompletedToday
import com.dessalines.habitmaker.db.utils.toEpochMillis
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.EncouragementViewModel
import com.dessalines.habitmaker.db.viewmodels.EncouragementViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitReminderViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitReminderViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModelFactory
import com.dessalines.habitmaker.notifications.CANCEL_HABIT_INTENT_ACTION
import com.dessalines.habitmaker.notifications.CANCEL_HABIT_INTENT_HABIT_ID
import com.dessalines.habitmaker.notifications.CHECK_HABIT_INTENT_ACTION
import com.dessalines.habitmaker.notifications.CHECK_HABIT_INTENT_HABIT_ID
import com.dessalines.habitmaker.notifications.SystemBroadcastReceiver
import com.dessalines.habitmaker.notifications.cancelReminders
import com.dessalines.habitmaker.notifications.createNotificationChannel
import com.dessalines.habitmaker.notifications.scheduleRemindersForHabit
import com.dessalines.habitmaker.ui.components.Main
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.checkHabitForDay
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.updateStatsForHabit
import com.dessalines.habitmaker.ui.theme.HabitMakerTheme
import com.dessalines.habitmaker.utils.getVersionCode
import com.google.android.gms.wearable.Wearable
import org.woheller69.freeDroidWarn.FreeDroidWarn
import java.time.DayOfWeek
import java.time.LocalDate

class HabitMakerApplication : Application() {
    private val database by lazy { AppDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val encouragementRepository by lazy { EncouragementRepository(database.encouragementDao()) }
    val habitCheckRepository by lazy { HabitCheckRepository(database.habitCheckDao()) }
    val habitReminderRepository by lazy { HabitReminderRepository(database.habitReminderDao()) }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    private val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory((application as HabitMakerApplication).appSettingsRepository)
    }

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val habitViewModel: HabitViewModel by viewModels {
        HabitViewModelFactory((application as HabitMakerApplication).habitRepository, dataClient)
    }

    private val encouragementViewModel: EncouragementViewModel by viewModels {
        EncouragementViewModelFactory((application as HabitMakerApplication).encouragementRepository)
    }

    private val habitCheckViewModel: HabitCheckViewModel by viewModels {
        HabitCheckViewModelFactory((application as HabitMakerApplication).habitCheckRepository, dataClient)
    }

    private val reminderViewModel: HabitReminderViewModel by viewModels {
        HabitReminderViewModelFactory((application as HabitMakerApplication).habitReminderRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        FreeDroidWarn.showWarningOnUpgrade(this, getVersionCode())

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by appSettingsViewModel.appSettings
                .asLiveData()
                .observeAsState()

            val ctx = LocalContext.current
            createNotificationChannel(ctx)

            BroadcastReceivers(
                settings,
                habitViewModel,
                habitCheckViewModel,
                reminderViewModel,
            )

            LaunchedEffect(Unit) {
                updateHabitStatsOnStartup(ctx)
                syncDBtoOtherDevices(
                    habitViewModel,
                    habitCheckViewModel,
                    dataClient,
                    lifecycleScope,
                )
            }

            HabitMakerTheme(
                settings = settings,
            ) {
                Main(
                    appSettingsViewModel = appSettingsViewModel,
                    habitViewModel = habitViewModel,
                    habitCheckViewModel = habitCheckViewModel,
                    encouragementViewModel = encouragementViewModel,
                    reminderViewModel = reminderViewModel,
                )
            }
        }
    }

    /**
     * Check habit streaks on startup.
     */
    fun updateHabitStatsOnStartup(ctx: Context) {
        cancelReminders(ctx)

        appSettingsViewModel.appSettingsSync?.let { settings ->

            // Unfortunately this requires looping over every habit.
            habitViewModel.getAllSync.forEach { habit ->
                // Use virtual completed to check streaks, otherwise all streaks today will appear broken
                val isCompletedLastCycle = isCompletedLastCycle(habit)
                val isCompleted = isCompletedToday(habit.lastCompletedTime)

                if (!isCompletedLastCycle) {
                    val checks = habitCheckViewModel.listForHabitSync(habit.id)
                    val completedCount = settings.completedCount
                    updateStatsForHabit(
                        habit = habit,
                        habitViewModel = habitViewModel,
                        checks = checks,
                        completedCount = completedCount,
                        firstDayOfWeek = settings.firstDayOfWeek,
                    )
                }
                // Reschedule the reminders, to skip today, or if its already virtual completed
                val reminders = reminderViewModel.listForHabitSync(habit.id)

                scheduleRemindersForHabit(
                    ctx,
                    reminders,
                    habit.name,
                    habit.id,
                    isCompleted || isCompletedLastCycle,
                )
            }
        }
    }
}

/**
 * This receives the check yes and check no actions from the notifications.
 */
@Composable
fun BroadcastReceivers(
    settings: AppSettings?,
    habitViewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    reminderViewModel: HabitReminderViewModel,
) {
    val ctx = LocalContext.current

    SystemBroadcastReceiver(CHECK_HABIT_INTENT_ACTION) { intent ->
        if (intent?.action == CHECK_HABIT_INTENT_ACTION) {
            val habitId = intent.getIntExtra(CHECK_HABIT_INTENT_HABIT_ID, 0)
            val firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY

            // Check the habit
            habitViewModel.getByIdSync(habitId)?.let { habit ->
                val checkTime = LocalDate.now().toEpochMillis()
                val completedCount = settings?.completedCount ?: 0

                val isCompleted = isCompletedToday(habit.lastCompletedTime)
                // Only check the habit if it hasn't been checked
                if (!isCompleted) {
                    val insertedId = checkHabitForDay(habitId, checkTime, habitCheckViewModel)
                    val checks = habitCheckViewModel.listForHabitSync(habitId)
                    val stats =
                        updateStatsForHabit(
                            habit = habit,
                            habitViewModel = habitViewModel,
                            checks = checks,
                            completedCount = completedCount,
                            firstDayOfWeek = firstDayOfWeek,
                        )
                    if (insertedId == -1L) {
                        habitCheckViewModel.sendHabitCheckDeleteAndStatsUpdate(
                            habitCheckDelete =
                                HabitCheckDelete(
                                    habitId = habitId,
                                    checkTime = checkTime,
                                ),
                            stats = stats,
                        )
                    } else {
                        habitCheckViewModel.sendHabitCheckInsertAndStatsUpdate(
                            insertedId = insertedId,
                            habitCheck =
                                HabitCheckInsert(
                                    habitId = habitId,
                                    checkTime = checkTime,
                                ),
                            stats = stats,
                        )
                    }
                }

                // Reschedule the reminders, to skip today
                val reminders = reminderViewModel.listForHabitSync(habit.id)
                scheduleRemindersForHabit(
                    ctx,
                    reminders,
                    habit.name,
                    habit.id,
                    isCompleted,
                )

                // Cancel the notif
                NotificationManagerCompat.from(ctx).cancel(habitId)
            }
        }
    }

    SystemBroadcastReceiver(CANCEL_HABIT_INTENT_ACTION) { intent ->
        if (intent?.action == CANCEL_HABIT_INTENT_ACTION) {
            val habitId = intent.getIntExtra(CANCEL_HABIT_INTENT_HABIT_ID, 0)

            // Cancel the notif
            NotificationManagerCompat.from(ctx).cancel(habitId)
        }
    }
}
