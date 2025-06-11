package gabema.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import gabema.activities.models.Activity
import gabema.activities.models.ActivityType
import gabema.activities.models.AppDatabase
import gabema.activities.ui.theme.ActivitiesTheme
import java.io.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ActivityUi(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val duration: String,
    val group: String // "Today", "Yesterday", etc.
) : Serializable

private val demoActivities = listOf(
    ActivityUi(1, "Arm Lifts", "15 reps @ 15 lb dumbbells", "Anaerobic", "15 min", "Today"),
    ActivityUi(2, "Arm Curls", "15 reps @ 15 lb dumbbells", "Anaerobic", "15 min", "Today"),
    ActivityUi(3, "Run", "2.5 mile grueling run", "Cardio", "25 min", "Today"),
    ActivityUi(4, "Walk", "4 mile stroll", "Cardio", "2 hr 13 min", "Today"),
    ActivityUi(5, "Chocolate Fun Size", "40 calories", "Treat", "—", "Yesterday"),
    ActivityUi(6, "Bible Reading", "1 John", "Activity", "15 min", "Yesterday")
)

fun typeColor(type: String): Color = when (type) {
    "Anaerobic" -> Color(0xFFB2DFDB)
    "Cardio" -> Color(0xFFBBDEFB)
    "Treat" -> Color(0xFFFFCDD2)
    "Activity" -> Color(0xFFFFF9C4)
    else -> Color.LightGray
}

class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "activities-db"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(dbInstance: SupportSQLiteDatabase) {
                super.onCreate(dbInstance)
                // Insert control data into activity_types using ActivityTypeDao
                val types = listOf("Anaerobic", "Cardio", "Treat", "Activity")
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    val dao = db.activityTypeDao()
                    types.forEach { typeName ->
                        dao.insert(ActivityType(id = 0, name = typeName))
                    }
                }

                // Insert test data into activity table
                scope.launch {
                    val dao = db.activityDao()
                    demoActivities.forEach { activity ->
                        dao.insert(Activity(
                            id = 0,
                            title = activity.title,
                            description = activity.description,
                            typeId = 1, // TODO Fix mapping
                            whenDateTime = 12820300, // TODO Fix mapping
                            durationMillis = 3600, // TODO Fix mapping
                        ))
                    }
                }
            }
        })
        .build()

        enableEdgeToEdge()
        setContent {
            ActivitiesTheme {
                val activityDao = db.activityDao()
                val activityTypeDao = db.activityTypeDao()
                var activities by remember { mutableStateOf<List<ActivityUi>>(emptyList()) }
                var activityTypes by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
                val coroutineScope = rememberCoroutineScope()

                // Load activities and types from DB
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val types = activityTypeDao.getAll().associateBy({ it.id }, { it.name })
                        val acts = activityDao.getAll()
                        withContext(Dispatchers.Main) {
                            activityTypes = types
                            activities = acts.map { entity ->
                                ActivityUi(
                                    id = entity.id,
                                    title = entity.title,
                                    description = entity.description,
                                    type = types[entity.typeId] ?: "",
                                    duration = formatDuration(entity.durationMillis),
                                    group = groupForDate(entity.whenDateTime)
                                )
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ActivityListScreen(
                        modifier = Modifier.padding(innerPadding),
                        activities = activities,
                        onEdit = { /* TODO: Implement edit */ },
                        onClone = { /* TODO: Implement clone */ },
                        onDelete = { /* TODO: Implement delete */ }
                    )
                }
            }
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (hours > 0) append("${hours} hr ")
        if (minutes > 0) append("${minutes} min ")
        if (seconds > 0) append("${seconds} sec")
    }.trim()
}

fun groupForDate(epochMillis: Long): String {
    // Simple grouping: Today, Yesterday, or date string
    val now = LocalDate.now()
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return when {
        date == now -> "Today"
        date == now.minusDays(1) -> "Yesterday"
        else -> date.toString()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ActivitiesTheme {
        Greeting("Android")
    }
}

@Composable
fun ActivityListScreen(
    modifier: Modifier = Modifier,
    activities: List<ActivityUi> = emptyList(),
    onEdit: (ActivityUi) -> Unit = {},
    onClone: (ActivityUi) -> Unit = {},
    onDelete: (ActivityUi) -> Unit = {}
) {
    var filter by remember { mutableStateOf("") }
    val grouped = activities
        .filter { it.title.contains(filter, true) || it.description.contains(filter, true) }
        .groupBy { it.group }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = filter,
            onValueChange = { filter = it },
            label = { Text("Filter by Type or title/description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (group, items) ->
                if (group != "Today") {
                    item {
                        Text(
                            text = group,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
                items(items, key = { it.id }) { activity ->
                    var dismissed by remember { mutableStateOf(false) }
                    if (!dismissed) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(activity) {
                                    detectDragGestures { change, dragAmount ->
                                        if (dragAmount.getDistance() > 100f) {
                                            dismissed = true
                                            onDelete(activity)
                                        }
                                    }
                                }
                                .pointerInput(activity) {
                                    detectTapGestures(
                                        onTap = { onEdit(activity) },
                                        onLongPress = { onClone(activity) }
                                    )
                                }
                        ) {
                            ActivityCard(activity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(activity: ActivityUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(typeColor(activity.type))
            .padding(16.dp)
            .padding(vertical = 4.dp)
    ) {
        Text(activity.title, fontWeight = FontWeight.Bold)
        Text(activity.description)
        if (activity.duration != "—") {
            Text(
                activity.duration,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}