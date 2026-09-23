package com.zamri.s35702753.medtrack

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zamri.s35702753.medtrack.ui.theme.MedTrackPro_Assignment3Theme
import com.zamri.s35702753.medtrack.viewmodel.AddMedicationEvent
import com.zamri.s35702753.medtrack.viewmodel.AddMedicationUiState
import com.zamri.s35702753.medtrack.viewmodel.AddMedicationViewModel
import java.util.Calendar

class AddMedicationScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackPro_Assignment3Theme {
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { AddMedTopBar() },
                    bottomBar = { AddMedicationBottomBar() },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    AddMedicationDisplay(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun AddMedicationDisplay(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication
    val factory = remember(app) {
        MedTrackViewModelFactory(app.repository, app.sessionManager)
    }
    val vm: AddMedicationViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is AddMedicationEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                AddMedicationEvent.NavigateHome -> {
                    context.startActivity(
                        Intent(context, HomeScreen::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    (context as? ComponentActivity)?.finish()
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFA2D3E0)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = state.medicationName,
                    onValueChange = vm::onMedicationNameChange,
                    label = {
                        Row {
                            Text("Medication Name ", color = Color.Black)
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.dosage,
                    onValueChange = vm::onDosageChange,
                    label = {
                        Row {
                            Text("Dosage ", color = Color.Black)
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                DropdownOutlinedField(
                    label = "Frequency",
                    options = listOf("Once daily", "Twice daily", "Three times daily", "As needed"),
                    selectedValue = state.frequency,
                    onSelectedChange = vm::onFrequencyChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showTimePicker(context) { picked ->
                            vm.onTimeChange(picked)
                        }
                    }
                ) {
                    Text(text = "Open Time Picker")
                }

                Text(
                    text = if (state.time.isBlank()) "Selected time: none" else "Selected time: ${state.time}",
                    color = Color.Black
                )

                DropdownOutlinedField(
                    label = "Medication Type",
                    options = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other"),
                    selectedValue = state.medicationType,
                    onSelectedChange = vm::onMedicationTypeChange
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = vm::onNotesChange,
                    label = { Text("Notes (optional)", color = Color.Black) },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.error.isNotBlank()) {
                    Text(
                        text = state.error,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Button(
                    onClick = { vm.onSaveClick() },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(text = "Save")
                }

                Button(
                    onClick = { vm.onClearClick() },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(text = "Clear")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownOutlinedField(
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

private fun showTimePicker(
    context: Context,
    onSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, pickedHour, pickedMinute ->
            onSelected(String.format("%02d:%02d", pickedHour, pickedMinute))
        },
        hour,
        minute,
        false
    ).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedTopBar() {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "ADD MEDICATION",
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
fun AddMedicationBottomBar() {
    val context = LocalContext.current

    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { context.startActivity(Intent(context, HomeScreen::class.java)) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { context.startActivity(Intent(context, SymptomsScreen::class.java)) },
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