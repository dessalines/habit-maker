package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import android.annotation.SuppressLint
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
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.HabitCheckInsert
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitUpdateStats
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.calculatePoints
import com.dessalines.habitmaker.utils.calculateScore
import com.dessalines.habitmaker.utils.calculateStreaks
import com.dessalines.habitmaker.utils.currentStreak
import com.dessalines.habitmaker.utils.epochMillisToLocalDate
import com.dessalines.habitmaker.utils.toEpochMillis
import com.dessalines.habitmaker.utils.toInt
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
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val completedCount = settings?.completedCount ?: 0

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
                                val success = checkHabitForDay(habitId, checkTime, habitCheckViewModel)
                                updateStatsForHabit(habitId, habitViewModel, habitCheckViewModel, completedCount)

                                // If successful, show a random encouragement
                                if (success) {
                                    encouragementViewModel.getRandomForHabit(habitId)?.let { encouragement ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(encouragement.content)
                                        }
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
                                        updateStatsForHabit(habit.id, habitViewModel, habitCheckViewModel, completedCount)
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
): Boolean {
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
        return false
    } else {
        return true
    }
}

fun updateStatsForHabit(
    habitId: Int,
    habitViewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    completedCount: Int,
) {
    // Read the history for that item
    val checks = habitCheckViewModel.listForHabitSync(habitId)
    val dateChecks = checks.map { it.checkTime.epochMillisToLocalDate() }
    val todayDate = LocalDate.now()

    val completed = dateChecks.lastOrNull() == todayDate

    val streaks = calculateStreaks(checks)
    val currentStreak = currentStreak(streaks, todayDate)
    val points = calculatePoints(streaks)
    val score = calculateScore(checks, completedCount)

    val statsUpdate =
        HabitUpdateStats(
            id = habitId,
            points = points.toInt(),
            score = score,
            streak = currentStreak.toInt(),
            completed = completed.toInt(),
        )
    habitViewModel.updateStats(statsUpdate)
}
