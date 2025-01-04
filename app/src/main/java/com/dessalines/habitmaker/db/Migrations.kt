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
