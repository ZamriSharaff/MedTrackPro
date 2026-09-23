package com.zamri.s35702753.medtrack

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zamri.s35702753.medtrack.ui.theme.MedTrackPro_Assignment3Theme
import com.zamri.s35702753.medtrack.viewmodel.SymptomTrendPoint
import com.zamri.s35702753.medtrack.viewmodel.SymptomsEvent
import com.zamri.s35702753.medtrack.viewmodel.SymptomsViewModel
import java.util.Calendar

class SymptomsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackPro_Assignment3Theme {
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { SymptomsTopBar() },
                    bottomBar = { BottomBarSymptoms() },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    SymptomsDisplay(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun SymptomsDisplay(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication
    val factory = remember(app) {
        MedTrackViewModelFactory(app.repository, app.sessionManager)
    }
    val vm: SymptomsViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is SymptomsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    val datePickerDialog = remember(state.date) {
        datePickerSymptom(context) { selectedDate ->
            vm.onDateChange(selectedDate)
        }
    }
    val timePickerDialog = remember(state.time) {
        timePickerSymptom(context) { selectedTime ->
            vm.onTimeChange(selectedTime)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFCFECF3)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "LOG SYMPTOM",
                    textAlign = TextAlign.Left,
                    fontFamily = FontFamily.Serif,
                    fontSize = 25.sp,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(2.dp)
                )
            }

            item {
                DropdownOutlinedFieldNew(
                    label = "Symptom Category",
                    options = listOf(
                        "Pain",
                        "Nausea",
                        "Dizziness",
                        "Fatigue",
                        "Headache",
                        "Skin Reaction",
                        "Other"
                    ),
                    selectedValue = state.symptomCategory,
                    onSelectedChange = vm::onCategoryChange
                )
            }

            item {
                val severityColor = when (state.severity.toInt()) {
                    in 1..3 -> Color(0xFF4CAF50)
                    in 4..6 -> Color(0xFFFF9800)
                    in 7..10 -> Color.Red
                    else -> Color.DarkGray
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Severity Rating (1-10) ",
                        color = severityColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text("*", color = Color.Red)
                    Text(": ${state.severity.toInt()}")
                }

                Slider(
                    value = state.severity,
                    onValueChange = vm::onSeverityChange,
                    valueRange = 1f..10f,
                    colors = SliderDefaults.colors(
                        thumbColor = severityColor,
                        activeTrackColor = severityColor,
                        inactiveTrackColor = Color.LightGray
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = state.additionalNotes,
                    onValueChange = vm::onNotesChange,
                    label = { Text("Notes (optional)", color = Color.Black) },
                    supportingText = {
                        Text("${state.additionalNotes.length} / 200")
                    },
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(),
                )
            }

            item {
                Button(onClick = { datePickerDialog.show() }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pick date of symptom occurrence ")
                        Text("*", color = Color.Red)
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (state.date.isBlank()) "No date selected"
                        else "Selected date: ${state.date}",
                        color = Color.Black
                    )
                }
            }

            item {
                Button(onClick = { timePickerDialog.show() }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pick time of symptom occurrence ")
                        Text("*", color = Color.Red)
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(5.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (state.time.isBlank()) "No time selected"
                        else "Selected time: ${state.time}",
                        color = Color.Black
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.error.isNotBlank()) {
                        Text(
                            text = state.error,
                            color = Color.Red,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Button(
                        onClick = { vm.onSaveClick() },
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = "Save Symptom")
                    }

                    Button(
                        onClick = { vm.onClearClick() },
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = "Clear")
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Gray
                )
            }

            item {
                SymptomTrendChart(trend = state.trendPoints)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "SYMPTOM HISTORY",
                    fontFamily = FontFamily.Serif,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (state.symptoms.isEmpty()) {
                item {
                    Text("No symptoms logged yet.")
                }
            }

            items(state.symptoms) { symptom ->
                val (severityLabel, severityColorItem) = getSeverityIndicator(symptom.severity)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Category: ${symptom.category}")
                        Text(
                            "Severity: $severityLabel (${symptom.severity})",
                            color = severityColorItem,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Date/Time: ${symptom.dateTime}")
                        Text("Notes: ${symptom.notes}")
                    }
                }
            }
        }
    }
}

@Composable
fun SymptomTrendChart(trend: List<SymptomTrendPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Symptom Trend (Last 7 Days)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (trend.isEmpty()) {
                Text("No trend data yet.")
                return@Column
            }

            val maxCount = trend.maxOfOrNull { it.count } ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                trend.forEach { point ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = point.count.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .height(
                                    (((point.count.toFloat() / maxCount.toFloat()) * 120f)
                                        .coerceAtLeast(4f)).dp
                                )
                                .width(18.dp)
                                .background(
                                    color = Color(0xFF6FA8DC),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = point.dateLabel,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Avg ${String.format("%.1f", point.averageSeverity)}",
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownOutlinedFieldNew(
    label: String,
    options: List<String>,
    selectedValue: String,
    onSelectedChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = {
                Row {
                    Text(label)
                    Text("*", color = Color.Red)
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun timePickerSymptom(
    context: Context,
    onSelected: (String) -> Unit
): TimePickerDialog {
    val calendar = Calendar.getInstance()
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val minute = calendar[Calendar.MINUTE]

    return TimePickerDialog(
        context,
        { _, pickedHour, pickedMinute ->
            onSelected(String.format("%02d:%02d", pickedHour, pickedMinute))
        },
        hour,
        minute,
        false
    )
}

fun datePickerSymptom(
    context: Context,
    onSelected: (String) -> Unit
): DatePickerDialog {
    val calendar = Calendar.getInstance()
    val year = calendar[Calendar.YEAR]
    val month = calendar[Calendar.MONTH]
    val day = calendar[Calendar.DAY_OF_MONTH]

    return DatePickerDialog(
        context,
        { _, pickedYear, pickedMonth, pickedDay ->
            onSelected(String.format("%04d-%02d-%02d", pickedYear, pickedMonth + 1, pickedDay))
        },
        year,
        month,
        day
    )
}

fun getSeverityIndicator(severity: Int): Pair<String, Color> {
    return when (severity) {
        in 1..3 -> "Mild" to Color(0xFF4CAF50)
        in 4..6 -> "Moderate" to Color(0xFFFF9800)
        in 7..10 -> "Severe" to Color.Red
        else -> "Unknown" to Color.DarkGray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsTopBar() {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "SYMPTOMS",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 30.sp
            )
        },
        actions = {
            Button(
                onClick = {
                    app.sessionManager.clearSession()
                    val intent = Intent(context, LoginScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }
            ) {
                Text("LOGOUT")
            }
        }
    )
}

@Composable
fun BottomBarSymptoms() {
    val context = LocalContext.current

    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { context.startActivity(Intent(context, HomeScreen::class.java)) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = true,
            onClick = {
                Toast.makeText(context, "You are already on Symptoms", Toast.LENGTH_SHORT).show()
            },
            icon = { Icon(Icons.Filled.Warning, contentDescription = "Symptoms") },
            label = { Text("Symptoms") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { context.startActivity(Intent(context, MedCoachScreen::class.java)) },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "MedCoach") },
            label = { Text("MedCoach") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { context.startActivity(Intent(context, SettingsScreen::class.java)) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}