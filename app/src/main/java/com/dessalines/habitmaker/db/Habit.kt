package com.dessalines.habitmaker.db

import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.Serializable

@Entity(
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["points"]),
        Index(value = ["score"]),
        Index(value = ["streak"]),
        Index(value = ["completed"]),
        Index(value = ["last_streak_time"]),
        Index(value = ["last_completed_time"]),
    ],
)
@Keep
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "frequency",
        defaultValue = "0",
    )
    val frequency: Int,
    @ColumnInfo(
        name = "times_per_frequency",
        defaultValue = "1",
    )
    val timesPerFrequency: Int,
    @ColumnInfo(
        name = "notes",
    )
    val notes: String?,
    @ColumnInfo(
        name = "archived",
        defaultValue = "0",
    )
    val archived: Int,
    @ColumnInfo(
        name = "points",
        defaultValue = "0",
    )
    val points: Int,
    @ColumnInfo(
        name = "score",
        defaultValue = "0",
    )
    val score: Int,
    @ColumnInfo(
        name = "streak",
        defaultValue = "0",
    )
    val streak: Int,
    @ColumnInfo(
        name = "completed",
        defaultValue = "0",
    )
    val completed: Int,
    @ColumnInfo(
        name = "context",
    )
    val context: String?,
    /**
     * This can be in the future for non-daily habits.
     */
    @ColumnInfo(
        name = "last_streak_time",
        defaultValue = "0",
    )
    val lastStreakTime: Long,
    @ColumnInfo(
        name = "last_completed_time",
        defaultValue = "0",
    )
    val lastCompletedTime: Long,
) : Serializable

@Entity
data class HabitInsert(
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "frequency",
        defaultValue = "0",
    )
    val frequency: Int,
    @ColumnInfo(
        name = "times_per_frequency",
        defaultValue = "1",
    )
    val timesPerFrequency: Int,
    @ColumnInfo(
        name = "notes",
    )
    val notes: String?,
    @ColumnInfo(
        name = "archived",
        defaultValue = "0",
    )
    val archived: Int,
    @ColumnInfo(
        name = "context",
    )
    val context: String?,
)

@Entity
data class HabitUpdate(
    val id: Int,
    @ColumnInfo(
        name = "name",
    )
    val name: String,
    @ColumnInfo(
        name = "frequency",
        defaultValue = "0",
    )
    val frequency: Int,
    @ColumnInfo(
        name = "times_per_frequency",
        defaultValue = "1",
    )
    val timesPerFrequency: Int,
    @ColumnInfo(
        name = "notes",
    )
    val notes: String?,
    @ColumnInfo(
        name = "archived",
        defaultValue = "0",
    )
    val archived: Int,
    @ColumnInfo(
        name = "context",
    )
    val context: String?,
)

@Entity
data class HabitUpdateStats(
    val id: Int,
    @ColumnInfo(
        name = "points",
        defaultValue = "0",
    )
    val points: Int,
    @ColumnInfo(
        name = "score",
        defaultValue = "0",
    )
    val score: Int,
    @ColumnInfo(
        name = "streak",
        defaultValue = "0",
    )
    val streak: Int,
    @ColumnInfo(
        name = "completed",
        defaultValue = "0",
    )
    val completed: Int,
    @ColumnInfo(
        name = "last_streak_time",
        defaultValue = "0",
    )
    val lastStreakTime: Long,
    @ColumnInfo(
        name = "last_completed_time",
        defaultValue = "0",
    )
    val lastCompletedTime: Long,
)

private const val BY_ID_QUERY = "SELECT * FROM Habit where id = :id"

@Dao
interface HabitDao {
    @Query("SELECT * FROM Habit")
    fun getAll(): Flow<List<Habit>>

    @Query("SELECT * FROM Habit")
    fun getAllSync(): List<Habit>

    @Query(BY_ID_QUERY)
    fun getById(id: Int): Flow<Habit?>

    @Query(BY_ID_QUERY)
    fun getByIdSync(id: Int): Habit?

    @Insert(entity = Habit::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(habit: HabitInsert): Long

    @Update(entity = Habit::class)
    suspend fun update(habit: HabitUpdate)

    @Update(entity = Habit::class)
    suspend fun updateStats(habit: HabitUpdateStats)

    @Delete
    suspend fun delete(habit: Habit)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HabitRepository(
    private val habitDao: HabitDao,
) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val getAll = habitDao.getAll()

    val getAllSync = habitDao.getAllSync()

    fun getById(id: Int) = habitDao.getById(id)

    fun getByIdSync(id: Int) = habitDao.getByIdSync(id)

    fun insert(habit: HabitInsert) = habitDao.insert(habit)

    @WorkerThread
    suspend fun update(habit: HabitUpdate) = habitDao.update(habit)

    @WorkerThread
    suspend fun updateStats(habit: HabitUpdateStats) = habitDao.updateStats(habit)

    @WorkerThread
    suspend fun delete(habit: Habit) = habitDao.delete(habit)
}

class HabitViewModel(
    private val repository: HabitRepository,
) : ViewModel() {
    val getAll = repository.getAll

    val getAllSync = repository.getAllSync

    fun getById(id: Int) = repository.getById(id)

    fun getByIdSync(id: Int) = repository.getByIdSync(id)

    fun insert(habit: HabitInsert) = repository.insert(habit)

    fun update(habit: HabitUpdate) =
        viewModelScope.launch {
            repository.update(habit)
        }

    fun updateStats(habit: HabitUpdateStats) =
        viewModelScope.launch {
            repository.updateStats(habit)
        }

    fun delete(habit: Habit) =
        viewModelScope.launch {
            repository.delete(habit)
        }
}

class HabitViewModelFactory(
    private val repository: HabitRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val sampleHabit =
    Habit(
        id = 1,
        name = "Study Chinese for 10m",
        frequency = 0,
        timesPerFrequency = 1,
        notes = null,
        context = "After breakfast, in my room",
        archived = 0,
        points = 1377,
        score = 0,
        streak = 0,
        completed = 0,
        lastStreakTime = 0L,
        lastCompletedTime = 0L,
    )

val sampleHabit2 =
    Habit(
        id = 1,
        name = "Clean up around the house",
        frequency = 1,
        timesPerFrequency = 3,
        notes = null,
        context = null,
        archived = 0,
        points = 0,
        score = 0,
        streak = 0,
        completed = 0,
        lastStreakTime = 0L,
        lastCompletedTime = 0L,
    )
