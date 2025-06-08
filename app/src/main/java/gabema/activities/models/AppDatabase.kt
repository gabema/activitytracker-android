import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ActivityEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}