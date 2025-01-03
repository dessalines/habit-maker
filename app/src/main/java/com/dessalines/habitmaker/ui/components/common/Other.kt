package com.dessalines.habitmaker.ui.components.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettings
import com.dessalines.habitmaker.db.Habit
import com.dessalines.habitmaker.db.sampleHabit
import com.dessalines.habitmaker.utils.HabitFrequency
import com.dessalines.habitmaker.utils.HabitStatus
import com.dessalines.habitmaker.utils.toBool

@Composable
fun SectionTitle(title: String) =
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = LARGE_PADDING),
        style = MaterialTheme.typography.titleLarge,
    )

@Composable
fun TodayCompletedCount(todayCompletedCount: Int) =
    Text(
        text = stringResource(R.string.completed_count_alt, todayCompletedCount),
        modifier = Modifier.padding(horizontal = LARGE_PADDING),
        style = MaterialTheme.typography.bodyMedium,
    )

@Composable
fun SectionDivider() = HorizontalDivider(modifier = Modifier.padding(vertical = MEDIUM_PADDING))

@Composable
fun textFieldBorder() =
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

        if (!(settings?.hideStreakOnHome ?: 0).toBool()) {
            val freq = HabitFrequency.entries[habit.frequency]
            // Streak has special colors
            val habitStatus = habitStatusFromStreak(habit.streak)
            HabitInfoChip(
                text =
                    stringResource(
                        when (freq) {
                            HabitFrequency.Daily -> R.string.x_day_streak
                            HabitFrequency.Weekly -> R.string.x_week_streak
                            HabitFrequency.Monthly -> R.string.x_month_streak
                            HabitFrequency.Yearly -> R.string.x_year_streak
                        },
                        habit.streak.toString(),
                    ),
                habitStatus = habitStatus,
                icon = Icons.AutoMirrored.Default.ShowChart,
            )
        }
        if (!(settings?.hidePointsOnHome ?: 0).toBool()) {
            HabitInfoChip(
                text =
                    stringResource(
                        R.string.x_points,
                        habit.points.toString(),
                    ),
                icon = Icons.Outlined.FavoriteBorder,
            )
        }
        if (!(settings?.hideScoreOnHome ?: 0).toBool()) {
            HabitInfoChip(
                text =
                    stringResource(
                        R.string.x_percent_complete,
                        habit.score.toString(),
                    ),
                icon = Icons.Default.Check,
            )
        }
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
@Preview
fun HabitChipsFlowRowPreview() {
    HabitChipsFlowRow(
        habit = sampleHabit,
        settings = null,
    )
}
