package com.dessalines.habitmaker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.SampleApplication
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.utils.isCompletedToday
import com.dessalines.habitmaker.db.utils.toEpochMillis
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.checkHabitForDay
import com.dessalines.habitmaker.db.viewmodels.updateStatsForHabit
import com.dessalines.habitmaker.presentation.theme.EXTRA_SMALL_PADDING
import com.dessalines.habitmaker.presentation.theme.SMALL_PADDING
import com.dessalines.habitmaker.presentation.theme.Theme
import com.dessalines.prettyFormat
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import java.time.DayOfWeek
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }

    private val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory((application as SampleApplication).appSettingsRepository)
    }

    private val habitViewModel: HabitViewModel by viewModels {
        HabitViewModelFactory((application as SampleApplication).habitRepository)
    }

    private val habitCheckViewModel: HabitCheckViewModel by viewModels {
        HabitCheckViewModelFactory((application as SampleApplication).habitCheckRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp(
                habitViewModel = habitViewModel,
                habitCheckViewModel = habitCheckViewModel,
                appSettingsViewModel = appSettingsViewModel,
                dataClient = dataClient,
            )
        }
    }
}

@Composable
fun WearApp(
    habitViewModel: HabitViewModel,
    habitCheckViewModel: HabitCheckViewModel,
    appSettingsViewModel: AppSettingsViewModel,
    dataClient: DataClient,
) {
    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()
    val habits by habitViewModel.getAll.asLiveData().observeAsState()

    val completedCount = settings?.completedCount ?: 0
    val firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY

    Theme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text(stringResource(R.string.settings))
                    }
                },
            ) { contentPadding ->
                // ScreenScaffold provides default padding; adjust as needed
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                                Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = stringResource(R.string.habits, "title"))
                        }
                    }
                    habits?.sortedBy { it.streak }?.sortedBy { it.score }?.forEach { habit ->
                        item(key = habit.id) {
                            HabitRow(
                                habit = habit,
                                onCheck = {
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
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitRow(
    habit: Habit,
    onCheck: () -> Unit,
) {
    val checked = isCompletedToday(habit.lastCompletedTime)

    ToggleChip(
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
                horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            ) {
                val style = MaterialTheme.typography.labelMedium
                val tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                // Streak
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
                Text(
                    "•",
                    style = style,
                    color = tint,
                )

                // Points
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
                Text(
                    "•",
                    style = style,
                    color = tint,
                )

                // Score
                Text(
                    "${habit.score}%",
                    style = style,
                    color = tint,
                )
            }
        },
        checked = checked,
        toggleControl = { Checkbox(checked = checked, enabled = true) },
        onCheckedChange = { onCheck() },
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

// @WearPreviewDevices
// @WearPreviewFontScales
// @Composable
// fun DefaultPreview() {
//    WearApp("Preview Android", 0)
// }
