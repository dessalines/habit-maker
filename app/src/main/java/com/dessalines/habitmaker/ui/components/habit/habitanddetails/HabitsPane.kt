package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import androidx.annotation.StringRes
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.ui.components.common.HabitChipsFlowRow
import com.dessalines.habitmaker.ui.components.common.LARGE_PADDING
import com.dessalines.habitmaker.ui.components.common.SectionDivider
import com.dessalines.habitmaker.ui.components.common.SectionTitle
import com.dessalines.habitmaker.ui.components.common.SimpleTopAppBar
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.HabitSort
import com.dessalines.habitmaker.utils.HabitSortOrder
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.toBool
import okhttp3.internal.toImmutableList
import kotlin.collections.orEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitsPane(
    habits: List<Habit>?,
    settings: AppSettings?,
    snackbarHostState: SnackbarHostState,
    onHabitClick: (habitId: Int) -> Unit,
    onHabitCheck: (habitId: Int) -> Unit,
    onCreateHabitClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHideCompletedClick: (Boolean) -> Unit,
    selectionState: SelectionVisibilityState<Int>,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val title = stringResource(R.string.habits)

    val filteredHabits = filterAndSortHabits(habits.orEmpty(), settings)

    val habitsByFrequency = buildHabitsByFrequency(filteredHabits)

    val hideCompleted = (settings?.hideCompleted ?: 0).toBool()
    val (hideIcon, hideText) =
        if (hideCompleted) {
            Pair(Icons.Default.VisibilityOff, stringResource(R.string.hide_completed))
        } else {
            Pair(Icons.Default.Visibility, stringResource(R.string.show_completed))
        }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = title,
                scrollBehavior = scrollBehavior,
                actions = {
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(hideText)
                        },
                    ) {
                        IconButton(
                            onClick = {
                                onHideCompletedClick(!hideCompleted)
                            },
                        ) {
                            Icon(
                                hideIcon,
                                contentDescription = hideText,
                            )
                        }
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.settings))
                        },
                    ) {
                        IconButton(
                            onClick = onSettingsClick,
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.Companion.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { padding ->
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                habitsByFrequency.forEach {
                    habitFrequencySection(
                        it.titleResId,
                        it.list,
                        settings,
                        selectionState,
                        onHabitClick,
                        onHabitCheck,
                    )
                }
                // Only show the empties if they're loaded from the DB
                habits?.let { habits ->
                    if (habits.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_habits),
                                modifier = Modifier.Companion.padding(horizontal = LARGE_PADDING),
                            )
                        }
                    }
                    // If there are habits, but they're filtered, then say all completed
                    if (habits.isNotEmpty() && filteredHabits.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.all_completed_for_today),
                                modifier = Modifier.Companion.padding(horizontal = LARGE_PADDING),
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            BasicTooltipBox(
                positionProvider = tooltipPosition,
                state = rememberBasicTooltipState(isPersistent = false),
                tooltip = {
                    ToolTip(stringResource(R.string.create_habit))
                },
            ) {
                FloatingActionButton(
                    modifier = Modifier.Companion.imePadding(),
                    onClick = onCreateHabitClick,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.create_habit),
                    )
                }
            }
        },
    )
}

fun LazyListScope.habitFrequencySection(
    @StringRes sectionTitleResId: Int,
    habits: List<Habit>,
    settings: AppSettings?,
    selectionState: SelectionVisibilityState<Int>,
    onHabitClick: (Int) -> Unit,
    onHabitCheck: (Int) -> Unit,
) {
    if (habits.isNotEmpty()) {
        item {
            SectionTitle(stringResource(sectionTitleResId))
        }
        itemsIndexed(habits) { index, habit ->
            val selected =
                when (selectionState) {
                    is SelectionVisibilityState.ShowSelection -> selectionState.selectedItem == habit.id
                    else -> false
                }

            HabitRow(
                habit = habit,
                settings = settings,
                onClick = { onHabitClick(habit.id) },
                onCheck = {
                    onHabitCheck(habit.id)
                },
                selected = selected,
            )

            // Dont show horizontal divider for last one
            if (index.plus(1) != habits.size) {
                HorizontalDivider()
            }
        }
        item {
            SectionDivider()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitRow(
    habit: Habit,
    settings: AppSettings?,
    selected: Boolean = false,
    onCheck: () -> Unit,
    onClick: () -> Unit,
) {
    val containerColor =
        if (!selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    val (icon, tint) =
        if (habit.completed.toBool()) {
            Pair(Icons.Outlined.Check, MaterialTheme.colorScheme.onSurface)
        } else {
            Pair(Icons.Outlined.Close, MaterialTheme.colorScheme.outline)
        }

    ListItem(
        headlineContent = {
            Text(
                text = habit.name,
                color =
                    if (habit.archived.toBool()) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        Color.Unspecified
                    },
            )
        },
        supportingContent = {
            HabitChipsFlowRow(habit, settings)
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier =
            Modifier.Companion.clickable {
                onClick()
            },
        trailingContent = {
            IconButton(
                onClick = onCheck,
            ) {
                Icon(
                    imageVector = icon,
                    tint = tint,
                    contentDescription = null,
                )
            }
        },
    )
}

@Composable
@Preview
fun HabitRowPreview() {
    HabitRow(
        habit = sampleHabit,
        settings = null,
        onCheck = {},
        onClick = {},
    )
}

data class HabitListAndTitle(
    @StringRes val titleResId: Int,
    val list: List<Habit>,
)

fun filterAndSortHabits(
    habits: List<Habit>,
    settings: AppSettings?,
): List<Habit> {
    val tmp = habits.toMutableList()

    // Hide completed
    if ((settings?.hideCompleted ?: 0).toBool()) {
        tmp.removeAll { it.completed.toBool() }
    }

    // Hide archived
    if ((settings?.hideArchived ?: 0).toBool()) {
        tmp.removeAll { it.archived.toBool() }
    }

    // Sorting
    val sortSetting = HabitSort.entries[settings?.sort ?: 0]
    when (sortSetting) {
        HabitSort.Name -> tmp.sortBy { it.name }
        HabitSort.Points -> tmp.sortBy { it.points }
        HabitSort.Score -> tmp.sortBy { it.score }
        HabitSort.Streak -> tmp.sortBy { it.streak }
        HabitSort.Status -> tmp.sortBy { it.completed }
        HabitSort.DateCreated -> tmp.sortBy { it.id }
    }
    val sortOrder = HabitSortOrder.entries[settings?.sortOrder ?: 0]
    if (sortOrder == HabitSortOrder.Descending) {
        tmp.reverse()
    }

    return tmp.toImmutableList()
}

@Composable
fun buildHabitsByFrequency(habits: List<Habit>) =
    listOf(
        HabitListAndTitle(
            titleResId = R.string.daily,
            list = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Daily },
        ),
        HabitListAndTitle(
            titleResId = R.string.weekly,
            list = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Weekly },
        ),
        HabitListAndTitle(
            titleResId = R.string.monthly,
            list = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Monthly },
        ),
        HabitListAndTitle(
            titleResId = R.string.yearly,
            list = habits.filter { HabitFrequency.entries[it.frequency] == HabitFrequency.Yearly },
        ),
    )
