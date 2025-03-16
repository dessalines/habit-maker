package com.dessalines.habitmaker.ui.components.habit

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.HabitReminder
import com.dessalines.habitmaker.ui.components.common.ReminderTimePickerDialog
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING
import com.dessalines.habitmaker.ui.components.common.textFieldBorder
import com.dessalines.habitmaker.utils.HabitReminderFrequency
import com.dessalines.habitmaker.utils.toLocalTime
import com.kizitonwose.calendar.core.daysOfWeek
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.MultiSelectListPreference
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitRemindersForm(
    initialReminders: List<HabitReminder>,
    onChange: (List<HabitReminder>) -> Unit,
) {
    val ctx = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    ctx,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            },
        )
    }

    val permissionRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { result ->
            hasNotificationPermission = result
        }

    val locale = Locale.getDefault()
    val habitId = initialReminders.firstOrNull()?.habitId ?: 0

    var reminders by rememberSaveable {
        mutableStateOf(initialReminders)
    }

    var days by rememberSaveable {
        mutableStateOf(reminders.map { it.day })
    }

    // Necessary to match the enum ordinals
    val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

    var frequency by rememberSaveable {
        val freq =
            if (days.isEmpty()) {
                HabitReminderFrequency.NoReminders
            } else if (daysOfWeek == days) {
                HabitReminderFrequency.EveryDay
            } else {
                HabitReminderFrequency.SpecificDays
            }
        mutableStateOf(freq)
    }

    // Reminders must all have the same time.
    val currentTime = Calendar.getInstance()

    val initialTime = initialReminders.firstOrNull()?.time
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialTime?.hour ?: currentTime.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialTime?.minute ?: currentTime.get(Calendar.MINUTE),
        )

    fun habitRemindersChange() =
        onChange(
            when (frequency) {
                HabitReminderFrequency.NoReminders -> emptyList<HabitReminder>()
                HabitReminderFrequency.EveryDay -> {
                    daysToReminders(daysOfWeek, habitId, timePickerState)
                }
                HabitReminderFrequency.SpecificDays -> {
                    daysToReminders(days, habitId, timePickerState)
                }
            },
        )

    var showTimePicker by remember { mutableStateOf(false) }
    if (showTimePicker) {
        ReminderTimePickerDialog(
            state = timePickerState,
            onCancel = { showTimePicker = false },
            onConfirm = {
                habitRemindersChange()
                showTimePicker = false
            },
        )
    }

    Column(
        modifier = Modifier.padding(horizontal = SMALL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        ProvidePreferenceTheme {
            ListPreference(
                modifier = Modifier.textFieldBorder(),
                type = ListPreferenceType.DROPDOWN_MENU,
                value = frequency,
                onValueChange = {
                    frequency = it
                    habitRemindersChange()
                },
                values = HabitReminderFrequency.entries,
                valueToText = {
                    AnnotatedString(ctx.getString(it.resId))
                },
                title = {
                    Text(stringResource(frequency.resId))
                },
            )

            // Only show time when frequency is not None
            AnimatedVisibility(
                frequency != HabitReminderFrequency.NoReminders,
            ) {
                Column {
                    if (!hasNotificationPermission) {
                        // Permission request
                        Text(stringResource(R.string.notification_permission_required))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                        ) {
                            Text(stringResource(R.string.request_notification_permission))
                        }
                    }

                    OutlinedTextField(
                        label = { Text(stringResource(R.string.time)) },
                        readOnly = true,
                        enabled = false,
                        value = timePickerState.toLocalTime().toString(),
                        onValueChange = {},
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { showTimePicker = true }),
                        // Override the enabled false colors
                        colors =
                            OutlinedTextFieldDefaults.colors().copy(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }
            // Only show days when its specific days
            AnimatedVisibility(
                frequency == HabitReminderFrequency.SpecificDays,
            ) {
                MultiSelectListPreference(
                    modifier = Modifier.textFieldBorder(),
                    title = { Text(stringResource(R.string.days)) },
                    summary = {
                        val daysStr = days.joinToString { it.toLocaleStr(locale) }
                        Text(daysStr)
                    },
                    value = days.toSet(),
                    values = daysOfWeek,
                    valueToText = {
                        AnnotatedString(it.toLocaleStr(locale))
                    },
                    onValueChange = {
                        days = it.toList()
                        habitRemindersChange()
                    },
                )
            }
        }
    }
}

@Composable
@Preview
fun HabitRemindersFormPreview() {
    HabitRemindersForm(
        initialReminders = emptyList(),
        onChange = {},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
fun daysToReminders(
    days: List<DayOfWeek>,
    habitId: Int,
    timePickerState: TimePickerState,
) = days.map {
    HabitReminder(
        id = 0,
        habitId = habitId,
        time = timePickerState.toLocalTime(),
        day = it,
    )
}

fun DayOfWeek.toLocaleStr(locale: Locale): String = this.getDisplayName(TextStyle.SHORT, locale)
