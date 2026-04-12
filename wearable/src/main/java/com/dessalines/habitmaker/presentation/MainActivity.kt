package com.dessalines.habitmaker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.dessalines.habitmaker.MainViewModel
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.SampleApplication
import com.dessalines.habitmaker.db.utils.isCompletedToday
import com.dessalines.habitmaker.db.utils.toEpochMillis
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.EncouragementViewModel
import com.dessalines.habitmaker.db.viewmodels.EncouragementViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitReminderViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitReminderViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.checkHabitForDay
import com.dessalines.habitmaker.db.viewmodels.updateStatsForHabit
import com.dessalines.habitmaker.listenForOtherDeviceDbChanges
import com.dessalines.habitmaker.presentation.theme.Theme
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import java.time.DayOfWeek
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val mainViewModel by viewModels<MainViewModel>()

    private val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory((application as SampleApplication).appSettingsRepository)
    }

    private val habitViewModel: HabitViewModel by viewModels {
        HabitViewModelFactory((application as SampleApplication).habitRepository)
    }

    private val encouragementViewModel: EncouragementViewModel by viewModels {
        EncouragementViewModelFactory((application as SampleApplication).encouragementRepository)
    }

    private val habitCheckViewModel: HabitCheckViewModel by viewModels {
        HabitCheckViewModelFactory((application as SampleApplication).habitCheckRepository)
    }

    private val reminderViewModel: HabitReminderViewModel by viewModels {
        HabitReminderViewModelFactory((application as SampleApplication).habitReminderRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listenForOtherDeviceDbChanges(lifecycleScope, mainViewModel, habitViewModel, habitCheckViewModel)

        setContent {
            WearApp(
                mainViewModel = mainViewModel,
                habitViewModel = habitViewModel,
                habitCheckViewModel = habitCheckViewModel,
                appSettingsViewModel = appSettingsViewModel,
                dataClient = dataClient,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(mainViewModel)
//        messageClient.addListener(mainViewModel)
        capabilityClient.addListener(
            mainViewModel,
            "wear://".toUri(),
            CapabilityClient.FILTER_REACHABLE,
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(mainViewModel)
        capabilityClient.removeListener(mainViewModel)
    }
}

@Composable
fun WearApp(
    mainViewModel: MainViewModel,
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
                            Text(text = stringResource(R.string.hello_world, "title"))
                        }
                    }
                    habits?.forEach { habit ->
                        item {
                            Text("eventCount: ${mainViewModel.events.size}")
                        }
                        item(key = habit.id) {
                            ToggleChip(
                                label = { Text(habit.name, maxLines = 3, overflow = TextOverflow.Ellipsis) },
                                checked = isCompletedToday(habit.lastCompletedTime),
                                toggleControl = { Checkbox(checked = false, enabled = true) },
                                onCheckedChange = {
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
                                enabled = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                    }
                }
            }
        }
    }
}

// @WearPreviewDevices
// @WearPreviewFontScales
// @Composable
// fun DefaultPreview() {
//    WearApp("Preview Android", 0)
// }
