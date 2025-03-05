import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.betone.data.entity.BetEntity

@Dao
interface BetDao {
    // Вставка новой ставки
    @Insert
    fun insert(bet: BetEntity)

    // Получение всех ставок для конкретной ветки
    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC")
    fun getBetsForBranch(branchId: Int): List<BetEntity>

    // Получение последней ставки для ветки (если нужно для проверки)
    @Query("SELECT * FROM bets WHERE branchId = :branchId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBetForBranch(branchId: Int): BetEntity?

    // Очистка ставок для ветки
    @Query("DELETE FROM bets WHERE branchId = :branchId")
    fun clearBetsForBranch(branchId: Int)

    // Получение общего количества ставок (для статистики, если нужно)
    @Query("SELECT COUNT(*) FROM bets")
    fun getBetCount(): Int
}