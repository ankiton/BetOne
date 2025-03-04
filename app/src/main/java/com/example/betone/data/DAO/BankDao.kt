import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.betone.data.entity.BankEntity

@Dao
interface BankDao {
    // Вставка нового банка (например, при инициализации или сбросе месяца)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bank: BankEntity)

    // Получение текущего банка (последнего по дате)
    @Query("SELECT * FROM bank ORDER BY startDate DESC LIMIT 1")
    fun getLatestBank(): BankEntity

    // Получение всех банков (если нужно для истории или отладки)
    @Query("SELECT * FROM bank")
    fun getAllBanks(): List<BankEntity>

    // Очистка таблицы (для сброса, если потребуется)
    @Query("DELETE FROM bank")
    fun clearBank()
}