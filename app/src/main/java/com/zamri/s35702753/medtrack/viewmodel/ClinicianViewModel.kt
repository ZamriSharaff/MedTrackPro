package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.zamri.s35702753.medtrack.BuildConfig
import kotlinx.coroutines.delay

data class ClinicianUiState(
    val totalPatients: Int = 0,
    val avgMedsPerPatient: Double = 0.0,
    val mostCommonSymptom: String = "",
    val avgSeverity: Float = 0f,
    val insights: List<String> = emptyList(),
    val loading: Boolean = false,
    val error: String = ""
)

class ClinicianViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClinicianUiState())
    val uiState: StateFlow<ClinicianUiState> = _uiState.asStateFlow()

    private val model = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                totalPatients = repository.getTotalPatients(),
                avgMedsPerPatient = repository.getAverageMedicationsPerPatient(),
                mostCommonSymptom = repository.getMostCommonSymptom(),
                avgSeverity = repository.getAverageSymptomSeverity()
            )
        }
    }

    fun generateInsights() {
        viewModelScope.launch {
            if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Gemini API key is not configured."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                loading = true,
                error = "",
                insights = emptyList()
            )

            val stats = _uiState.value

            val prompt = """
            You are a medical data analyst.

            Based on these aggregate statistics:
            - Total patients: ${stats.totalPatients}
            - Average medications per patient: ${stats.avgMedsPerPatient}
            - Most common symptom: ${stats.mostCommonSymptom}
            - Average symptom severity: ${stats.avgSeverity}

            Generate exactly 3 short, data-driven observations.
            Do not invent additional data that is not provided.
            Return one observation per line.
        """.trimIndent()

            try {
                val responseText = generateWithRetry(prompt)

                val insights = responseText
                    .lineSequence()
                    .map { line ->
                        line.trim()
                            .removePrefix("- ")
                            .removePrefix("* ")
                            .replaceFirst(Regex("^\\d+[.)]\\s*"), "")
                            .trim()
                    }
                    .filter { it.isNotBlank() }
                    .take(3)
                    .toList()

                if (insights.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Gemini returned no usable insights. Please try again."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    insights = insights,
                    loading = false,
                    error = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = friendlyGeminiError(e)
                )
            }
        }
    }

    private suspend fun generateWithRetry(prompt: String): String {
        var lastError: Exception? = null

        repeat(3) { attempt ->
            try {
                return model.generateContent(prompt).text?.trim().orEmpty()
            } catch (e: Exception) {
                lastError = e

                if (!isRetryableGeminiError(e) || attempt == 2) {
                    throw e
                }

                delay(1500L * (attempt + 1))
            }
        }

        throw lastError ?: IllegalStateException("Gemini request failed")
    }

    private fun isRetryableGeminiError(error: Throwable): Boolean {
        val message = error.message.orEmpty().uppercase()

        return "503" in message ||
                "UNAVAILABLE" in message ||
                "HIGH DEMAND" in message ||
                "429" in message ||
                "RESOURCE_EXHAUSTED" in message
    }

    private fun friendlyGeminiError(error: Throwable): String {
        val message = error.message.orEmpty().lowercase()

        return when {
            "503" in message ||
                    "unavailable" in message ||
                    "high demand" in message ->
                "Gemini is temporarily busy. Please try again in a moment."

            "429" in message ||
                    "resource_exhausted" in message ||
                    "quota" in message ->
                "Gemini request limit reached. Please try again later."

            "401" in message ||
                    "403" in message ||
                    "api key" in message ->
                "Gemini authentication failed. Please check the API key configuration."

            else ->
                "Unable to generate insights right now. Please try again."
        }
    }
}