package com.galtagency.pointstracker

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
    return when (parts.size) {
        1 -> parts[0].toIntOrNull()?.let { it * 100 }
        2 -> {
            val dollars = parts[0].toIntOrNull() ?: return null
            val centsStr = parts[1].padEnd(2, '0').take(2)
            val cents = centsStr.toIntOrNull() ?: return null
            dollars * 100 + cents
        }
        else -> null
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            color = MaterialTheme.colorScheme.primary,
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

    Card(
        modifier = modifier.fillMaxWidth()
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

    var valueText by remember(if (isDollars) Unit else value) {
        val text = if (isDollars) formatDollars(value) else formatWithCommas(value)
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }
    var thresholdText by remember(if (isDollars) Unit else threshold) {
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

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = card.displayName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
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
            text = valueLabel,
            style = MaterialTheme.typography.bodyLarge
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
            prefix = if (isDollars) {{ Text("$") }} else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDollars) KeyboardType.Decimal else KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
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
            prefix = if (isDollars) {{ Text("$") }} else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isDollars) KeyboardType.Decimal else KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onResetClick,
            modifier = Modifier.fillMaxWidth()
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
