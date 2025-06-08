import androidx.room.*

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities")
    suspend fun getAll(): List<ActivityEntity>

    @Insert
    suspend fun insert(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)
}