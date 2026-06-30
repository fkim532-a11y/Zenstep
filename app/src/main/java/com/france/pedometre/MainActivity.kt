package com.france.pedometre

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            var permissionsGranted by remember { mutableStateOf(checkPermissions(context)) }
            val prefs = remember { context.getSharedPreferences("user_settings", Context.MODE_PRIVATE) }
            var showMiuiDialog by remember {
                mutableStateOf(
                    Build.MANUFACTURER.lowercase() == "xiaomi" &&
                    !prefs.getBoolean("miui_dialog_shown", false)
                )
            }

            val permLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                permissionsGranted = results.values.all { it }
                if (permissionsGranted) {
                    startForegroundService(Intent(context, StepCounterService::class.java))
                }
            }

            LaunchedEffect(Unit) {
                val needed = requiredPermissions()
                if (needed.isEmpty() || checkPermissions(context)) {
                    permissionsGranted = true
                    startForegroundService(Intent(context, StepCounterService::class.java))
                } else {
                    permLauncher.launch(needed)
                }
                val pm = context.getSystemService(POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    })
                }
            }

            val stepCount by StepRepository.currentSteps.collectAsState()
            val navController = rememberNavController()

            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF00E676),
                background = Color(0xFF121212),
                surface = Color(0xFF1E1E1E)
            )) {
                if (showMiuiDialog && permissionsGranted) {
                    AlertDialog(
                        onDismissRequest = {},
                        containerColor = Color(0xFF1E1E1E),
                        title = {
                            Text("Xiaomi détecté", color = Color.White, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Text(
                                "Pour que le podomètre fonctionne quand l'app est fermée, activez ces 2 réglages :\n\n" +
                                "Paramètres › Applications › Gérer les apps › Mon Podomètre\n\n" +
                                "• Économiseur de batterie → Aucune restriction\n" +
                                "• Démarrage automatique → Activer",
                                color = Color.Gray
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                prefs.edit().putBoolean("miui_dialog_shown", true).apply()
                                showMiuiDialog = false
                            }) {
                                Text("Compris", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                if (!permissionsGranted) {
                    PermissionScreen { permLauncher.launch(requiredPermissions()) }
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(containerColor = Color(0xFF1A1A1A)) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route

                                NavigationBarItem(
                                    icon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null) },
                                    label = { Text("Activité") },
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, null) },
                                    label = { Text("Historique") },
                                    selected = currentRoute == "history",
                                    onClick = { navController.navigate("history") }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, null) },
                                    label = { Text("Réglages") },
                                    selected = currentRoute == "settings",
                                    onClick = { navController.navigate("settings") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
                            composable("home")     { ActivityScreen(stepCount) }
                            composable("history")  { HistoryScreen() }
                            composable("settings") { SettingsScreen(navController) }
                            composable("privacy")  { PrivacyPolicyScreen { navController.popBackStack() } }
                        }
                    }
                }
            }
        }
    }
}

fun requiredPermissions(): Array<String> = buildList {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        add(Manifest.permission.ACTIVITY_RECOGNITION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

fun checkPermissions(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED)
        return false
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        return false
    return true
}

@Composable
fun PermissionScreen(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, null, tint = Color(0xFF00E676), modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text("Permissions requises", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Text(
            "Le podomètre a besoin de :\n• Reconnaître votre activité physique\n• Envoyer des notifications",
            color = Color.Gray, fontSize = 15.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
        ) {
            Text("Accorder les permissions", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
