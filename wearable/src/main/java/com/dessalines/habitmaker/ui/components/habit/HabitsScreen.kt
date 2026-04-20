package com.dessalines.habitmaker.ui.components.habit

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsertWearable
import com.dessalines.habitmaker.db.utils.HabitGroupData
import com.dessalines.habitmaker.db.utils.buildHabitsByFrequency
import com.dessalines.habitmaker.db.utils.isCompletedToday
import com.dessalines.habitmaker.db.utils.toBool
import com.dessalines.habitmaker.db.utils.toEpochMillis
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.db.viewmodels.checkHabitForDay
import com.dessalines.habitmaker.db.viewmodels.updateStatsForHabit
import com.dessalines.habitmaker.ui.components.common.ListHeaderHabits
import com.dessalines.habitmaker.ui.components.common.toResId
import com.dessalines.habitmaker.ui.theme.EXTRA_SMALL_PADDING
import com.dessalines.habitmaker.ui.theme.MEDIUM_PADDING
import com.dessalines.habitmaker.ui.theme.SMALL_PADDING
import com.dessalines.habitmaker.utils.updateComplication
import com.dessalines.prettyFormat
import com.google.android.gms.wearable.DataClient
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun HabitsScreen(
    habitViewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    dataClient: DataClient,
    navController: NavController,
) {
    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()
    val habits by habitViewModel.getAll.asLiveData().observeAsState()

    // Group them by frequency
    val habitsByFrequency = buildHabitsByFrequency(habits.orEmpty(), settings)

    val completedCount = settings?.completedCount ?: 0
    val firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY

    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    val ctx = LocalContext.current

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = {
                    navController.navigate("settings")
                },
            ) {
                Text(stringResource(R.string.settings))
            }
        },
    ) { contentPadding ->
        // ScreenScaffold provides default padding; adjust as needed
        TransformingLazyColumn(
            contentPadding = contentPadding,
            state = listState,
        ) {
            // If it's loading, show a progress indicator
            if (habits == null) {
                item {
                    CircularProgressIndicator()
                }
            }
            // Only show the empties if they're loaded from the DB
            habits?.let { habits ->
                if (habits.isEmpty()) {
                    item {
                        ListHeaderHabits(
                            stringResource(R.string.no_habits),
                            transformationSpec,
                        )
                    }
                }
            }

            habitsByFrequency.forEach {
                habitFrequencySection(
                    data = it,
                    settings = settings,
                    transformationSpec = transformationSpec,
                    onCheck = { habit ->
                        val checkTime = LocalDate.now().toEpochMillis()
                        checkHabitForDay(
                            habit.id,
                            checkTime,
                            habitCheckViewModel,
                            dataClient,
                        )
                        val checks = habitCheckViewModel.listForHabitSync(habit.id)
                        updateStatsForHabit(
                            habit,
                            habitViewModel,
                            dataClient,
                            checks,
                            completedCount,
                            firstDayOfWeek,
                        )

                        updateComplication(ctx)
                    },
                )
            }
            item {
                CreateHabitButton(
                    transformationSpec = transformationSpec,
                    onCreate = { name ->
                        val insert =
                            HabitInsertWearable(
                                name = name,
                            )
                        habitViewModel.insertWearable(insert, dataClient)

                        updateComplication(ctx)
                    },
                )
            }
        }
    }
}

fun TransformingLazyColumnScope.habitFrequencySection(
    data: HabitGroupData,
    settings: AppSettings?,
    transformationSpec: TransformationSpec,
    onCheck: (Habit) -> Unit,
) {
    if (data.filteredList.isNotEmpty()) {
        item {
            ListHeaderHabits(
                stringResource(data.frequency.toResId()),
                transformationSpec,
            )
        }
    }

    data.filteredList.forEach { habit ->
        item(key = habit.id) {
            HabitRow(
                habit = habit,
                settings = settings,
                transformationSpec = transformationSpec,
                onCheck = {
                    onCheck(habit)
                },
            )
        }
    }
}

@Composable
fun TransformingLazyColumnItemScope.HabitRow(
    habit: Habit,
    settings: AppSettings?,
    transformationSpec: TransformationSpec,
    onCheck: () -> Unit,
) {
    val checked = isCompletedToday(habit.lastCompletedTime)

    CheckboxButton(
        label = {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = SMALL_PADDING),
            )
        },
        secondaryLabel = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
            ) {
                val style = MaterialTheme.typography.labelMedium
                val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                // Streak
                if (!(settings?.hideStreakOnHome ?: 0).toBool()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(EXTRA_SMALL_PADDING),
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ShowChart,
                            contentDescription = null,
                            modifier = Modifier.size(style.fontSize.value.dp),
                            tint = tint,
                        )
                        Text(
                            prettyFormat(habit.streak),
                            style = style,
                            color = tint,
                        )
                    }
                }

                // Points
                if (!(settings?.hidePointsOnHome ?: 0).toBool()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(EXTRA_SMALL_PADDING),
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(style.fontSize.value.dp),
                            tint = tint,
                        )
                        Text(
                            prettyFormat(habit.points),
                            style = style,
                            color = tint,
                        )
                    }
                }

                // Score
                if (!(settings?.hideScoreOnHome ?: 0).toBool()) {
                    Text(
                        "${habit.score}%",
                        style = style,
                        color = tint,
                    )
                }
            }
        },
        checked = checked,
        onCheckedChange = { onCheck() },
        enabled = true,
        modifier =
            Modifier
                .fillMaxWidth()
                .transformedHeight(this, transformationSpec)
                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                .animateItem(),
        transformation = SurfaceTransformation(transformationSpec),
    )
}

@Composable
fun TransformingLazyColumnItemScope.CreateHabitButton(
    onCreate: (value: String) -> Unit,
    transformationSpec: TransformationSpec,
) {
    val placeholder = stringResource(R.string.create_habit)

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.let { data ->
                val results: Bundle = RemoteInput.getResultsFromIntent(data)
                val newValue: CharSequence? = results.getCharSequence(placeholder)
                onCreate(newValue as String)
            }
        }
    Button(
        content = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = placeholder,
            )
        },
        onClick = {
            val intent: Intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs: List<RemoteInput> =
                listOf(
                    RemoteInput
                        .Builder(placeholder)
                        .setLabel(placeholder)
                        .wearableExtender {
                            setEmojisAllowed(false)
                            setInputActionType(EditorInfo.IME_ACTION_DONE)
                        }.build(),
                )

            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)

            launcher.launch(intent)
        },
        modifier =
            Modifier
                .transformedHeight(this, transformationSpec)
                .padding(top = MEDIUM_PADDING)
                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
        transformation = SurfaceTransformation(transformationSpec),
    )
}
