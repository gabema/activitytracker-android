package gabema.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.unit.dp
import androidx.room.Room
import gabema.activities.models.AppDatabase
import gabema.activities.ui.theme.ActivitiesTheme
import kotlinx.coroutines.launch

data class ActivityUi(
    val id: Int,
    val title: String,
    val description: String,
    val type: String,
    val duration: String,
    val group: String // "Today", "Yesterday", etc.
)

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
        ).build()

        val editLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updated = result.data?.getSerializableExtra("activityUi") as? ActivityUi
                // TODO: Update the list with the edited or cloned activity
            }
        }

        enableEdgeToEdge()
        setContent {
            ActivitiesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ActivityListScreen(
                        modifier = Modifier.padding(innerPadding),
                        onEdit = { activityUi ->
                            val intent = Intent(this, EditActivity::class.java)
                            intent.putExtra("activityUi", activityUi)
                            intent.putExtra("isClone", false)
                            editLauncher.launch(intent)
                        },
                        onClone = { activityUi ->
                            val intent = Intent(this, EditActivity::class.java)
                            intent.putExtra("activityUi", activityUi)
                            intent.putExtra("isClone", true)
                            editLauncher.launch(intent)
                        },
                        onDelete = { activityUi ->
                            // TODO: Remove from list or database
                        }
                    )
                }
            }
        }
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
    onEdit: (ActivityUi) -> Unit = {},
    onClone: (ActivityUi) -> Unit = {},
    onDelete: (ActivityUi) -> Unit = {}
) {
    var filter by remember { mutableStateOf("") }
    var activities by remember { mutableStateOf(demoActivities.toMutableList()) }
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
                                        if (dragAmount < -100 || dragAmount > 100) {
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