package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettingsViewModel
import com.dessalines.habitmaker.db.Encouragement
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitCheck
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitReminderViewModel
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.db.SettingsUpdateHideCompleted
import com.dessalines.habitmaker.notifications.deleteRemindersForHabit
import com.dessalines.habitmaker.notifications.scheduleRemindersForHabit
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.SUCCESS_EMOJIS
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.calculatePoints
import com.dessalines.habitmaker.utils.calculateScore
import com.dessalines.habitmaker.utils.calculateStreaks
import com.dessalines.habitmaker.utils.epochMillisToLocalDate
import com.dessalines.habitmaker.utils.isCompletedToday
import com.dessalines.habitmaker.utils.nthTriangle
import com.dessalines.habitmaker.utils.toEpochMillis
import com.dessalines.habitmaker.utils.toInt
import com.dessalines.habitmaker.utils.todayStreak
import com.dessalines.prettyFormat
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun HabitsAndDetailScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel,
    habitViewModel: HabitViewModel,
    encouragementViewModel: EncouragementViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    reminderViewModel: HabitReminderViewModel,
    id: Int?,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val paneExpansionState = rememberPaneExpansionState()
    paneExpansionState.setFirstPaneProportion(0.4f)

    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()
    val completedCount = settings?.completedCount ?: 0
    val defaultEncouragements = buildDefaultEncouragements()
    val firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY

    var selectedHabitId: Int? by rememberSaveable { mutableStateOf(id) }
    val habits by habitViewModel.getAll.asLiveData().observeAsState()

    val habitsPaneListState = rememberLazyListState()
    val habitsPaneScrollBehavior =
        TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Companion.Expanded &&
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Companion.Expanded
    val isDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Companion.Expanded

    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    SharedTransitionLayout {
        AnimatedContent(targetState = isListAndDetailVisible, label = "simple sample") {
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                paneExpansionState = paneExpansionState,
                listPane = {
                    val currentSelectedHabitId = selectedHabitId
                    val selectionState =
                        if (isDetailVisible && currentSelectedHabitId != null) {
                            SelectionVisibilityState.ShowSelection(currentSelectedHabitId)
                        } else {
                            SelectionVisibilityState.NoSelection
                        }

                    AnimatedPane {
                        HabitsPane(
                            habits = habits,
                            listState = habitsPaneListState,
                            scrollBehavior = habitsPaneScrollBehavior,
                            settings = settings,
                            snackbarHostState = snackbarHostState,
                            onHabitClick = { habitId ->
                                selectedHabitId = habitId
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            },
                            onHabitCheck = { habitId ->
                                val habit = habits?.find { it.id == habitId }
                                habit?.let { habit ->
                                    val checkTime = LocalDate.now().toEpochMillis()
                                    checkHabitForDay(habitId, checkTime, habitCheckViewModel)
                                    val checks = habitCheckViewModel.listForHabitSync(habitId)
                                    val stats =
                                        updateStatsForHabit(
                                            habit,
                                            habitViewModel,
                                            checks,
                                            completedCount,
                                            firstDayOfWeek,
                                        )

                                    // If successful, show a random encouragement
                                    val isCompleted = isCompletedToday(stats.lastCompletedTime)
                                    if (isCompleted) {
                                        val randomEncouragement =
                                            encouragementViewModel.getRandomForHabit(habitId)
                                                ?: defaultEncouragements.random()
                                        val frequency = HabitFrequency.entries[habit.frequency]
                                        val congratsMessage =
                                            buildCongratsSnackMessage(
                                                ctx = ctx,
                                                stats = stats,
                                                frequency = frequency,
                                                encouragement = randomEncouragement,
                                            )
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = congratsMessage,
                                                withDismissAction = true,
                                            )
                                        }
                                    }
                                    // Reschedule the reminders, to skip completed today
                                    val reminders = reminderViewModel.listForHabitSync(habit.id)
                                    scheduleRemindersForHabit(
                                        ctx,
                                        reminders,
                                        habit.name,
                                        habit.id,
                                        isCompleted,
                                    )
                                }
                            },
                            selectionState = selectionState,
                            onCreateHabitClick = {
                                navController.navigate("createHabit")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            },
                            onHideCompletedClick = {
                                appSettingsViewModel.updateHideCompleted(
                                    SettingsUpdateHideCompleted(
                                        id = 1,
                                        hideCompleted = it.toInt(),
                                    ),
                                )
                            },
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        selectedHabitId?.let { habitId ->

                            val habit by habitViewModel
                                .getById(habitId)
                                .asLiveData()
                                .observeAsState()
                            val habitChecks by habitCheckViewModel
                                .listForHabit(habitId)
                                .asLiveData()
                                .observeAsState()

                            habit?.let { habit ->
                                HabitDetailPane(
                                    habit = habit,
                                    habitChecks = habitChecks.orEmpty(),
                                    firstDayOfWeek = firstDayOfWeek,
                                    isListAndDetailVisible = isListAndDetailVisible,
                                    onEditClick = {
                                        navController.navigate("editHabit/${habit.id}")
                                    },
                                    onBackClick = {
                                        scope.launch {
                                            navigator.navigateBack()
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            deleteRemindersForHabit(ctx, habitId)
                                            habitViewModel.delete(habit)
                                            navigator.navigateBack()
                                        }
                                    },
                                    onHabitCheck = { localDate ->
                                        val habit = habits?.find { it.id == habitId }
                                        habit?.let { habit ->
                                            val checkTime =
                                                localDate
                                                    .atStartOfDay(ZoneId.systemDefault())
                                                    .toInstant()
                                                    .toEpochMilli()
                                            checkHabitForDay(
                                                habit.id,
                                                checkTime,
                                                habitCheckViewModel,
                                            )
                                            val checks =
                                                habitCheckViewModel.listForHabitSync(habitId)
                                            val stats =
                                                updateStatsForHabit(
                                                    habit,
                                                    habitViewModel,
                                                    checks,
                                                    completedCount,
                                                    firstDayOfWeek,
                                                )
                                            // Reschedule the reminders, to skip completed today
                                            val isCompleted = isCompletedToday(stats.lastCompletedTime)
                                            val reminders = reminderViewModel.listForHabitSync(habit.id)
                                            scheduleRemindersForHabit(
                                                ctx,
                                                reminders,
                                                habit.name,
                                                habit.id,
                                                isCompleted,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

/**
 * Checks / toggles a habit for a given check time.
 *
 * If it already exists, it deletes the row in order to toggle it.
 *
 * returns true if successful / check, false for deleted check.
 */
fun checkHabitForDay(
    habitId: Int,
    checkTime: Long,
    habitCheckViewModel: HabitCheckViewModel,
) {
    val insert =
        HabitCheckInsert(
            habitId = habitId,
            checkTime = checkTime,
        )
    val success = habitCheckViewModel.insert(insert)

    // If its -1, that means that its already been checked for today,
    // and you actually need to delete it to toggle
    if (success == -1L) {
        habitCheckViewModel.deleteForDay(habitId, checkTime)
    }
}

fun updateStatsForHabit(
    habit: Habit,
    habitViewModel: HabitViewModel,
    checks: List<HabitCheck>,
    completedCount: Int,
    firstDayOfWeek: DayOfWeek,
): HabitUpdateStats {
    val dateChecks = checks.map { it.checkTime.epochMillisToLocalDate() }

    val frequency = HabitFrequency.entries[habit.frequency]

    // Calculating a few totals
    val streaks = calculateStreaks(frequency, habit.timesPerFrequency, dateChecks, firstDayOfWeek)
    val points = calculatePoints(frequency, streaks)
    val score = calculateScore(checks, completedCount)

    // The last streak time can be in the future for non-daily habits,
    // and is used for filtering / hiding.
    val lastStreakTime = streaks.lastOrNull()?.end?.toEpochMillis() ?: 0

    // The last completed time is used to see which habits have been checked today.
    val lastCompletedTime = checks.lastOrNull()?.checkTime ?: 0
    val todayStreak = todayStreak(frequency, streaks.lastOrNull())

    val statsUpdate =
        HabitUpdateStats(
            id = habit.id,
            points = points.toInt(),
            score = score,
            streak = todayStreak.toInt(),
            lastStreakTime = lastStreakTime,
            lastCompletedTime = lastCompletedTime,
            completed = checks.size,
        )
    habitViewModel.updateStats(statsUpdate)

    return statsUpdate
}

fun buildCongratsSnackMessage(
    ctx: Context,
    stats: HabitUpdateStats,
    frequency: HabitFrequency,
    encouragement: Encouragement,
): String {
    val randomSuccessEmoji = SUCCESS_EMOJIS.random()
    val congratsLine = randomSuccessEmoji + " " + encouragement.content
    val messages = mutableListOf(congratsLine)
    val todayPoints = stats.streak.toLong().nthTriangle()

    val resId =
        when (frequency) {
            HabitFrequency.Daily -> R.string.youre_on_a_x_day_streak
            HabitFrequency.Weekly -> R.string.youre_on_a_x_week_streak
            HabitFrequency.Monthly -> R.string.youre_on_a_x_month_streak
            HabitFrequency.Yearly -> R.string.youre_on_a_x_year_streak
        }
    if (stats.streak > 0) {
        messages.add(
            ctx.getString(
                resId,
                prettyFormat(stats.streak),
                prettyFormat(todayPoints),
            ),
        )
    }

    return messages.joinToString("\n")
}

@Composable
fun buildDefaultEncouragements() =
    listOf(
        stringResource(R.string.default_encouragement_1),
        stringResource(R.string.default_encouragement_2),
        stringResource(R.string.default_encouragement_3),
    ).map {
        Encouragement(
            id = 0,
            habitId = 0,
            content = it,
        )
    }
