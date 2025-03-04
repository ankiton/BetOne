import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.betone.data.entity.BankEntity
import com.example.betone.data.entity.BetBranchEntity
import com.example.betone.data.entity.BetEntity

@Database(entities = [BankEntity::class, BetBranchEntity::class, BetEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun branchDao(): BranchDao
    abstract fun betDao(): BetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "betting_db"
                ).build() // Здесь возникает NullPointerException
                INSTANCE = instance
                instance
            }
        }
    }
}