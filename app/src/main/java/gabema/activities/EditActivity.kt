package gabema.activities

import android.content.Intent
import android.app.Activity
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
                        setResult(Activity.RESULT_OK, resultIntent)
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
    var type by remember { mutableStateOf(initial?.type ?: "") }
    var duration by remember { mutableStateOf(initial?.duration ?: "") }
    var group by remember { mutableStateOf(initial?.group ?: "Today") }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = group, onValueChange = { group = it }, label = { Text("Group") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onCancel) { Text("Cancel") }
            Button(onClick = {
                val id = if (isClone) 0 else (initial?.id ?: 0)
                onSave(ActivityUi(id, title, description, type, duration, group))
            }) { Text("Save") }
        }
    }
}
