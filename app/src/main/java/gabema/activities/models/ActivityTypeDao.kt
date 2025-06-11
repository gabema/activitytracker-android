package gabema.activities.models

import androidx.room.*

@Dao
interface ActivityTypeDao {
    @Query("SELECT * FROM activity_types")
    suspend fun getAll(): List<ActivityType>

    @Insert
    suspend fun insert(activityType: ActivityType)

    @Delete
    suspend fun delete(activityType: ActivityType)
}