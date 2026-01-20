package com.galtagency.pointstracker

// ... other imports
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.galtagency.pointstracker.ui.theme.PointsTrackerTheme
import java.text.NumberFormat
import java.util.Locale

private fun formatWithCommas(value: Int): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)

private fun parseWithCommas(text: String): Int? =
    text.replace(",", "").toIntOrNull()


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
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
    var thresholdText by remember(threshold) {
        val text = formatWithCommas(threshold)
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }
    var pointsText by remember(points) {
        val text = formatWithCommas(points)
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }

    val currentPoints = parseWithCommas(pointsText.text) ?: 0
    val currentThreshold = parseWithCommas(thresholdText.text)?.takeIf { it > 0 } ?: 1
    val progressTarget = (currentPoints.toFloat() / currentThreshold.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        label = "progressAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp), // Increased padding
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                strokeCap = StrokeCap.Butt
            )

            val percentText = "${(animatedProgress * 100).toInt()}%"
            val outlineColor = MaterialTheme.colorScheme.secondaryContainer
            val textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = outlineColor,
                    offset = Offset(4f, 4f),
                    blurRadius = 8f
                )
            )

            Text(
                text = percentText,
                color = MaterialTheme.colorScheme.primary,
                style = textStyle
            )
        }

        Text(
            text = formatWithCommas(currentPoints),
            style = MaterialTheme.typography.displayLarge, // Bigger, more prominent text
            color = MaterialTheme.colorScheme.primary
        )

        val pointsPlural = if (currentPoints == 1) "point" else "points"
        Text(
            text = "Accumulated $pointsPlural since last notification",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))



        OutlinedTextField(
            value = pointsText,
            onValueChange = { newValue ->
                val newText = newValue.text
                if (newText.all { it.isDigit() || it == ',' }) {
                    if (newText.isEmpty()) {
                        pointsText = TextFieldValue("", TextRange(0))
                    } else {
                        parseWithCommas(newText)?.let { parsed ->
                            val formatted = formatWithCommas(parsed)
                            val cursorPos = formatted.length
                            pointsText = TextFieldValue(formatted, TextRange(cursorPos))
                            onPointsChange(parsed)
                        }
                    }
                }
            },
            label = { Text("Set Points") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = thresholdText,
            onValueChange = { newValue ->
                val newText = newValue.text
                if (newText.all { it.isDigit() || it == ',' }) {
                    if (newText.isEmpty()) {
                        thresholdText = TextFieldValue("", TextRange(0))
                    } else {
                        parseWithCommas(newText)?.let { parsed ->
                            val formatted = formatWithCommas(parsed)
                            val cursorPos = formatted.length
                            thresholdText = TextFieldValue(formatted, TextRange(cursorPos))
                            onThresholdChange(parsed)
                        }
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
            onPointsChange = { })
    }
}
