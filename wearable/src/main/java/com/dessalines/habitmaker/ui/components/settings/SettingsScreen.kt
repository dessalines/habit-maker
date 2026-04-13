package com.dessalines.habitmaker.ui.components.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.asLiveData
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material.SelectableChip
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.SettingsUpdateWearable
import com.dessalines.habitmaker.db.utils.HabitSort
import com.dessalines.habitmaker.db.utils.HabitSortOrder
import com.dessalines.habitmaker.db.utils.toBool
import com.dessalines.habitmaker.db.utils.toInt
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.ui.components.common.ListHeaderHabits

@Composable
fun SettingsScreen(appSettingsViewModel: AppSettingsViewModel) {
    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()

    var sortState = HabitSort.entries[settings?.sort ?: 0]
    var sortOrderState = HabitSortOrder.entries[settings?.sortOrder ?: 0]
    var hideCompletedState = (settings?.hideCompleted ?: 0).toBool()
    var hideArchivedState = (settings?.hideArchived ?: 0).toBool()
    var hidePointsOnHomeState = (settings?.hidePointsOnHome ?: 0).toBool()
    var hideScoreOnHomeState = (settings?.hideScoreOnHome ?: 0).toBool()
    var hideStreakOnHomeState = (settings?.hideStreakOnHome ?: 0).toBool()

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    fun updateSettings() =
        appSettingsViewModel.updateSettingsWearable(
            SettingsUpdateWearable(
                id = 1,
                sort = sortState.ordinal,
                sortOrder = sortOrderState.ordinal,
                hideCompleted = hideCompletedState.toInt(),
                hideArchived = hideArchivedState.toInt(),
                hidePointsOnHome = hidePointsOnHomeState.toInt(),
                hideScoreOnHome = hideScoreOnHomeState.toInt(),
                hideStreakOnHome = hideStreakOnHomeState.toInt(),
            ),
        )

    ScreenScaffold(
        scrollState = listState,
    ) { contentPadding ->
        // ScreenScaffold provides default padding; adjust as needed
        TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
            item {
                ListHeaderHabits(
                    stringResource(R.string.settings),
                    transformationSpec,
                )
            }
            item {
                ToggleChip(
                    label = {
                        Text(
                            stringResource(R.string.hide_completed),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideCompletedState,
                    toggleControl = { Switch(checked = hideCompletedState, enabled = true) },
                    onCheckedChange = {
                        hideCompletedState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ToggleChip(
                    label = {
                        Text(
                            stringResource(R.string.hide_archived),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideArchivedState,
                    toggleControl = { Switch(checked = hideArchivedState, enabled = true) },
                    onCheckedChange = {
                        hideArchivedState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ToggleChip(
                    label = {
                        Text(
                            stringResource(R.string.hide_points),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hidePointsOnHomeState,
                    toggleControl = { Switch(checked = hidePointsOnHomeState, enabled = true) },
                    onCheckedChange = {
                        hidePointsOnHomeState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ToggleChip(
                    label = {
                        Text(
                            stringResource(R.string.hide_score),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideScoreOnHomeState,
                    toggleControl = { Switch(checked = hideScoreOnHomeState, enabled = true) },
                    onCheckedChange = {
                        hideScoreOnHomeState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ToggleChip(
                    label = {
                        Text(
                            stringResource(R.string.hide_streak),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideStreakOnHomeState,
                    toggleControl = { Switch(checked = hideStreakOnHomeState, enabled = true) },
                    onCheckedChange = {
                        hideStreakOnHomeState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ListHeaderHabits(
                    title = stringResource(R.string.sort),
                    transformationSpec = transformationSpec,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortState == HabitSort.Streak,
                    onClick = {
                        sortState = HabitSort.Streak
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.streak), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortState == HabitSort.Points,
                    onClick = {
                        sortState = HabitSort.Points
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.points), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortState == HabitSort.Score,
                    onClick = {
                        sortState = HabitSort.Score
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.score), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortState == HabitSort.Status,
                    onClick = {
                        sortState = HabitSort.Status
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.status), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortState == HabitSort.DateCreated,
                    onClick = {
                        sortState = HabitSort.DateCreated
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.date_created), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortState == HabitSort.Name,
                    onClick = {
                        sortState = HabitSort.Name
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.name), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
            item {
                ListHeaderHabits(
                    title = stringResource(R.string.sort_order),
                    transformationSpec = transformationSpec,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortOrderState == HabitSortOrder.Ascending,
                    onClick = {
                        sortOrderState = HabitSortOrder.Ascending
                        updateSettings()
                    },
                    label = {
                        Text(
                            stringResource(R.string.ascending),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    enabled = true,
                )
            }
            item {
                SelectableChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = sortOrderState == HabitSortOrder.Descending,
                    onClick = {
                        sortOrderState = HabitSortOrder.Descending
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.descending), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                )
            }
        }
    }
}
