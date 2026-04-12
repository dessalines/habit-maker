package com.dessalines.habitmaker

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.EncouragementRepository
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitReminderRepository
import com.dessalines.habitmaker.db.HabitRepository
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
import com.dessalines.habitmaker.ui.components.about.AboutScreen
import com.dessalines.habitmaker.ui.components.common.ShowChangelog
import com.dessalines.habitmaker.ui.components.habit.CreateHabitScreen
import com.dessalines.habitmaker.ui.components.habit.EditHabitScreen
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.HabitsAndDetailScreen
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.checkHabitForDay
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.updateStatsForHabit
import com.dessalines.habitmaker.ui.components.settings.BackupAndRestoreScreen
import com.dessalines.habitmaker.ui.components.settings.BehaviorScreen
import com.dessalines.habitmaker.ui.components.settings.LookAndFeelScreen
import com.dessalines.habitmaker.ui.components.settings.SettingsScreen
import com.dessalines.habitmaker.ui.theme.HabitMakerTheme
import com.dessalines.habitmaker.utils.TAG
import com.dessalines.habitmaker.utils.getVersionCode
import com.dessalines.habitmaker.utils.isCompletedLastCycle
import com.dessalines.habitmaker.utils.isCompletedToday
import com.dessalines.habitmaker.utils.toEpochMillis
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.AvailabilityException
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.woheller69.freeDroidWarn.FreeDroidWarn
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlin.getValue

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

    private val habitViewModel: HabitViewModel by viewModels {
        HabitViewModelFactory((application as HabitMakerApplication).habitRepository)
    }

    private val encouragementViewModel: EncouragementViewModel by viewModels {
        EncouragementViewModelFactory((application as HabitMakerApplication).encouragementRepository)
    }

    private val habitCheckViewModel: HabitCheckViewModel by viewModels {
        HabitCheckViewModelFactory((application as HabitMakerApplication).habitCheckRepository)
    }

    private val reminderViewModel: HabitReminderViewModel by viewModels {
        HabitReminderViewModelFactory((application as HabitMakerApplication).habitReminderRepository)
    }
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        FreeDroidWarn.showWarningOnUpgrade(this, getVersionCode())

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by appSettingsViewModel.appSettings
                .asLiveData()
                .observeAsState()

            val startDestination = "habits"

            val ctx = LocalContext.current
            createNotificationChannel(ctx)

            BroadcastReceivers(
                settings,
                habitViewModel,
                habitCheckViewModel,
                reminderViewModel,
            )

            var apiAvailable by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                updateHabitStatsOnStartup(ctx)
                apiAvailable = isAvailable(capabilityClient)
                Log.d(TAG, "Api available: $apiAvailable")
                sendToHandheldDevice(lifecycleScope, dataClient, "u turd")
            }

            HabitMakerTheme(
                settings = settings,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    ShowChangelog(appSettingsViewModel = appSettingsViewModel)

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable(
                            route = "habits?id={id}",
                            arguments =
                                listOf(
                                    navArgument("id") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    },
                                ),
                        ) {
                            val id = it.arguments?.getString("id")?.toInt()

                            HabitsAndDetailScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                                habitViewModel = habitViewModel,
                                encouragementViewModel = encouragementViewModel,
                                habitCheckViewModel = habitCheckViewModel,
                                reminderViewModel = reminderViewModel,
                                id = id,
                            )
                        }
                        composable(
                            route = "createHabit",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            CreateHabitScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                                habitViewModel = habitViewModel,
                                encouragementViewModel = encouragementViewModel,
                                reminderViewModel = reminderViewModel,
                            )
                        }
                        composable(
                            route = "editHabit/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType }),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val id = it.arguments?.getInt("id") ?: 0
                            EditHabitScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                                habitViewModel = habitViewModel,
                                encouragementViewModel = encouragementViewModel,
                                reminderViewModel = reminderViewModel,
                                id = id,
                            )
                        }

                        composable(
                            route = "settings",
                        ) {
                            SettingsScreen(
                                navController = navController,
                            )
                        }
                        composable(
                            route = "about",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            AboutScreen(
                                navController = navController,
                            )
                        }
                        composable(
                            route = "lookAndFeel",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            LookAndFeelScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                            )
                        }
                        composable(
                            route = "behavior",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            BehaviorScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                            )
                        }
                        composable(
                            route = "backupAndRestore",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            BackupAndRestoreScreen(
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Check habit streaks on startup.
     */
    fun updateHabitStatsOnStartup(ctx: Context) {
        val settings = appSettingsViewModel.appSettingsSync
        val firstDayOfWeek = settings.firstDayOfWeek

        cancelReminders(ctx)

        // Unfortunately this requires looping over every habit.
        habitViewModel.getAllSync.forEach { habit ->
            // Use virtual completed to check streaks, otherwise all streaks today will appear broken
            val isCompletedLastCycle = isCompletedLastCycle(habit)
            val isCompleted = isCompletedToday(habit.lastCompletedTime)

            if (!isCompletedLastCycle) {
                val checks = habitCheckViewModel.listForHabitSync(habit.id)
                val completedCount = settings.completedCount
                updateStatsForHabit(habit, habitViewModel, checks, completedCount, firstDayOfWeek)
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

fun sendToHandheldDevice(
    scope: LifecycleCoroutineScope,
    dataClient: DataClient,
    message: String,
) {
    scope.launch {
        if (isAvailable(dataClient)) {
            try {
                val result =
                    dataClient
                        .putDataItem(
                            PutDataMapRequest
                                .create("/message")
                                .apply {
                                    dataMap.putString("message", message)
                                    dataMap.putLong("time", Instant.now().epochSecond)
                                }.asPutDataRequest()
                                .setUrgent(),
                        ).await()

                Log.d(TAG, "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Saving DataItem failed: $exception")
            }
        }
    }
}

private fun enterAnimation(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
    {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
        )
    }

private fun exitAnimation(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
    {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
        )
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
                    checkHabitForDay(habitId, checkTime, habitCheckViewModel)
                    val checks = habitCheckViewModel.listForHabitSync(habitId)
                    updateStatsForHabit(habit, habitViewModel, checks, completedCount, firstDayOfWeek)
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

suspend fun isAvailable(api: GoogleApi<*>): Boolean =
    try {
        GoogleApiAvailability
            .getInstance()
            .checkApiAvailability(api)
            .await()

        true
    } catch (e: AvailabilityException) {
        Log.d(
            TAG,
            "${api.javaClass.simpleName} API is not available in this device.",
        )
        false
    }
