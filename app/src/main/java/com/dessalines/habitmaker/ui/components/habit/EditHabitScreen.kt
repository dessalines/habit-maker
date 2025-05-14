package com.dessalines.habitmaker.ui.components.habit

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.EncouragementInsert
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.HabitReminderInsert
import com.dessalines.habitmaker.db.HabitReminderViewModel
import com.dessalines.habitmaker.db.HabitUpdate
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.notifications.scheduleRemindersForHabit
import com.dessalines.habitmaker.ui.components.common.BackButton
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.utils.isCompletedToday

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditHabitScreen(
    navController: NavController,
    habitViewModel: HabitViewModel,
    encouragementViewModel: EncouragementViewModel,
    reminderViewModel: HabitReminderViewModel,
    id: Int,
) {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()

    val habit = habitViewModel.getByIdSync(id)
    val encouragements = encouragementViewModel.listForHabitSync(id)
    val reminders = reminderViewModel.listForHabitSync(id)

    // Copy the habit and encouragements from the DB first
    var editedHabit by remember {
        mutableStateOf(habit)
    }

    var editedEncouragements by remember {
        mutableStateOf(encouragements)
    }

    var editedReminders by remember {
        mutableStateOf(reminders)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_habit)) },
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
                    habit = editedHabit,
                    onChange = { editedHabit = it },
                )
                HabitRemindersForm(
                    initialReminders = editedReminders,
                    onChange = { editedReminders = it },
                )
                EncouragementsForm(
                    initialEncouragements = editedEncouragements,
                    onChange = { editedEncouragements = it },
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
                        editedHabit?.let { editedHabit ->
                            if (habitFormValid(editedHabit)) {
                                val update =
                                    HabitUpdate(
                                        id = editedHabit.id,
                                        name = editedHabit.name,
                                        frequency = editedHabit.frequency,
                                        timesPerFrequency = editedHabit.timesPerFrequency,
                                        notes = editedHabit.notes,
                                        context = editedHabit.context,
                                        archived = editedHabit.archived,
                                    )
                                habitViewModel.update(update)

                                // Delete then add all the reminders
                                reminderViewModel.delete(editedHabit.id)
                                editedReminders.forEach {
                                    val insert =
                                        HabitReminderInsert(
                                            habitId = editedHabit.id,
                                            time = it.time,
                                            day = it.day,
                                        )
                                    reminderViewModel.insert(insert)
                                }

                                // Reschedule the reminders for that habit
                                val isCompleted = isCompletedToday(editedHabit.lastCompletedTime)
                                scheduleRemindersForHabit(
                                    ctx,
                                    editedReminders,
                                    editedHabit.name,
                                    editedHabit.id,
                                    isCompleted,
                                )

                                // Delete then add all the encouragements
                                encouragementViewModel.deleteForHabit(editedHabit.id)
                                editedEncouragements.forEach {
                                    val insert =
                                        EncouragementInsert(
                                            habitId = editedHabit.id,
                                            content = it.content,
                                        )
                                    encouragementViewModel.insert(insert)
                                }
                                navController.navigateUp()
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
