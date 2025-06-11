package gabema.activities.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Activity::class, ActivityType::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun activityTypeDao() : ActivityTypeDao
}