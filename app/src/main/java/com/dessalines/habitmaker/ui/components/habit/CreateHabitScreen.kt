package com.dessalines.habitmaker.ui.components.habit

import android.widget.Toast
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettingsViewModel
import com.dessalines.habitmaker.db.Encouragement
import com.dessalines.habitmaker.db.EncouragementInsert
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitReminder
import com.dessalines.habitmaker.db.HabitReminderInsert
import com.dessalines.habitmaker.db.HabitReminderViewModel
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.notifications.scheduleRemindersForHabit
import com.dessalines.habitmaker.ui.components.common.BackButton
import com.dessalines.habitmaker.ui.components.common.ToolTip
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateHabitScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel,
    habitViewModel: HabitViewModel,
    encouragementViewModel: EncouragementViewModel,
    reminderViewModel: HabitReminderViewModel,
) {
    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()
    val firstDayOfWeek = settings?.firstDayOfWeek ?: DayOfWeek.SUNDAY

    val scrollState = rememberScrollState()
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val ctx = LocalContext.current

    var habit: Habit? = null
    var encouragements: List<Encouragement> = listOf()
    var reminders: List<HabitReminder> = listOf()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_habit)) },
                navigationIcon = {
                    BackButton(
                        onBackClick = { navController.navigateUp() },
                    )
                },
            )
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .imePadding(),
            ) {
                HabitForm(
                    onChange = { habit = it },
                )
                HabitRemindersForm(
                    initialReminders = reminders,
                    firstDayOfWeek = firstDayOfWeek,
                    onChange = { reminders = it },
                )
                EncouragementsForm(
                    initialEncouragements = encouragements,
                    onChange = { encouragements = it },
                )
            }
        },
        floatingActionButton = {
            BasicTooltipBox(
                positionProvider = tooltipPosition,
                state = rememberBasicTooltipState(isPersistent = false),
                tooltip = {
                    ToolTip(stringResource(R.string.save))
                },
            ) {
                FloatingActionButton(
                    modifier = Modifier.imePadding(),
                    onClick = {
                        habit?.let { habit ->
                            if (habitFormValid(habit)) {
                                val insert =
                                    HabitInsert(
                                        name = habit.name,
                                        frequency = habit.frequency,
                                        timesPerFrequency = habit.timesPerFrequency,
                                        notes = habit.notes,
                                        context = habit.context,
                                        archived = habit.archived,
                                    )
                                val insertedHabitId = habitViewModel.insert(insert)

                                // The id is -1 if its a failed insert
                                if (insertedHabitId != -1L) {
                                    // Insert the reminders
                                    reminders.forEach {
                                        val insert =
                                            HabitReminderInsert(
                                                habitId = insertedHabitId.toInt(),
                                                time = it.time,
                                                day = it.day,
                                            )
                                        reminderViewModel.insert(insert)
                                    }
                                    // Reschedule the reminders for that habit
                                    scheduleRemindersForHabit(
                                        ctx,
                                        reminders,
                                        habit.name,
                                        insertedHabitId.toInt(),
                                        false,
                                    )

                                    // Insert the encouragements
                                    encouragements.forEach {
                                        val insert =
                                            EncouragementInsert(
                                                habitId = insertedHabitId.toInt(),
                                                content = it.content,
                                            )
                                        encouragementViewModel.insert(insert)
                                    }

                                    navController.navigate("habits?id=$insertedHabitId") {
                                        popUpTo("habits")
                                    }
                                } else {
                                    Toast
                                        .makeText(
                                            ctx,
                                            ctx.getString(R.string.habit_already_exists),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        }
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = stringResource(R.string.save),
                    )
                }
            }
        },
    )
}
