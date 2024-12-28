package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
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
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Encouragement
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.HabitCheck
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.utils.SUCCESS_EMOJIS
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.calculatePoints
import com.dessalines.habitmaker.utils.calculateScore
import com.dessalines.habitmaker.utils.calculateStreaks
import com.dessalines.habitmaker.utils.epochMillisToLocalDate
import com.dessalines.habitmaker.utils.nthTriangle
import com.dessalines.habitmaker.utils.toEpochMillis
import com.dessalines.habitmaker.utils.toInt
import com.dessalines.habitmaker.utils.todayStreak
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun HabitsAndDetailScreen(
    navController: NavController,
    settings: AppSettings?,
    habitViewModel: HabitViewModel,
    encouragementViewModel: EncouragementViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    id: Int?,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val paneExpansionState = rememberPaneExpansionState()
    paneExpansionState.setFirstPaneProportion(0.4f)

    val completedCount = settings?.completedCount ?: 0
    val defaultEncouragements = buildDefaultEncouragements()

    var selectedHabitId: Int? by rememberSaveable { mutableStateOf(id) }
    val habits by habitViewModel.getAll.asLiveData().observeAsState()

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
                            snackbarHostState = snackbarHostState,
                            onHabitClick = { habitId ->
                                selectedHabitId = habitId
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            },
                            onHabitCheck = { habitId ->
                                val checkTime = LocalDate.now().toEpochMillis()
                                checkHabitForDay(habitId, checkTime, habitCheckViewModel)
                                val checks = habitCheckViewModel.listForHabitSync(habitId)
                                val todayStats = updateStatsForHabit(habitId, habitViewModel, checks, completedCount)

                                // If successful, show a random encouragement
                                if (todayStats.completed) {
                                    val randomEncouragement =
                                        encouragementViewModel.getRandomForHabit(habitId) ?: defaultEncouragements.random()
                                    val congratsMessage = buildCongratsSnackMessage(ctx, todayStats, randomEncouragement)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(congratsMessage)
                                    }
                                }
                            },
                            selectionState = selectionState,
                            isListAndDetailVisible = isListAndDetailVisible,
                            onCreateHabitClick = {
                                navController.navigate("createHabit")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
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
                                            habitViewModel.delete(habit)
                                            navigator.navigateBack()
//                                        Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onHabitCheck = {
                                        val checkTime = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                        checkHabitForDay(habit.id, checkTime, habitCheckViewModel)
                                        val checks = habitCheckViewModel.listForHabitSync(habitId)
                                        updateStatsForHabit(habit.id, habitViewModel, checks, completedCount)
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

data class HabitTodayStats(
    val completed: Boolean,
    val streak: Long,
    val points: Long,
)

fun updateStatsForHabit(
    habitId: Int,
    habitViewModel: HabitViewModel,
    checks: List<HabitCheck>,
    completedCount: Int,
): HabitTodayStats {
    val dateChecks = checks.map { it.checkTime.epochMillisToLocalDate() }
    val todayDate = LocalDate.now()

    val streaks = calculateStreaks(checks)
    val points = calculatePoints(streaks)
    val score = calculateScore(checks, completedCount)

    val todayCompleted = dateChecks.lastOrNull() == todayDate
    val todayStreak = todayStreak(streaks, todayDate)
    val streakPoints = todayStreak.nthTriangle()

    val statsUpdate =
        HabitUpdateStats(
            id = habitId,
            points = points.toInt(),
            score = score,
            streak = todayStreak.toInt(),
            completed = todayCompleted.toInt(),
        )
    habitViewModel.updateStats(statsUpdate)

    return HabitTodayStats(
        completed = todayCompleted,
        points = streakPoints,
        streak = todayStreak,
    )
}

fun buildCongratsSnackMessage(
    ctx: Context,
    todayStats: HabitTodayStats,
    encouragement: Encouragement,
): String {
    val randomSuccessEmoji = SUCCESS_EMOJIS.random()
    val congratsLine = randomSuccessEmoji + " " + encouragement.content
    var messages = mutableListOf<String>(congratsLine)
    if (todayStats.streak > 0) {
        messages.add(
            ctx.getString(
                R.string.youre_on_a_x_day_streak,
                todayStats.streak.toString(),
                todayStats.points.toString(),
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
