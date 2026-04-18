package com.dessalines.habitmaker.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dessalines.habitmaker.db.viewmodels.AppSettingsViewModel
import com.dessalines.habitmaker.db.viewmodels.EncouragementViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitCheckViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitReminderViewModel
import com.dessalines.habitmaker.db.viewmodels.HabitViewModel
import com.dessalines.habitmaker.ui.components.about.AboutScreen
import com.dessalines.habitmaker.ui.components.common.ShowChangelog
import com.dessalines.habitmaker.ui.components.habit.CreateHabitScreen
import com.dessalines.habitmaker.ui.components.habit.EditHabitScreen
import com.dessalines.habitmaker.ui.components.habit.habitanddetails.HabitsAndDetailScreen
import com.dessalines.habitmaker.ui.components.settings.BackupAndRestoreScreen
import com.dessalines.habitmaker.ui.components.settings.BehaviorScreen
import com.dessalines.habitmaker.ui.components.settings.LookAndFeelScreen
import com.dessalines.habitmaker.ui.components.settings.SettingsScreen

@Composable
fun Main(appSettingsViewModel: AppSettingsViewModel,
         habitViewModel: HabitViewModel,
         habitCheckViewModel: HabitCheckViewModel,
         encouragementViewModel: EncouragementViewModel,
         reminderViewModel: HabitReminderViewModel,
) {
    val startDestination = "habits"

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
                    reminderViewModel = reminderViewModel,
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
                    appSettingsViewModel = appSettingsViewModel,
                    habitViewModel = habitViewModel,
                    encouragementViewModel = encouragementViewModel,
                    reminderViewModel = reminderViewModel,
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
                    appSettingsViewModel = appSettingsViewModel,
                    habitViewModel = habitViewModel,
                    encouragementViewModel = encouragementViewModel,
                    reminderViewModel = reminderViewModel,
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

