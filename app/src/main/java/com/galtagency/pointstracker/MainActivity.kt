package com.galtagency.pointstracker

// ... other imports
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.galtagency.pointstracker.ui.theme.PointsTrackerTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        enableEdgeToEdge()
        setContent {
            PointsTrackerTheme {
                // Collect both points and threshold as state
                val currentPoints by PointsRepository.points.collectAsState()
                val currentThreshold by PointsRepository.threshold.collectAsState()
                PointsRepository.initialize(applicationContext)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pass both values to the main screen composable
                    MainScreen(
                        points = currentPoints,
                        threshold = currentThreshold,
                        onThresholdChange = { newThreshold ->
                            PointsRepository.setThreshold(newThreshold)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        // ... (this function remains the same)
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val componentName = ComponentName(this, PointsTrackerService::class.java)
        return enabledListeners?.split(":")?.map {
            ComponentName.unflattenFromString(it)
        }?.any { it == componentName } ?: false
    }
}

// Renamed Greeting to MainScreen for clarity
@Composable
fun MainScreen(
    points: Int,
    threshold: Int,
    onThresholdChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // A local state for the text field to prevent recomposing on every key press
    var thresholdText by remember(threshold) { mutableStateOf(threshold.toString()) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val pointsText = if (points == 1) "point" else "points"
        Text(text = "Accumulated $points $pointsText since last notification.")

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = thresholdText,
            onValueChange = { newText ->
                // Allow only digits
                if (newText.all { it.isDigit() }) {
                    thresholdText = newText
                    newText.toIntOrNull()?.let {
                        onThresholdChange(it)
                    }
                }
            },
            label = { Text("Notification Threshold") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PointsTrackerTheme {
        MainScreen(points = 150, threshold = 1000, onThresholdChange = {})
    }
}
