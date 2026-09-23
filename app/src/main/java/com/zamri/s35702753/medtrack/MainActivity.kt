package com.zamri.s35702753.medtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zamri.s35702753.medtrack.ui.theme.MedTrackPro_Assignment3Theme
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.zamri.s35702753.medtrack.data.DatabaseSeeder
import com.zamri.s35702753.medtrack.data.MedTrackDatabase
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MedTrackApplication

        lifecycleScope.launch {
            DatabaseSeeder.seedDatabase(applicationContext, app.repository)

            val loggedInId = app.sessionManager.getLoggedInPatientId()

            if (!loggedInId.isNullOrBlank()) {
                startActivity(
                    Intent(this@MainActivity, HomeScreen::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                finish()
                return@launch
            }

            setContent {
                MedTrackPro_Assignment3Theme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        WelcomeScreen(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFB3E5FC)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "MedTrack",
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontSize = 40.sp,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            // I used gen AI to generate a logo image for Medtrack
            // https://www.design.com/share/a33489b8-5f1b-4b01-a6f9-035a81424409
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.medtrack_logo),
                contentDescription = "MedTrack Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(200.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, LoginScreen::class.java))
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp)
                ) {
                    Text(
                        text = "LOGIN",
                        fontSize = 20.sp,
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, SignUpScreen::class.java))
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .width(150.dp)
                ) {
                    Text(
                        text = "SIGN UP",
                        fontSize = 20.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            WebsiteLink(
                link = "https://monashhealth.org",
                displayText = "Monash Health Clinic website"

            )
            Text(
                text = "Student Name: Zamri Sharaff",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            )

            Text(
                text = "Student ID: 35702753",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            )

            Text(
                text = "This app is for tracking purposes only and does not replace professional medical advice.",
                modifier = Modifier
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun WebsiteLink(link: String, displayText: String) {
    val context = LocalContext.current
    Text(
        text = displayText,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, link.toUri())
                context.startActivity(intent)
            }
    )
}
