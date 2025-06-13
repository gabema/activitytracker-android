package gabema.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gabema.activities.ui.theme.ActivitiesTheme

class EditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityUi = intent.getSerializableExtra("activityUi") as? ActivityUi
        val isClone = intent.getBooleanExtra("isClone", false)
        setContent {
            ActivitiesTheme {
                EditActivityScreen(
                    initial = activityUi,
                    isClone = isClone,
                    onSave = { updated ->
                        val resultIntent = Intent().apply {
                            putExtra("activityUi", updated)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@Composable
fun EditActivityScreen(
    initial: ActivityUi?,
    isClone: Boolean,
    onSave: (ActivityUi) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var typeId by remember { mutableIntStateOf(initial?.typeId ?: 1) }
    var duration by remember { mutableStateOf(initial?.duration?.toString() ?: "0") }
    val whenDate by remember { mutableLongStateOf(initial?.whenDate ?: System.currentTimeMillis()) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = typeId.toString(),
            onValueChange = { v -> typeId = v.toIntOrNull() ?: 1 },
            label = { Text("TypeId") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (ms)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        // Optionally add a date picker for whenDate
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onCancel) { Text("Cancel") }
            Button(onClick = {
                val id = if (isClone) 0 else (initial?.id ?: 0)
                onSave(
                    ActivityUi(
                        id = id,
                        title = title,
                        description = description,
                        typeId = typeId,
                        duration = duration.toLongOrNull() ?: 0L,
                        whenDate = whenDate
                    )
                )
            }) { Text("Save") }
        }
    }
}
