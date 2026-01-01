package com.galtagency.pointstracker

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.galtagency.pointstracker.ui.theme.PointsTrackerTheme
import com.galtagency.pointstrackerimport.PointsRepository

class MainActivity : ComponentActivity() {
    // No longer need a SharedPreferences instance here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        enableEdgeToEdge()
        setContent {
            PointsTrackerTheme {
                // Collect the points value as a state
                val currentPoints by PointsRepository.points.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        points = currentPoints, // Pass the state value to the composable
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val componentName = ComponentName(this, PointsTrackerService::class.java)
        return enabledListeners?.split(":")?.map {
            ComponentName.unflattenFromString(it)
        }?.any { it == componentName } ?: false
    }
}

@Composable
fun Greeting(points: Int, modifier: Modifier = Modifier) {
    // The text now correctly displays "point" or "points"
    val pointsText = if (points == 1) "point" else "points"
    Text(
        text = "Accumulated $points $pointsText since last notification.",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PointsTrackerTheme {
        Greeting(points = 150)
    }
}
