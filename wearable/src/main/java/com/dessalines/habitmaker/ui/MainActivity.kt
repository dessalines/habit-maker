package com.dessalines.habitmaker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.dessalines.habitmaker.SampleApplication
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModelFactory
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModelFactory
import com.dessalines.habitmaker.ui.components.habit.HabitsScreen
import com.dessalines.habitmaker.ui.components.settings.SettingsScreen
import com.dessalines.habitmaker.ui.theme.Theme
import com.google.android.gms.wearable.Wearable

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
            Theme {
                AppScaffold {
                    val navController = rememberSwipeDismissableNavController()

                    SwipeDismissableNavHost(
                        navController = navController,
                        startDestination = "habits",
                    ) {
                        composable("habits") {
                            HabitsScreen(
                                habitViewModel = habitViewModel,
                                habitCheckViewModel = habitCheckViewModel,
                                appSettingsViewModel = appSettingsViewModel,
                                dataClient = dataClient,
                                navController = navController,
                            )
                        }
                        composable("settings") {
                            SettingsScreen(appSettingsViewModel = appSettingsViewModel)
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
