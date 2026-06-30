package com.france.pedometre

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun ActivityScreen(steps: Int) {
    val context = LocalContext.current
    val prefs = remember { UserPrefs(context) }
    val sensorAvailable by StepRepository.sensorAvailable.collectAsState()

    val goal = prefs.getGoal()
    val height = prefs.getHeight()
    val weight = prefs.getWeight()
    val isMale = prefs.getIsMale()
    val customStride = prefs.getStride()

    val stepLengthKm = if (customStride > 0) {
        customStride.toDouble() / 100000.0
    } else {
        val strideMultiplier = if (isMale) 0.415 else 0.413
        (height * strideMultiplier) / 100000.0
    }

    val distance = steps * stepLengthKm
    val calories = steps * 0.0005 * weight * 1.1
    val totalMinutes = steps / 100
    val hoursActive = totalMinutes / 60
    val minsActive = totalMinutes % 60
    val timeDisplay = "${hoursActive}h${minsActive.toString().padStart(2, '0')}"
    val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)

    if (!sensorAvailable) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚠", fontSize = 48.sp, color = Color(0xFFFFB300))
                Spacer(Modifier.height(16.dp))
                Text(
                    "Capteur de pas non disponible",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cet appareil ne possède pas de capteur de podomètre matériel.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("ÉTAT D'ACTIVITÉ", color = Color.Gray, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp)) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = 0.05f),
                strokeWidth = 16.dp,
                strokeCap = StrokeCap.Round
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF00E676),
                strokeWidth = 16.dp,
                strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$steps", fontSize = 68.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("OBJECTIF: $goal", fontSize = 14.sp, color = Color(0xFF00E676).copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCard(Modifier.weight(1f), "DISTANCE", String.format(Locale.getDefault(), "%.2f", distance), "Km")
            InfoCard(Modifier.weight(1f), "CALORIES", "${calories.toInt()}", "Kcal")
            InfoCard(Modifier.weight(1f), "TEMPS", timeDisplay, "")
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, label: String, value: String, unit: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(unit, fontSize = 12.sp, color = Color(0xFF00E676))
        }
    }
}
