package com.dessalines.habitmaker.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Long.epochMillisToLocalDate() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun Long.epochMillisToLocalDateTime() = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()

fun LocalDate.toEpochMillis() = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
