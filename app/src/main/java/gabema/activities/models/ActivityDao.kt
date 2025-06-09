package gabema.activities.models

import androidx.room.*

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities")
    suspend fun getAll(): List<Activity>

    @Insert
    suspend fun insert(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)
}