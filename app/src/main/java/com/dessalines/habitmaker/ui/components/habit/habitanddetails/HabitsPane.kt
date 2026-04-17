package com.dessalines.habitmaker.ui.components.habit.habitanddetails

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
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
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.db.utils.HabitGroupData
import com.dessalines.habitmaker.db.utils.buildHabitsByFrequency
import com.dessalines.habitmaker.db.utils.isCompletedToday
import com.dessalines.habitmaker.db.utils.toBool
import com.dessalines.habitmaker.ui.components.common.HabitChipsFlowRow
import com.dessalines.habitmaker.ui.components.common.HabitDaysCompletedInfoChip
import com.dessalines.habitmaker.ui.components.common.HabitPointsInfoChip
import com.dessalines.habitmaker.ui.components.common.LARGE_PADDING
import com.dessalines.habitmaker.ui.components.common.MEDIUM_PADDING
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING
import com.dessalines.habitmaker.ui.components.common.SectionProgress
import com.dessalines.habitmaker.ui.components.common.SectionTitle
import com.dessalines.habitmaker.ui.components.common.TasksDaysOrToday
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.ui.components.common.toResId
import com.dessalines.habitmaker.utils.SelectionVisibilityState
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
    val tooltipPosition = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    val title = stringResource(R.string.habits)

    // Group them by frequency
    val habitsByFrequency = buildHabitsByFrequency(habits.orEmpty(), settings)

    val hideCompleted = (settings?.hideCompleted ?: 0).toBool()
    val hideTotals = (settings?.hideTotals ?: 0).toBool()

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
                if (!hideTotals && habits.orEmpty().isNotEmpty()) {
                    item {
                        HabitTotals(habits, settings)
                    }
                }
                habitsByFrequency.forEach {
                    habitFrequencySection(
                        data = it,
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
                                modifier = Modifier.padding(horizontal = LARGE_PADDING),
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
                    modifier = Modifier.imePadding(),
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
    data: HabitGroupData,
    settings: AppSettings?,
    selectionState: SelectionVisibilityState<Int>,
    onHabitClick: (Int) -> Unit,
    onHabitCheck: (Int) -> Unit,
) {
    if (data.filteredList.isNotEmpty()) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LARGE_PADDING),
            ) {
                SectionTitle(stringResource(data.frequency.toResId()))
                if (data.completed > 0) {
                    SectionProgress(data.completed, data.total)
                }
            }
        }
        itemsIndexed(
            items = data.filteredList,
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

                // Don't show horizontal divider for last one
                if (index.plus(1) != data.filteredList.size) {
                    HorizontalDivider()
                }
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = MEDIUM_PADDING))
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
            Pair(Icons.Outlined.CheckBox, MaterialTheme.colorScheme.primary)
        } else {
            Pair(Icons.Outlined.CheckBoxOutlineBlank, MaterialTheme.colorScheme.outline)
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

@Composable
fun HabitTotals(
    habits: List<Habit>?,
    settings: AppSettings?,
) {
    Column(
        Modifier
            .padding(horizontal = LARGE_PADDING),
    ) {
        SectionTitle(stringResource(R.string.totals))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        ) {
            // Show the # completed today, with a status color
            HabitDaysCompletedInfoChip(
                completed = habits.orEmpty().count { isCompletedToday(it.lastCompletedTime) },
                taskType = TasksDaysOrToday.Today,
                showHabitStatus = true,
                settings = settings,
            )

            // Streak and score doesn't make sense for totals, but the others do.
            // Show total completed tasks
            HabitDaysCompletedInfoChip(
                completed = habits.orEmpty().sumOf { it.completed },
                taskType = TasksDaysOrToday.Tasks,
                settings = settings,
            )

            // Show the total points
            HabitPointsInfoChip(
                points = habits.orEmpty().sumOf { it.points },
                settings = settings,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = MEDIUM_PADDING))
    }
}
