package com.zamri.s35702753.medtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import com.zamri.s35702753.medtrack.viewmodel.HomeViewModel

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackPro_Assignment3Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { HomeTopBar() },
                    bottomBar = { HomeBottomBar() }
                ) { innerPadding ->
                    HomeDisplay(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication
    val factory = remember(app) {
        MedTrackViewModelFactory(app.repository, app.sessionManager)
    }
    val vm: HomeViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFB2EBF2)
    ) {
        if (state.patientId.isBlank()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No active session. Please log in again.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black
                )
            }
        } else {
            val medicationsList = state.medications
            val takenMap = state.takenMap
            val vm: HomeViewModel = viewModel(factory = factory)

            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Hello, ${state.patientName}!",
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineLarge,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Patient ID: ${state.patientId}",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, top = 160.dp, bottom = 160.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (medicationsList.isEmpty()) {
                        item {
                            Text(
                                text = "No medications scheduled.",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else {
                        itemsIndexed(medicationsList) { index, med ->
                            val isTaken = takenMap[med.medicationId] ?: false

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isTaken) Color(0xFFE5E5E5) else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = med.medicationName,
                                        fontSize = 18.sp,
                                        color = Color.Black,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Text(
                                        text = "Dosage: ${med.dosage}",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Frequency: ${med.frequency}",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "Scheduled Time: ${med.scheduledTime}",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = if (med.notes.isBlank()) {
                                            "Notes: None"
                                        } else {
                                            "Notes: ${med.notes}"
                                        },
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Switch(
                                        checked = isTaken,
                                        onCheckedChange = { checked ->
                                            vm.toggleTaken(med.medicationId, checked)
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val takenCount = takenMap.values.count { it }
                    Text(
                        text = "$takenCount of ${medicationsList.size} medications taken today",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(onClick = {
                        val intent = Intent(context, AddMedicationScreen::class.java)
                        context.startActivity(intent)
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Medication")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "HOME",
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
fun HomeBottomBar() {
    val context = LocalContext.current

    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = { Toast.makeText(context, "You are already on Home", Toast.LENGTH_SHORT).show() },
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