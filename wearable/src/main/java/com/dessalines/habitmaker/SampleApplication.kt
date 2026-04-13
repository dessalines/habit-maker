package com.dessalines.habitmaker

import android.app.Application
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitRepository

// TODO rename this
class SampleApplication : Application() {
    private val database by lazy { AppDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val habitCheckRepository by lazy { HabitCheckRepository(database.habitCheckDao()) }
}
