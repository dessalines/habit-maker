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

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("habit_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "content"], unique = true)],
)
@Keep
data class Encouragement(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "content",
    )
    val content: String,
)

@Entity
data class EncouragementInsert(
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "content",
    )
    val content: String,
)

private const val BY_HABIT_ID_QUERY = "SELECT * FROM Encouragement where habit_id = :habitId"

@Dao
interface EncouragementDao {
    @Query(BY_HABIT_ID_QUERY)
    fun listForHabitSync(habitId: Int): List<Encouragement>

    @Query("SELECT * FROM Encouragement where habit_id = :habitId ORDER BY RANDOM() LIMIT 1")
    fun getRandomForHabit(habitId: Int): Encouragement?

    @Insert(entity = Encouragement::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(encouragement: EncouragementInsert): Long

    @Query("DELETE FROM Encouragement where habit_id = :habitId")
    fun deleteForHabit(habitId: Int)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class EncouragementRepository(
    private val encouragementDao: EncouragementDao,
) {
    fun listForHabitSync(habitId: Int) = encouragementDao.listForHabitSync(habitId)

    fun getRandomForHabit(habitId: Int) = encouragementDao.getRandomForHabit(habitId)

    fun deleteForHabit(habitId: Int) = encouragementDao.deleteForHabit(habitId)

    fun insert(encouragement: EncouragementInsert) = encouragementDao.insert(encouragement)
}

class EncouragementViewModel(
    private val repository: EncouragementRepository,
) : ViewModel() {
    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun getRandomForHabit(habitId: Int) = repository.getRandomForHabit(habitId)

    fun deleteForHabit(habitId: Int) = repository.deleteForHabit(habitId)

    fun insert(encouragement: EncouragementInsert) = repository.insert(encouragement)
}

class EncouragementViewModelFactory(
    private val repository: EncouragementRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EncouragementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EncouragementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val sampleEncouragements =
    listOf(
        Encouragement(
            id = 1,
            habitId = 1,
            content = "Great job, keep going!",
        ),
        Encouragement(
            id = 2,
            habitId = 1,
            content = "Excellent! Remember why you're doing this.",
        ),
        Encouragement(
            id = 3,
            habitId = 1,
            content = "Nice! You're almost there!",
        ),
    )
