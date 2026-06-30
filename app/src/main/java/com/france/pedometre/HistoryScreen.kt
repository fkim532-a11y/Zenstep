package com.france.pedometre

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val prefs = remember { UserPrefs(context) }
    val goal = prefs.getGoal()
    val height = prefs.getHeight()
    val isMale = prefs.getIsMale()
    val customStride = prefs.getStride()

    val strideMultiplier = if (isMale) 0.415 else 0.413
    val stepLengthKm = if (customStride > 0) customStride.toDouble() / 100000.0
                       else (height * strideMultiplier) / 100000.0

    val weight = prefs.getWeight()

    val historyList by db.stepDao().getAllSteps().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Calendrier", "Semaines", "Mois")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(Icons.Default.DateRange, null, tint = Color(0xFF00E676), modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("HISTORIQUE", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF00E676),
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF00E676)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (selectedTab) {
            0 -> CalendarView(historyList, goal, stepLengthKm, weight)
            else -> {
                val displayList = when (selectedTab) {
                    1 -> groupStepsByWeek(historyList)
                    2 -> groupStepsByMonth(historyList)
                    else -> emptyList()
                }
                if (displayList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucune donnée", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(displayList) { item ->
                            HistoryStatsCard(item.label, item.totalSteps, item.totalSteps * stepLengthKm, item.totalSteps * 0.0005 * weight * 1.1, goal, isPeriod = true)
                        }
                    }
                }
            }
        }
    }
}

// ─── Vue Calendrier ──────────────────────────────────────────────────────────

@Composable
fun CalendarView(historyList: List<StepData>, goal: Int, stepLengthKm: Double, weight: Int) {
    val today = remember { Calendar.getInstance() }
    var displayYear  by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var selectedDay  by remember { mutableStateOf<Triple<String, Int, Int>?>(null) }

    val stepMap = remember(historyList) { historyList.associateBy { it.date } }
    val sdf     = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val firstOfMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR,         displayYear)
        set(Calendar.MONTH,        displayMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth  = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Décalage 0=Lun … 6=Dim (calendrier français)
    val startOffset  = (firstOfMonth.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
    val monthLabel   = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        .format(firstOfMonth.time)
        .replaceFirstChar { it.uppercase() }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                if (displayMonth == 0) { displayMonth = 11; displayYear-- }
                else displayMonth--
            }) { Text("‹", color = Color(0xFF00E676), fontSize = 28.sp, fontWeight = FontWeight.Light) }

            Text(monthLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)

            TextButton(onClick = {
                if (displayMonth == 11) { displayMonth = 0; displayYear++ }
                else displayMonth++
            }) { Text("›", color = Color(0xFF00E676), fontSize = 28.sp, fontWeight = FontWeight.Light) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { d ->
                Text(
                    d,
                    modifier    = Modifier.weight(1f),
                    textAlign   = TextAlign.Center,
                    color       = Color(0xFF00E676).copy(alpha = 0.7f),
                    fontSize    = 10.sp,
                    fontWeight  = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.07f), thickness = 1.dp)
        Spacer(Modifier.height(6.dp))

        val totalRows = ((startOffset + daysInMonth) + 6) / 7
        for (row in 0 until totalRows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = row * 7 + col - startOffset + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val dayCalendar = Calendar.getInstance().apply {
                            set(displayYear, displayMonth, day)
                        }
                        val dateStr = sdf.format(dayCalendar.time)
                        val steps   = stepMap[dateStr]?.steps ?: -1
                        val isToday = displayYear  == today.get(Calendar.YEAR) &&
                                      displayMonth == today.get(Calendar.MONTH) &&
                                      day          == today.get(Calendar.DAY_OF_MONTH)
                        DayCell(day, steps, goal, isToday, Modifier.weight(1f)) {
                            if (steps > 0) selectedDay = Triple(dateStr, day, steps)
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        selectedDay?.let { (dateStr, _, steps) ->
            DayDetailDialog(
                dateStr      = dateStr,
                steps        = steps,
                stepLengthKm = stepLengthKm,
                weight       = weight,
                onDismiss    = { selectedDay = null }
            )
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.07f), thickness = 1.dp)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CalendarLegendItem(Color(0xFF00E676),                   "Objectif atteint")
            CalendarLegendItem(Color(0xFF00E676).copy(alpha = 0.4f),"En cours")
            CalendarLegendItem(Color(0xFF2A2A2A),                   "Aucune donnée")
        }
    }
}

@Composable
fun DayCell(day: Int, steps: Int, goal: Int, isToday: Boolean, modifier: Modifier, onClick: () -> Unit = {}) {
    val bgColor = when {
        steps <= 0    -> Color(0xFF2A2A2A)
        steps >= goal -> Color(0xFF00E676)
        else          -> Color(0xFF00E676).copy(alpha = 0.15f + (steps.toFloat() / goal) * 0.65f)
    }
    val textColor = if (steps >= goal && steps > 0) Color.Black else Color.White
    val shape     = RoundedCornerShape(7.dp)

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.75f)
            .background(bgColor, shape)
            .then(if (isToday) Modifier.border(1.5.dp, Color.White, shape) else Modifier)
            .then(if (steps > 0) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "$day",
                color      = textColor,
                fontSize   = 12.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                lineHeight = 14.sp
            )
            if (steps > 0) {
                Text(
                    formatStepsShort(steps),
                    color      = textColor.copy(alpha = 0.85f),
                    fontSize   = 9.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 11.sp
                )
            }
        }
    }
}

@Composable
fun DayDetailDialog(dateStr: String, steps: Int, stepLengthKm: Double, weight: Int, onDismiss: () -> Unit) {
    val distance = steps * stepLengthKm
    val calories = steps * 0.0005 * weight * 1.1
    val dateLabel = remember(dateStr) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
            SimpleDateFormat("EEEE d MMMM yyyy", Locale.getDefault())
                .format(parsed!!)
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) { dateStr }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    dateLabel,
                    color = Color(0xFF00E676),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DayDetailStat(Modifier.weight(1f), "PAS", "$steps", "")
                    DayDetailStat(Modifier.weight(1f), "DISTANCE", String.format(Locale.getDefault(), "%.2f", distance), "km")
                    DayDetailStat(Modifier.weight(1f), "CALORIES", "${calories.toInt()}", "kcal")
                }
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = onDismiss) {
                    Text("FERMER", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DayDetailStat(modifier: Modifier, label: String, value: String, unit: String) {
    Column(
        modifier = modifier
            .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        if (unit.isNotEmpty())
            Text(unit, fontSize = 11.sp, color = Color(0xFF00E676))
    }
}

fun formatStepsShort(steps: Int): String = when {
    steps >= 10_000 -> "${steps / 1000}k"
    steps >= 1_000  -> "${steps / 1000}.${(steps % 1000) / 100}k"
    else            -> "$steps"
}

@Composable
fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

// ─── Historique semaines / mois ──────────────────────────────────────────────

@Composable
fun HistoryStatsCard(label: String, steps: Int, distanceKm: Double, calories: Double, goal: Int, isPeriod: Boolean) {
    val factor   = if (isPeriod) 7 else 1
    val progress = (steps.toFloat() / (goal * factor)).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = Color(0xFF00E676), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(String.format(Locale.getDefault(), "%.2f", distanceKm), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Kilomètres", color = Color.Gray, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${calories.toInt()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Calories", color = Color.Gray, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$steps", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Pas total", color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress  = { progress },
                modifier  = Modifier.fillMaxWidth().height(6.dp),
                color     = Color(0xFF00E676),
                trackColor = Color.White.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

data class HistoryItemData(val label: String, val totalSteps: Int)

fun groupStepsByWeek(history: List<StepData>): List<HistoryItemData> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    return history.groupBy {
        val date = sdf.parse(it.date) ?: Date()
        cal.time = date
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.WEEK_OF_YEAR).toString().padStart(2, '0')}"
    }.entries.sortedByDescending { it.key }
        .map { (key, items) ->
            val (year, week) = key.split("-")
            HistoryItemData("Semaine ${week.toInt()} - $year", items.sumOf { d -> d.steps })
        }
}

fun groupStepsByMonth(history: List<StepData>): List<HistoryItemData> {
    val sdf      = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val keySdf   = SimpleDateFormat("yyyy-MM",    Locale.getDefault())
    val monthSdf = SimpleDateFormat("MMMM yyyy",  Locale.getDefault())
    return history.groupBy {
        keySdf.format(sdf.parse(it.date) ?: Date())
    }.entries.sortedByDescending { it.key }
        .map { (key, items) ->
            val date = sdf.parse("$key-01") ?: Date()
            HistoryItemData(
                monthSdf.format(date).replaceFirstChar { it.uppercase() },
                items.sumOf { d -> d.steps }
            )
        }
}

fun formatDisplayDate(dateString: String): String {
    return try {
        val parser    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        formatter.format(parser.parse(dateString) ?: return dateString)
    } catch (_: Exception) { dateString }
}
