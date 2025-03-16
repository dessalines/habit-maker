package com.dessalines.habitmaker.utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

fun Long.epochMillisToLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun LocalDate.toEpochMillis() = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toLocalTime(): LocalTime = LocalTime.of(this.hour, this.minute)
