package com.dessalines.habitmaker.db

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("habit_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "check_time"], unique = true)],
)
@Keep
@Serializable
data class HabitCheck(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "check_time",
    )
    val checkTime: Long,
)

@Entity
@Serializable
data class HabitCheckInsert(
    // Necessary for DB sync
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "check_time",
    )
    val checkTime: Long,
)

@Entity
@Serializable
data class HabitCheckDelete(
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "check_time",
    )
    val checkTime: Long,
)

private const val BY_HABIT_ID_QUERY = "SELECT * FROM HabitCheck where habit_id = :habitId order by check_time"

@Dao
interface HabitCheckDao {
    @Query("SELECT * FROM HabitCheck")
    fun getAllSync(): List<HabitCheck>

    @Query(BY_HABIT_ID_QUERY)
    fun listForHabit(habitId: Int): Flow<List<HabitCheck>>

    @Query(BY_HABIT_ID_QUERY)
    fun listForHabitSync(habitId: Int): List<HabitCheck>

    @Insert(entity = HabitCheck::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(habitCheck: HabitCheckInsert): Long

    // TODO
//    @Query("DELETE FROM HabitCheck where habit_id = :habitId and check_time = :checkTime")
    @Delete(entity = HabitCheck::class)
    fun deleteForDay(habitCheck: HabitCheckDelete)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HabitCheckRepository(
    private val habitCheckDao: HabitCheckDao,
) {
    val getAllSync = habitCheckDao.getAllSync()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun listForHabit(habitId: Int) = habitCheckDao.listForHabit(habitId)

    fun listForHabitSync(habitId: Int) = habitCheckDao.listForHabitSync(habitId)

    fun insert(habitCheck: HabitCheckInsert) = habitCheckDao.insert(habitCheck)

    fun deleteForDay(habitCheck: HabitCheckDelete) = habitCheckDao.deleteForDay(habitCheck)
}

val sampleHabitChecks =
    listOf(
        HabitCheck(
            id = 1,
            habitId = 1,
            checkTime = 0,
        ),
        HabitCheck(
            id = 2,
            habitId = 2,
            checkTime = 0,
        ),
    )
