package com.zamri.s35702753.medtrack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zamri.s35702753.medtrack.ui.theme.MedTrackPro_Assignment3Theme
import com.zamri.s35702753.medtrack.viewmodel.LoginEvent
import com.zamri.s35702753.medtrack.viewmodel.LoginViewModel

class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackPro_Assignment3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginDisplay(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication
    val factory = remember(app) {
        MedTrackViewModelFactory(app.repository, app.sessionManager)
    }
    val vm: LoginViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    val patientIdFromSignup = context as? ComponentActivity
    val launchIntent = patientIdFromSignup?.intent
    val prefillId = launchIntent?.getStringExtra("prefill_patient_id")
    val startInClaimMode = launchIntent?.getBooleanExtra("start_in_claim_mode", true) ?: true

    LaunchedEffect(Unit) {
        vm.loadLoginMode(prefillId, startInClaimMode)
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                LoginEvent.NavigateHome -> {
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
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFB2EBF2)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            Text(
                text = if (state.isClaimMode) "CLAIM ACCOUNT" else "LOGIN PAGE",
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontSize = 40.sp,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = state.patientId,
                    onValueChange = vm::onPatientIdChange,
                    label = {
                        Row {
                            Text("Patient ID ")
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                if (state.isClaimMode) {
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = vm::onPhoneNumberChange,
                        label = {
                            Row {
                                Text("Phone Number ")
                                Text("*", color = Color.Red)
                            }
                        },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = {
                        Row {
                            Text(if (state.isClaimMode) "New Password " else "Password ")
                            Text("*", color = Color.Red)
                        }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                if (state.isClaimMode) {
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = vm::onConfirmPasswordChange,
                        label = {
                            Row {
                                Text("Confirm Password ")
                                Text("*", color = Color.Red)
                            }
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                }

                if (state.error.isNotBlank()) {
                    Text(
                        text = state.error,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Button(
                    onClick = { vm.onPrimaryActionClick() },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(if (state.isClaimMode) "CLAIM ACCOUNT" else "LOGIN")
                }

                Button(
                    onClick = { vm.setMode(!state.isClaimMode) },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(if (state.isClaimMode) "Switch to Password Login" else "Switch to Claim Account")
                }

                if (prefillId != null && !state.isClaimMode) {
                    Text(
                        text = "Your Patient ID: $prefillId",
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text("Back")
                }
            }
        }
    }
}