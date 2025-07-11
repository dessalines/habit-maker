package com.dessalines.habitmaker.ui.components.habit.habitanddetails.calendars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.db.HabitCheck
import com.dessalines.habitmaker.db.sampleHabitChecks
import com.dessalines.habitmaker.ui.components.common.MEDIUM_PADDING
import com.dessalines.habitmaker.utils.epochMillisToLocalDate
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HabitCalendar(
    habitChecks: List<HabitCheck>,
    firstDayOfWeek: DayOfWeek,
    onClickDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkDates = habitChecks.map { it.checkTime.epochMillisToLocalDate() }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth }
    val firstDayOfWeek = remember { firstDayOfWeek }

    val state =
        rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = firstDayOfWeek,
        )

    HorizontalCalendar(
        modifier = modifier,
        state = state,
        monthHeader = { month ->
            MonthHeader(month)
        },
        dayContent = { day ->
            Day(
                day = day,
                // TODO probably a more efficient way to do this
                // Maybe a hashmap of dates?
                checked = checkDates.contains(day.date),
                onClick = { onClickDay(it.date) },
                modifier = modifier,
            )
        },
    )
}

@Composable
fun MonthHeader(
    calendarMonth: CalendarMonth,
    modifier: Modifier = Modifier,
) {
    val daysOfWeek = calendarMonth.weekDays.first().map { it.date.dayOfWeek }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
    ) {
        val locale = Locale.getDefault()
        Text(
            text = calendarMonth.yearMonth.month.getDisplayName(TextStyle.SHORT, locale),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textDecoration = TextDecoration.Underline,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            for (dayOfWeek in daysOfWeek) {
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    checked: Boolean,
    onClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    //  Only allow clicking dates in the past
    val allowedDate = day.date.isBefore(LocalDate.now().plusDays(1))
    val isToday = day.date == LocalDate.now()

    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clickable(
                    enabled = allowedDate,
                    onClick = { onClick(day) },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null,
            )
        } else {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                // Underline today's date
                textDecoration =
                    if (isToday) {
                        TextDecoration.Underline
                    } else {
                        TextDecoration.None
                    },
                color =
                    if (allowedDate) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
            )
        }
    }
}

@Preview
@Composable
fun HabitCalendarPreview() {
    HabitCalendar(
        habitChecks = sampleHabitChecks,
        firstDayOfWeek = DayOfWeek.SUNDAY,
        onClickDay = {},
    )
}
