package com.dessalines.habitmaker

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.AppSettingsViewModel
import com.dessalines.habitmaker.db.AppSettingsViewModelFactory
import com.dessalines.habitmaker.db.EncouragementRepository
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.EncouragementViewModelFactory
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitCheckViewModel
import com.dessalines.habitmaker.db.HabitCheckViewModelFactory
import com.dessalines.habitmaker.db.HabitRepository
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.db.HabitViewModelFactory
import com.dessalines.habitmaker.ui.components.about.AboutScreen
import com.dessalines.habitmaker.ui.components.common.ShowChangelog
import com.dessalines.habitmaker.ui.components.habit.CreateHabitScreen
import com.dessalines.habitmaker.ui.components.habit.EditHabitScreen
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.HabitsAndDetailScreen
import com.dessalines.habitmaker.ui.components.settings.BackupAndRestoreScreen
import com.dessalines.habitmaker.ui.components.settings.BehaviorScreen
import com.dessalines.habitmaker.ui.components.settings.LookAndFeelScreen
import com.dessalines.habitmaker.ui.components.settings.SettingsScreen
import com.dessalines.habitmaker.ui.theme.HabitMakerTheme

class HabitMakerApplication : Application() {
    private val database by lazy { AppDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val encouragementRepository by lazy { EncouragementRepository(database.encouragementDao()) }
    val habitCheckRepository by lazy { HabitCheckRepository(database.habitCheckDao()) }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : AppCompatActivity() {
    private val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory((application as HabitMakerApplication).appSettingsRepository)
    }

    private val habitViewModel: HabitViewModel by viewModels {
        HabitViewModelFactory((application as HabitMakerApplication).habitRepository)
    }

    private val encouragementViewModel: EncouragementViewModel by viewModels {
        EncouragementViewModelFactory((application as HabitMakerApplication).encouragementRepository)
    }

    private val habitCheckViewModel: HabitCheckViewModel by viewModels {
        HabitCheckViewModelFactory((application as HabitMakerApplication).habitCheckRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by appSettingsViewModel.appSettings
                .asLiveData()
                .observeAsState()

            val startDestination = "habits"

            HabitMakerTheme(
                settings = settings,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    ShowChangelog(appSettingsViewModel = appSettingsViewModel)

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable(
                            route = "habits?id={id}",
                            arguments =
                                listOf(
                                    navArgument("id") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    },
                                ),
                        ) {
                            val id = it.arguments?.getString("id")?.toInt()

                            HabitsAndDetailScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                                habitViewModel = habitViewModel,
                                encouragementViewModel = encouragementViewModel,
                                habitCheckViewModel = habitCheckViewModel,
                                id = id,
                            )
                        }
                        composable(
                            route = "createHabit",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            CreateHabitScreen(
                                navController = navController,
                                habitViewModel = habitViewModel,
                                encouragementViewModel = encouragementViewModel,
                            )
                        }
                        composable(
                            route = "editHabit/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType }),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val id = it.arguments?.getInt("id") ?: 0
                            EditHabitScreen(
                                navController = navController,
                                habitViewModel = habitViewModel,
                                encouragementViewModel = encouragementViewModel,
                                id = id,
                            )
                        }

                        composable(
                            route = "settings",
                        ) {
                            SettingsScreen(
                                navController = navController,
                            )
                        }
                        composable(
                            route = "about",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            AboutScreen(
                                navController = navController,
                            )
                        }
                        composable(
                            route = "lookAndFeel",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            LookAndFeelScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                            )
                        }
                        composable(
                            route = "behavior",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            BehaviorScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                            )
                        }
                        composable(
                            route = "backupAndRestore",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            BackupAndRestoreScreen(
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun enterAnimation(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
    {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
        )
    }

private fun exitAnimation(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
    {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
        )
    }
