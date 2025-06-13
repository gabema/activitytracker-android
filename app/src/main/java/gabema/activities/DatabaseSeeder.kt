package gabema.activities

import gabema.activities.models.Activity
import gabema.activities.models.ActivityType
import gabema.activities.models.AppDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseSeeder {
    fun seed(db: AppDatabase, dbInstance: SupportSQLiteDatabase) {
        val types = listOf("Anaerobic", "Cardio", "Treat", "Activity")
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val typeDao = db.activityTypeDao()
            types.forEach { typeName ->
                typeDao.insert(ActivityType(id = 0, name = typeName))
            }
            // Insert test data into activity table
            val activityDao = db.activityDao()
            demoActivities.forEach { activity ->
                activityDao.insert(Activity(
                    id = 0,
                    title = activity.title,
                    description = activity.description,
                    typeId = activity.typeId,
                    whenDateTime = activity.whenDate,
                    durationMillis = activity.duration
                ))
            }
        }
    }
}

// Demo data for seeding
val demoActivities = listOf(
    ActivityUi(1, "Arm Lifts", "15 reps @ 15 lb dumbbells", 1, 23 * 60 * 1000, java.time.Instant.parse("2025-06-12T05:50").toEpochMilli()),
    ActivityUi(2, "Arm Curls", "15 reps @ 15 lb dumbbells", 1, 15 * 60 * 1000, java.time.Instant.parse("2025-06-12T04:50").toEpochMilli()),
    ActivityUi(3, "Run", "2.5 mile grueling run", 2, 25 * 60 * 1000, java.time.Instant.parse("2025-06-12T02:50").toEpochMilli()),
    ActivityUi(4, "Walk", "4 mile stroll", 2, 2 * 60 * 60 * 1000 + 13 * 60 * 1000, java.time.Instant.parse("2025-06-12T02:50").toEpochMilli()),
    ActivityUi(5, "Chocolate Fun Size", "40 calories", 3, 0, java.time.Instant.parse("2025-06-11T20:50").toEpochMilli()),
    ActivityUi(6, "Bible Reading", "1 John", 4, 15 * 60 * 1000, java.time.Instant.parse("2025-06-11T18:40").toEpochMilli())
)
