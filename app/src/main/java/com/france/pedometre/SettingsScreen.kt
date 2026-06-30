package com.france.pedometre

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs   = remember { UserPrefs(context) }

    var goal   by remember { mutableFloatStateOf(prefs.getGoal().toFloat()) }
    var height by remember { mutableFloatStateOf(prefs.getHeight().toFloat()) }
    var weight by remember { mutableFloatStateOf(prefs.getWeight().toFloat()) }
    var stride by remember { mutableFloatStateOf(prefs.getStride().toFloat()) }
    var isMale by remember { mutableStateOf(prefs.getIsMale()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("RÉGLAGES PROFIL", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(32.dp))

        SettingSlider("Objectif de pas", goal,   2000f, 20000f, " pas")        { goal   = it; prefs.setGoal(it.toInt()) }
        SettingSlider("Votre Taille",    height, 100f,  220f,   " cm")         { height = it; prefs.setHeight(it.toInt()) }
        SettingSlider("Votre Poids",     weight, 30f,   150f,   " kg")         { weight = it; prefs.setWeight(it.toInt()) }
        SettingSlider("Longueur d'un pas (Foulée)", stride, 0f, 120f,
            if (stride == 0f) " (Auto)" else " cm")                            { stride = it; prefs.setStride(it.toInt()) }

        Text("Sexe", color = Color.Gray, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            FilterChip(
                selected = isMale,
                onClick  = { isMale = true;  prefs.setIsMale(true) },
                label    = { Text("Homme") },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            FilterChip(
                selected = !isMale,
                onClick  = { isMale = false; prefs.setIsMale(false) },
                label    = { Text("Femme") },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
        TextButton(
            onClick = { navController.navigate("privacy") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Politique de confidentialité", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
fun SettingSlider(label: String, value: Float, min: Float, max: Float, unit: String, onValueChange: (Float) -> Unit) {
    Column(Modifier.padding(bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${value.toInt()}$unit", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, fontSize = 14.sp)
        }
        Slider(
            value          = value,
            onValueChange  = onValueChange,
            valueRange     = min..max,
            colors         = SliderDefaults.colors(thumbColor = Color(0xFF00E676), activeTrackColor = Color(0xFF00E676))
        )
    }
}
