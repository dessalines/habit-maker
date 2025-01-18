package com.dessalines.habitmaker.db
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
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("habit_id"),
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["habit_id", "time", "day"], unique = true)],
)
data class HabitReminder(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "time",
    )
    val time: LocalTime,
    @ColumnInfo(
        name = "day",
    )
    val day: DayOfWeek,
)

@Entity
data class HabitReminderInsert(
    @ColumnInfo(
        name = "habit_id",
    )
    val habitId: Int,
    @ColumnInfo(
        name = "time",
    )
    val time: LocalTime,
    @ColumnInfo(
        name = "day",
    )
    val day: DayOfWeek,
)

private const val BY_HABIT_ID_QUERY = "SELECT * FROM HabitReminder where habit_id = :habitId"

@Dao
interface HabitReminderDao {
    @Query("SELECT * FROM HabitReminder inner join Habit on HabitReminder.habit_id = Habit.id")
    fun listAllSync(): Map<HabitReminder, Habit>

    @Query(BY_HABIT_ID_QUERY)
    fun listForHabitSync(habitId: Int): List<HabitReminder>

    @Insert(entity = HabitReminder::class, onConflict = OnConflictStrategy.IGNORE)
    fun insert(habitReminder: HabitReminderInsert): Long

    @Query("DELETE FROM HabitReminder where habit_id = :habitId")
    fun delete(habitId: Int)
}

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HabitReminderRepository(
    private val habitReminderDao: HabitReminderDao,
) {
    fun listAllSync() = habitReminderDao.listAllSync()

    fun listForHabitSync(habitId: Int) = habitReminderDao.listForHabitSync(habitId)

    fun insert(habitReminder: HabitReminderInsert) = habitReminderDao.insert(habitReminder)

    fun delete(habitId: Int) = habitReminderDao.delete(habitId)
}

class HabitReminderViewModel(
    private val repository: HabitReminderRepository,
) : ViewModel() {
    fun listAllSync() = repository.listAllSync()

    fun listForHabitSync(habitId: Int) = repository.listForHabitSync(habitId)

    fun insert(habitReminder: HabitReminderInsert) = repository.insert(habitReminder)

    fun delete(habitId: Int) = repository.delete(habitId)
}

class HabitReminderViewModelFactory(
    private val repository: HabitReminderRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitReminderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
