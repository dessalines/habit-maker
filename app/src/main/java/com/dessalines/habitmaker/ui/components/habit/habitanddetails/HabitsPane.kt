package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import androidx.annotation.StringRes
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TopAppBarScrollBehavior
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
import com.dessalines.habitmaker.ui.components.common.MEDIUM_PADDING
import com.dessalines.habitmaker.ui.components.common.SectionTitle
import com.dessalines.habitmaker.ui.components.common.TodayCompletedCount
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.HabitSort
import com.dessalines.habitmaker.utils.HabitSortOrder
import com.dessalines.habitmaker.utils.SelectionVisibilityState
import com.dessalines.habitmaker.utils.isCompletedToday
import com.dessalines.habitmaker.utils.isVirtualCompleted
import com.dessalines.habitmaker.utils.toBool
import okhttp3.internal.toImmutableList
import kotlin.collections.orEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HabitsPane(
    habits: List<Habit>?,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
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
    val title = stringResource(R.string.habits)

    // Calculate the completed today before filtering (since hide completed would filter these out)
    val todayCompletedCount = habits.orEmpty().count { isCompletedToday(it.lastCompletedTime) }

    // Filter and sort the habits
    val filteredHabits = filterAndSortHabits(habits.orEmpty(), settings)

    // Group them by frequency
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
            CenterAlignedTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
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
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                },
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
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                if (todayCompletedCount > 0) {
                    item {
                        TodayCompletedCount(todayCompletedCount)
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
        itemsIndexed(
            items = habits,
            key = { _, item -> item.id },
        ) { index, habit ->
            val selected =
                when (selectionState) {
                    is SelectionVisibilityState.ShowSelection -> selectionState.selectedItem == habit.id
                    else -> false
                }

            Column(Modifier.animateItem()) {
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
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(bottom = MEDIUM_PADDING))
        }
    }
}

@Composable
fun HabitRow(
    habit: Habit,
    modifier: Modifier = Modifier,
    settings: AppSettings?,
    selected: Boolean = false,
    onCheck: () -> Unit,
    onClick: () -> Unit,
) {
    val containerColor =
        if (!selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    val (icon, tint) =
        if (isCompletedToday(habit.lastCompletedTime)) {
            Pair(Icons.Outlined.Check, MaterialTheme.colorScheme.primary)
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
            modifier.clickable {
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
        tmp.removeAll { isVirtualCompleted(it.lastStreakTime) }
    }

    // Hide archived
    if ((settings?.hideArchived ?: 0).toBool()) {
        tmp.removeAll { it.archived.toBool() }
    }

    // Sorting
    val sortSetting = HabitSort.entries[settings?.sort ?: 0]
    when (sortSetting) {
        HabitSort.Name -> tmp.sortedBy { it.name }
        HabitSort.Points -> tmp.sortedBy { it.points }
        HabitSort.Score -> tmp.sortWith(compareBy({ it.score }, { it.points }))
        HabitSort.Streak -> tmp.sortWith(compareBy({ it.streak }, { it.points }))
        HabitSort.Status -> tmp.sortWith(compareBy({ isVirtualCompleted(it.lastStreakTime) }, { it.points }))
        HabitSort.DateCreated -> tmp.sortedBy { it.id }
    }
    val sortOrder = HabitSortOrder.entries[settings?.sortOrder ?: 0]
    if (sortOrder == HabitSortOrder.Descending) {
        tmp.reverse()
    }

    return tmp.toImmutableList()
}

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
