package com.galtagency.pointstracker

// ... other imports
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
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
        PointsRepository.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            PointsTrackerTheme {
                val currentPoints by PointsRepository.points.collectAsState()
                val currentThreshold by PointsRepository.threshold.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        points = currentPoints,
                        threshold = currentThreshold,
                        onThresholdChange = { newThreshold ->
                            PointsRepository.setThreshold(newThreshold)
                        },
                        onPointsChange = { newPoints ->
                            PointsRepository.setPoints(newPoints)
                        },
                        onResetClick = { PointsRepository.resetPoints() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val componentName = ComponentName(this, PointsTrackerService::class.java)
        return enabledListeners?.split(":")?.map {
            ComponentName.unflattenFromString(it)
        }?.any { it == componentName } ?: false
    }
}


@Composable
fun MainScreen(
    points: Int,
    threshold: Int,
    onThresholdChange: (Int) -> Unit,
    onPointsChange: (Int) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var thresholdText by remember(threshold) { mutableStateOf(threshold.toString()) }
    var pointsText by remember(points) { mutableStateOf(points.toString()) }

    val currentPoints = pointsText.toIntOrNull() ?: 0
    val currentThreshold = thresholdText.toIntOrNull()?.takeIf { it > 0 } ?: 1
    val progress =
        (currentPoints.toFloat() / currentThreshold.toFloat()).coerceIn(
            0f,
            1f
        )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp), // Increased padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$points",
            style = MaterialTheme.typography.displayLarge, // Bigger, more prominent text
            color = MaterialTheme.colorScheme.primary
        )

        val pointsPlural = if (points == 1) "point" else "points"
        Text(
            text = "Accumulated $pointsPlural",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeCap = StrokeCap.Butt // Rectangular progress bar
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = MaterialTheme.colorScheme.background,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp)) // More space
        OutlinedTextField(
            value = pointsText,
            onValueChange = { newText ->
                if (newText.all { it.isDigit() }) {
                    pointsText = newText
                    newText.toIntOrNull()?.let {
                        onPointsChange(it)
                    }
                }
            },
            label = { Text("Set Points") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = thresholdText,
            onValueChange = { newText ->
                if (newText.all { it.isDigit() }) {
                    thresholdText = newText
                    newText.toIntOrNull()?.let {
                        onThresholdChange(it)
                    }
                }
            },
            label = { Text("Set Notification Threshold") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onResetClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset Points")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PointsTrackerTheme {
        MainScreen(
            points = 150,
            threshold = 1000,
            onThresholdChange = {},
            onResetClick = {},
            onPointsChange = {})
    }
}
