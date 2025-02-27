package com.dessalines.habitmaker.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dessalines.habitmaker.utils.TAG
import java.util.concurrent.Executors

@Database(
    version = 6,
    entities = [
        AppSettings::class,
        Habit::class,
        Encouragement::class,
        HabitCheck::class,
        HabitReminder::class,
    ],
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDB : RoomDatabase() {
    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun habitDao(): HabitDao

    abstract fun encouragementDao(): EncouragementDao

    abstract fun habitCheckDao(): HabitCheckDao

    abstract fun habitReminderDao(): HabitReminderDao

    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun getDatabase(context: Context): AppDB {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return instance ?: synchronized(this) {
                val i =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDB::class.java,
                            TAG,
                        ).allowMainThreadQueries()
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                        )
                        // Necessary because it can't insert data on creation
                        .addCallback(
                            object : Callback() {
                                override fun onOpen(db: SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Executors.newSingleThreadExecutor().execute {
                                        db.insert(
                                            "AppSettings",
                                            // Ensures it won't overwrite the existing data
                                            CONFLICT_IGNORE,
                                            ContentValues(2).apply {
                                                put("id", 1)
                                            },
                                        )
                                    }
                                }
                            },
                        ).build()
                instance = i
                // return instance
                i
            }
        }
    }
}
