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
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
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
                SwitchButton(
                    label = {
                        Text(
                            stringResource(R.string.hide_completed),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideCompletedState,
                    onCheckedChange = {
                        hideCompletedState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                SwitchButton(
                    label = {
                        Text(
                            stringResource(R.string.hide_archived),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideArchivedState,
                    onCheckedChange = {
                        hideArchivedState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                SwitchButton(
                    label = {
                        Text(
                            stringResource(R.string.hide_points),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hidePointsOnHomeState,
                    onCheckedChange = {
                        hidePointsOnHomeState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                SwitchButton(
                    label = {
                        Text(
                            stringResource(R.string.hide_score),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideScoreOnHomeState,
                    onCheckedChange = {
                        hideScoreOnHomeState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                SwitchButton(
                    label = {
                        Text(
                            stringResource(R.string.hide_streak),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    checked = hideStreakOnHomeState,
                    onCheckedChange = {
                        hideStreakOnHomeState = it
                        updateSettings()
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                ListHeaderHabits(
                    title = stringResource(R.string.sort),
                    transformationSpec = transformationSpec,
                )
            }
            item {
                RadioButton(
                    selected = sortState == HabitSort.Streak,
                    onSelect = {
                        sortState = HabitSort.Streak
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.streak), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                RadioButton(
                    selected = sortState == HabitSort.Points,
                    onSelect = {
                        sortState = HabitSort.Points
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.points), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                RadioButton(
                    selected = sortState == HabitSort.Score,
                    onSelect = {
                        sortState = HabitSort.Score
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.score), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                RadioButton(
                    selected = sortState == HabitSort.Status,
                    onSelect = {
                        sortState = HabitSort.Status
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.status), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                RadioButton(
                    selected = sortState == HabitSort.DateCreated,
                    onSelect = {
                        sortState = HabitSort.DateCreated
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.date_created), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                RadioButton(
                    selected = sortState == HabitSort.Name,
                    onSelect = {
                        sortState = HabitSort.Name
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.name), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                ListHeaderHabits(
                    title = stringResource(R.string.sort_order),
                    transformationSpec = transformationSpec,
                )
            }
            item {
                RadioButton(
                    selected = sortOrderState == HabitSortOrder.Ascending,
                    onSelect = {
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item {
                RadioButton(
                    selected = sortOrderState == HabitSortOrder.Descending,
                    onSelect = {
                        sortOrderState = HabitSortOrder.Descending
                        updateSettings()
                    },
                    label = {
                        Text(stringResource(R.string.descending), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    },
                    enabled = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
        }
    }
}
