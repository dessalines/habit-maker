package com.dessalines.habitmaker.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.sampleAppSettings
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.HabitStatus
import com.dessalines.habitmaker.utils.toBool
import com.dessalines.prettyFormat

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) = Text(
    text = title,
    style = MaterialTheme.typography.titleLarge,
    modifier = modifier,
)

@Composable
fun SectionProgress(
    completed: Int,
    total: Int,
) {
    val progress = completed.toFloat() / total.toFloat()

    val textColor =
        if (progress == 1.0f) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progress },
            color = textColor,
        )
        Text(
            text = completed.toString(),
            color = textColor,
        )
    }
}

@Composable
fun Modifier.textFieldBorder(): Modifier =
    this then
        Modifier.border(
            width = OutlinedTextFieldDefaults.UnfocusedBorderThickness,
            color = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
            shape = OutlinedTextFieldDefaults.shape,
        )

@Composable
fun HabitInfoChip(
    text: String,
    icon: ImageVector,
    habitStatus: HabitStatus = HabitStatus.Normal,
) {
    val (containerColor, labelColor) =
        when (habitStatus) {
            HabitStatus.Normal -> Pair(Color.Transparent, AssistChipDefaults.assistChipColors().labelColor)
            HabitStatus.Silver -> Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
            HabitStatus.Gold -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            HabitStatus.Platinum -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        }

    AssistChip(
        colors = AssistChipDefaults.assistChipColors().copy(containerColor = containerColor, labelColor = labelColor),
//        onClick = { openLink(USER_GUIDE_URL, ctx) },
        onClick = {},
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                contentDescription = null,
            )
        },
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun HabitChipsFlowRow(
    habit: Habit,
    settings: AppSettings?,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        modifier = modifier,
    ) {
        if (habit.context?.isNotBlank() == true) {
            HabitInfoChip(
                text = habit.context,
                icon = Icons.Default.Schedule,
            )
        }
        HabitStreakInfoChip(
            streak = habit.streak,
            frequency = habit.frequency,
            settings = settings,
        )
        HabitDaysCompletedInfoChip(
            completed = habit.completed,
            useTasksInsteadOfDaysString = false,
            settings = settings,
        )
        HabitPointsInfoChip(
            points = habit.points,
            settings = settings,
        )
        HabitScoreInfoChip(
            score = habit.score,
            settings = settings,
        )
    }
}

@Composable
fun HabitStreakInfoChip(
    streak: Int,
    frequency: Int,
    settings: AppSettings?,
) {
    if (!(settings?.hideStreakOnHome ?: 0).toBool()) {
        val freq = HabitFrequency.entries[frequency]
        // Streak has special colors
        val habitStatus = habitStatusFromStreak(streak)
        val text =
            if (!(settings?.hideChipDescriptions ?: 0).toBool()) {
                stringResource(
                    when (freq) {
                        HabitFrequency.Daily -> R.string.x_day_streak
                        HabitFrequency.Weekly -> R.string.x_week_streak
                        HabitFrequency.Monthly -> R.string.x_month_streak
                        HabitFrequency.Yearly -> R.string.x_year_streak
                    },
                    prettyFormat(streak),
                )
            } else {
                prettyFormat(streak)
            }
        HabitInfoChip(
            text = text,
            habitStatus = habitStatus,
            icon = Icons.AutoMirrored.Default.ShowChart,
        )
    }
}

@Composable
fun HabitDaysCompletedInfoChip(
    completed: Int,
    useTasksInsteadOfDaysString: Boolean,
    settings: AppSettings?,
) {
    val (countString, icon) =
        if (useTasksInsteadOfDaysString) {
            Pair(R.string.x_tasks_completed, Icons.Default.Check)
        } else {
            Pair(R.string.x_days_completed, Icons.Default.Today)
        }

    if (!(settings?.hideDaysCompletedOnHome ?: 0).toBool()) {
        val text =
            if (!(settings?.hideChipDescriptions ?: 0).toBool()) {
                stringResource(
                    countString,
                    prettyFormat(completed),
                )
            } else {
                prettyFormat(completed)
            }

        HabitInfoChip(
            text = text,
            icon = icon,
        )
    }
}

@Composable
fun HabitScoreInfoChip(
    score: Int,
    settings: AppSettings?,
) {
    if (!(settings?.hideScoreOnHome ?: 0).toBool()) {
        val text =
            if (!(settings?.hideChipDescriptions ?: 0).toBool()) {
                stringResource(
                    R.string.x_percent_complete,
                    score.toString(),
                )
            } else {
                "$score%"
            }

        HabitInfoChip(
            text = text,
            icon = Icons.Default.Check,
        )
    }
}

fun habitStatusFromStreak(streak: Int) =
    when (streak) {
        in 0..3 -> HabitStatus.Normal
        in 4..7 -> HabitStatus.Silver
        in 8..21 -> HabitStatus.Gold
        in 22..500 -> HabitStatus.Platinum
        else -> HabitStatus.Normal
    }

@Composable
fun HabitPointsInfoChip(
    points: Int,
    settings: AppSettings?,
) {
    if (!(settings?.hidePointsOnHome ?: 0).toBool()) {
        val text =
            if (!(settings?.hideChipDescriptions ?: 0).toBool()) {
                stringResource(
                    R.string.x_points,
                    prettyFormat(points),
                )
            } else {
                prettyFormat(points)
            }
        HabitInfoChip(
            text = text,
            icon = Icons.Outlined.FavoriteBorder,
        )
    }
}

@Composable
@Preview
fun HabitChipsFlowRowPreview() {
    HabitChipsFlowRow(
        habit = sampleHabit,
        settings = null,
    )
}

@Composable
@Preview
fun HabitChipsFlowRowNoDescriptionsPreview() {
    HabitChipsFlowRow(
        habit = sampleHabit,
        settings = sampleAppSettings.copy(hideChipDescriptions = 1),
    )
}

@Composable
@Preview
fun SectionProgressPreview() {
    SectionProgress(3, 3)
}
