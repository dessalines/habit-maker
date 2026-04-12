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
import androidx.core.net.toUri
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
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
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.EncouragementRepository
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitReminderRepository
import com.dessalines.habitmaker.db.HabitRepository
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
import com.dessalines.habitmaker.listenForOtherDeviceDbChanges
import com.dessalines.habitmaker.presentation.theme.Theme
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

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

        listenForOtherDeviceDbChanges(lifecycleScope, mainViewModel, habitViewModel)

        setContent {
            WearApp(
                mainViewModel = mainViewModel,
                habitViewModel = habitViewModel,
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
) {
    val firstEventText = mainViewModel.events.firstOrNull()?.text ?: "John"
    val greetingName = firstEventText

    val eventsSize = mainViewModel.events.size

    val habits by habitViewModel.getAll.asLiveData().observeAsState()

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
                        Text("More")
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
                            Text(text = stringResource(R.string.hello_world, greetingName))
                        }
                    }
                    item {
                        Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                            Text(eventsSize.toString())
                        }
                    }
                    habits?.forEach { habit ->
                        item(key = habit.id) {
                            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                                Text(habit.name)
                            }
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
