package com.galtagency.pointstracker

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.galtagency.pointstracker.cards.CardDefinition
import com.galtagency.pointstracker.cards.CardDetector
import com.galtagency.pointstracker.cards.ValueType
import com.galtagency.pointstracker.ui.theme.PointsTrackerTheme
import java.text.NumberFormat
import java.util.Locale

private fun formatWithCommas(value: Int): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)

private fun parseWithCommas(text: String): Int? =
    text.replace(",", "").toIntOrNull()

private fun formatDollars(cents: Int): String {
    val dollars = cents / 100
    val remainingCents = cents % 100
    return "${formatWithCommas(dollars)}.${"%02d".format(remainingCents)}"
}

private fun parseDollars(text: String): Int? {
    val cleaned = text.replace(",", "")
    val parts = cleaned.split(".")
    if (parts.size > 2) return null

    val dollars = parts[0]
    val cents = parts.getOrNull(1)?.padEnd(2, '0')?.take(2) ?: "00"
    return "$dollars$cents".toIntOrNull()
}

@Composable
private fun cardAccentColor(card: CardDefinition): Color = when (card.id) {
    "robinhood" -> Color(0xFFC9A227)
    "chase" -> Color(0xFF117ACA)
    else -> MaterialTheme.colorScheme.primary
}

private fun cardOnAccentColor(card: CardDefinition): Color = when (card.id) {
    "robinhood" -> Color(0xFF1B1B1B)
    "chase" -> Color.White
    else -> Color.White
}

class MainActivity : ComponentActivity() {

    private val requestNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        enableEdgeToEdge()

        val detector = CardDetector(packageManager)
        val installedCards = detector.detectInstalledCards()

        setContent {
            PointsTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MultiCardScreen(
                        installedCards = installedCards,
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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@Composable
fun MultiCardScreen(
    installedCards: List<CardDefinition>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PointsTracker",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (installedCards.isEmpty()) {
            Text(
                text = "No supported card apps detected",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            for (card in installedCards) {
                CardWidget(card = card)
            }
        }
    }
}

@Composable
fun CardWidget(
    card: CardDefinition,
    modifier: Modifier = Modifier
) {
    val currentValue by PointsRepository.getCardValue(card.id).collectAsState()
    val currentThreshold by PointsRepository.getCardThreshold(card.id).collectAsState()
    val accent = cardAccentColor(card)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
    ) {
        CardWidgetContent(
            card = card,
            value = currentValue,
            threshold = currentThreshold,
            onValueChange = { PointsRepository.setCardValue(card.id, it) },
            onThresholdChange = { PointsRepository.setCardThreshold(card.id, it) },
            onResetClick = { PointsRepository.resetCard(card.id) }
        )
    }
}

@Composable
fun CardWidgetContent(
    card: CardDefinition,
    value: Int,
    threshold: Int,
    onValueChange: (Int) -> Unit,
    onThresholdChange: (Int) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDollars = card.valueType == ValueType.DOLLARS
    val accent = cardAccentColor(card)
    val onAccent = cardOnAccentColor(card)

    var valueText by remember(value, isDollars) {
        val text = if (isDollars) formatDollars(value) else formatWithCommas(value)
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }
    var thresholdText by remember(threshold, isDollars) {
        val text = if (isDollars) formatDollars(threshold) else formatWithCommas(threshold)
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }

    val currentValue = if (isDollars) {
        parseDollars(valueText.text) ?: 0
    } else {
        parseWithCommas(valueText.text) ?: 0
    }
    val currentThreshold = if (isDollars) {
        (parseDollars(thresholdText.text) ?: 1).coerceAtLeast(1)
    } else {
        (parseWithCommas(thresholdText.text) ?: 1).coerceAtLeast(1)
    }

    val progressTarget = (currentValue.toFloat() / currentThreshold.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        label = "progressAnimation"
    )

    val valueLabel = if (isDollars) {
        "$${formatDollars(currentValue)} / $${formatDollars(currentThreshold)}"
    } else {
        val pointsPlural = if (currentValue == 1) "point" else "points"
        "${formatWithCommas(currentValue)} / ${formatWithCommas(currentThreshold)} $pointsPlural"
    }
    val percentText = "${(animatedProgress * 100).toInt()}%"
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        unfocusedBorderColor = accent.copy(alpha = 0.65f),
        focusedLabelColor = accent,
        unfocusedLabelColor = accent.copy(alpha = 0.8f),
        cursorColor = accent
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(accent)
        )

        Text(
            text = card.displayName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            textAlign = TextAlign.Start
        )

        Text(
            text = valueLabel,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Start
        )

        Text(
            text = percentText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Start
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = accent,
            trackColor = accent.copy(alpha = 0.2f)
        )

        Text(
            text = "Manual adjustments",
            style = MaterialTheme.typography.labelLarge,
            color = accent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Start
        )

        OutlinedTextField(
            value = valueText,
            onValueChange = { newValue ->
                val newText = newValue.text
                val allowedChars = if (isDollars) {
                    newText.all { it.isDigit() || it == ',' || it == '.' }
                } else {
                    newText.all { it.isDigit() || it == ',' }
                }
                if (allowedChars) {
                    if (newText.isEmpty()) {
                        valueText = TextFieldValue("", TextRange(0))
                    } else if (isDollars) {
                        parseDollars(newText)?.let { parsed ->
                            valueText = newValue
                            onValueChange(parsed)
                        }
                    } else {
                        parseWithCommas(newText)?.let { parsed ->
                            val formatted = formatWithCommas(parsed)
                            valueText = TextFieldValue(formatted, TextRange(formatted.length))
                            onValueChange(parsed)
                        }
                    }
                }
            },
            label = {
                Text(if (isDollars) "Set Amount" else "Set Points")
            },
            prefix = if (isDollars) { { Text("$", color = accent) } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDollars) KeyboardType.Decimal else KeyboardType.Number
            ),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        OutlinedTextField(
            value = thresholdText,
            onValueChange = { newValue ->
                val newText = newValue.text
                val allowedChars = if (isDollars) {
                    newText.all { it.isDigit() || it == ',' || it == '.' }
                } else {
                    newText.all { it.isDigit() || it == ',' }
                }
                if (allowedChars) {
                    if (newText.isEmpty()) {
                        thresholdText = TextFieldValue("", TextRange(0))
                    } else if (isDollars) {
                        parseDollars(newText)?.let { parsed ->
                            thresholdText = newValue
                            onThresholdChange(parsed)
                        }
                    } else {
                        parseWithCommas(newText)?.let { parsed ->
                            val formatted = formatWithCommas(parsed)
                            thresholdText = TextFieldValue(formatted, TextRange(formatted.length))
                            onThresholdChange(parsed)
                        }
                    }
                }
            },
            label = { Text("Set Notification Threshold") },
            prefix = if (isDollars) { { Text("$", color = accent) } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDollars) KeyboardType.Decimal else KeyboardType.Number
            ),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Button(
            onClick = onResetClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = onAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp)
        ) {
            Text(if (isDollars) "Reset Amount" else "Reset Points")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardWidgetPreview() {
    PointsTrackerTheme {
        CardWidgetContent(
            card = CardDefinition.Robinhood,
            value = 7500,
            threshold = 10000,
            onValueChange = {},
            onThresholdChange = {},
            onResetClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CardWidgetDollarsPreview() {
    PointsTrackerTheme {
        CardWidgetContent(
            card = CardDefinition.Chase,
            value = 26050,
            threshold = 50000,
            onValueChange = {},
            onThresholdChange = {},
            onResetClick = {}
        )
    }
}
