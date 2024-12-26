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
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.EncouragementViewModel
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.HabitInsert
import com.dessalines.habitmaker.db.HabitViewModel
import com.dessalines.habitmaker.ui.components.common.SimpleTopAppBar
import com.dessalines.habitmaker.ui.components.common.ToolTip
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.nameIsValid
import com.dessalines.habitmaker.utils.timesPerFrequencyIsValid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateHabitScreen(
    navController: NavController,
    habitViewModel: HabitViewModel,
    encouragementViewModel: EncouragementViewModel,
) {
    val scrollState = rememberScrollState()
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val ctx = LocalContext.current

    var habit: Habit? = null

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.create_habit),
                onBackClick = { navController.navigateUp() },
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
                        habit?.let {
                            if (habitFormValid(it)) {
                                val insert =
                                    HabitInsert(
                                        name = it.name,
                                        frequency = it.frequency,
                                        timesPerFrequency = it.timesPerFrequency,
                                        notes = it.notes,
                                    )
                                val insertedId = habitViewModel.insert(insert)

                                // The id is -1 if its a failed insert
                                if (insertedId != -1L) {
                                    navController.navigate("habits?id=$insertedId")
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

fun habitFormValid(habit: Habit): Boolean =
    nameIsValid(habit.name) &&
        timesPerFrequencyIsValid(
            habit.timesPerFrequency,
            HabitFrequency.entries[habit.frequency],
        )
