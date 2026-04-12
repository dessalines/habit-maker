package com.dessalines.habitmaker

import android.app.Application
import com.dessalines.habitmaker.db.AppDB
import com.dessalines.habitmaker.db.AppSettingsRepository
import com.dessalines.habitmaker.db.EncouragementRepository
import com.dessalines.habitmaker.db.HabitCheckRepository
import com.dessalines.habitmaker.db.HabitReminderRepository
import com.dessalines.habitmaker.db.HabitRepository
import com.google.android.gms.wearable.Wearable

// TODO rename this
class SampleApplication : Application() {
    val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
    private val database by lazy { AppDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val encouragementRepository by lazy { EncouragementRepository(database.encouragementDao()) }
    val habitCheckRepository by lazy { HabitCheckRepository(database.habitCheckDao()) }
    val habitReminderRepository by lazy { HabitReminderRepository(database.habitReminderDao()) }
}
