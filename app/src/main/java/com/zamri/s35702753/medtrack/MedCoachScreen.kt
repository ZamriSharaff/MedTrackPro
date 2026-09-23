package com.zamri.s35702753.medtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zamri.s35702753.medtrack.ui.theme.MedTrackPro_Assignment3Theme
import com.zamri.s35702753.medtrack.viewmodel.MedCoachViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedCoachScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackPro_Assignment3Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { MedCoachTopBar() },
                    bottomBar = { MedCoachBottomBar() }
                ) { innerPadding ->
                    MedCoachDisplay(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MedCoachDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication
    val factory = remember(app) {
        MedTrackViewModelFactory(app.repository, app.sessionManager)
    }
    val vm: MedCoachViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFCFECF3)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "DRUG INFORMATION",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            MedicationAutocompleteField(
                query = state.query,
                suggestions = state.savedMedicationNames,
                onQueryChange = vm::onQueryChange,
                onSuggestionSelected = vm::onSuggestionSelected
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { vm.onSearchClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Medication Info")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoadingDrug) {
                Text("Loading...", color = Color.Black)
            }

            if (state.drugError.isNotBlank()) {
                Text(
                    text = state.drugError,
                    color = Color.Red
                )
            }

            if (state.brandName.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        item {
                            Text("Medication: ${state.brandName}", fontSize = 18.sp)
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Text("Purpose: ${state.purpose}", fontSize = 14.sp)
                        }
                        item {
                            Text("Warnings: ${state.warnings}", fontSize = 14.sp)
                        }
                        item {
                            Text(
                                "Dosage / Administration: ${state.dosageAndAdministration}",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                thickness = 1.dp,
                color = Color.Gray
            )

            Text(
                text = "GENAI TIPS",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { vm.onGenerateTipClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isGeneratingTip
            ) {
                Text("Generate Tip")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isGeneratingTip) {
                Text("Generating tip...", color = Color.Black)
            }

            if (state.tipError.isNotBlank()) {
                Text(
                    text = state.tipError,
                    color = Color.Red
                )
            }

            if (state.generatedTip.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Latest Tip",
                                fontSize = 16.sp,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            Text(
                                text = state.generatedTip,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = { vm.onShowTipsClick() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.tipsHistory.isNotEmpty()
            ) {
                Text("Show All Tips")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.showTipsDialog) {
        TipsHistoryDialog(
            tips = state.tipsHistory,
            onDismiss = vm::onDismissTipsDialog
        )
    }
}

@Composable
fun TipsHistoryDialog(
    tips: List<com.zamri.s35702753.medtrack.data.MedCoachTipEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "All Previous Tips",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (tips.isEmpty()) {
                    Text("No tips generated yet.", color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(tips) { tip ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = tip.tipText,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = formatTipTime(tip.createdAt),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

private fun formatTipTime(createdAt: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(createdAt))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationAutocompleteField(
    query: String,
    suggestions: List<String>,
    onQueryChange: (String) -> Unit,
    onSuggestionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredSuggestions = suggestions
        .filter { it.contains(query, ignoreCase = true) }
        .take(8)

    ExposedDropdownMenuBox(
        expanded = expanded && filteredSuggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = false
            },
            label = { Text("Medication Name") },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredSuggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredSuggestions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSuggestionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedCoachTopBar() {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = "MEDCOACH",
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
fun MedCoachBottomBar() {
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
            selected = true,
            onClick = { Toast.makeText(context, "You are already on MedCoach", Toast.LENGTH_SHORT).show() },
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