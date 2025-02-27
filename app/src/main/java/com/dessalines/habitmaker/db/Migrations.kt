package com.dessalines.habitmaker.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dessalines.habitmaker.utils.toEpochMillis
import java.time.LocalDate

/**
 * Adds a context column for habits (IE when / where)
 */
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE Habit ADD COLUMN context TEXT",
            )
        }
    }

/**
 * Replace the completed boolean column, for a last_streak_time column.
 */
val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val now = LocalDate.now().toEpochMillis()
            db.execSQL(
                "ALTER TABLE Habit ADD COLUMN last_streak_time INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                "ALTER TABLE Habit ADD COLUMN last_completed_time INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                "UPDATE Habit set last_streak_time = $now, last_completed_time = $now where completed = 1",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_Habit_last_streak_time` ON Habit (last_streak_time)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_Habit_last_completed_time` ON Habit (last_completed_time)")
        }
    }

/**
 * Create a table for habit reminders
 */
val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS HabitReminder (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `habit_id` INTEGER NOT NULL,
                    `time` INTEGER NOT NULL,
                    `day` INTEGER NOT NULL,
                    FOREIGN KEY(`habit_id`) REFERENCES `Habit`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_HabitReminder_habit_id_time_day` ON `HabitReminder` (`habit_id`, `time`,`day`)",
            )
        }
    }

/**
 * Add a setting to hide the chip descriptions.
 */
val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE AppSettings ADD COLUMN hide_chip_descriptions INTEGER NOT NULL DEFAULT 0",
            )
        }
    }

/**
 * Add a setting to hide the days completed on home
 */
val MIGRATION_5_6 =
    object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE AppSettings ADD COLUMN hide_days_completed_on_home INTEGER NOT NULL DEFAULT 0",
            )
        }
    }
