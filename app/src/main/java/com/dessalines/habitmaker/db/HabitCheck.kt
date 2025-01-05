package com.dessalines.habitmaker.db

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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
data class HabitCheckInsert(
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
    @Query(BY_HABIT_ID_QUERY)
    fun listForHabit(habitId: Int): Flow<List<HabitCheck>>

    @Query(BY_HABIT_ID_QUERY)
    fun listForHabitSync(habitId: Int): List<HabitCheck>

    @Insert(entity = HabitCheck::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(habitCheck: HabitCheckInsert): Long

    @Query("DELETE FROM HabitCheck where habit_id = :habitId and check_time = :checkTime")
    fun deleteForDay(
        habitId: Int,
        checkTime: Long,
    )
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HabitCheckRepository(
    private val habitCheckDao: HabitCheckDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun listForHabit(habitId: Int) = habitCheckDao.listForHabit(habitId)

    fun listForHabitSync(habitId: Int) = habitCheckDao.listForHabitSync(habitId)

    fun insert(habitCheck: HabitCheckInsert) = habitCheckDao.insert(habitCheck)

    fun deleteForDay(
        habitId: Int,
        checkTime: Long,
    ) = habitCheckDao.deleteForDay(habitId, checkTime)
}

class HabitCheckViewModel(
    private val repository: HabitCheckRepository,
) : ViewModel() {
    fun listForHabit(habitId: Int) = repository.listForHabit(habitId)

    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun insert(habitCheck: HabitCheckInsert) = repository.insert(habitCheck)

    fun deleteForDay(
        habitId: Int,
        checkTime: Long,
    ) = repository.deleteForDay(habitId, checkTime)
}

class HabitCheckViewModelFactory(
    private val repository: HabitCheckRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitCheckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitCheckViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
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
