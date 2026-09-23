package com.zamri.s35702753.medtrack

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zamri.s35702753.medtrack.ui.theme.MedTrackPro_Assignment3Theme
import com.zamri.s35702753.medtrack.viewmodel.SignUpEvent
import com.zamri.s35702753.medtrack.viewmodel.SignUpViewModel

class SignUpScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackPro_Assignment3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignUpDisplay(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SignUpDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as MedTrackApplication
    val factory = remember(app) {
        MedTrackViewModelFactory(app.repository, app.sessionManager)
    }
    val vm: SignUpViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is SignUpEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is SignUpEvent.NavigateToLogin -> {
                    val intent = Intent(context, LoginScreen::class.java).apply {
                        putExtra("prefill_patient_id", event.patientId)
                        putExtra("start_in_claim_mode", false)
                    }
                    context.startActivity(intent)
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
                text = "SIGN UP PAGE",
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
                    value = state.fullName,
                    onValueChange = vm::onFullNameChange,
                    label = {
                        Row {
                            Text("Full Name ")
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

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

                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::onPasswordChange,
                    label = {
                        Row {
                            Text("Password ")
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = vm::onConfirmPasswordChange,
                    label = {
                        Row {
                            Text("Confirm Password ")
                            Text("*", color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )

                if (state.error.isNotBlank()) {
                    Text(
                        text = state.error,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Button(
                    onClick = { vm.onSignUpClick() },
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text("SIGN UP")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, LoginScreen::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                ) {
                    Text("Back to Login")
                }
            }
        }
    }
}