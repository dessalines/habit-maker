package com.dessalines.habitmaker.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
