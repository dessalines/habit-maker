package com.dessalines.habitmaker.db

import java.time.LocalDate
import java.time.ZoneId

const val TAG = "com.habitmaker.db"

fun LocalDate.toEpochMillis() = this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
