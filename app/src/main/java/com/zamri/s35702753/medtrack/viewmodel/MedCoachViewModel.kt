package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zamri.s35702753.medtrack.data.MedCoachTipEntity
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.SessionManager
import com.zamri.s35702753.medtrack.data.SymptomEntity
import com.zamri.s35702753.medtrack.data.network.DrugLookupResult
import com.zamri.s35702753.medtrack.data.network.MedInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.zamri.s35702753.medtrack.BuildConfig
import kotlinx.coroutines.delay

data class MedCoachUiState(
    val patientName: String = "",
    val query: String = "",
    val savedMedicationNames: List<String> = emptyList(),
    val symptomsHistory: List<SymptomEntity> = emptyList(),
    val brandName: String = "",
    val purpose: String = "",
    val warnings: String = "",
    val dosageAndAdministration: String = "",
    val drugError: String = "",
    val isLoadingDrug: Boolean = false,
    val generatedTip: String = "",
    val tipError: String = "",
    val isGeneratingTip: Boolean = false,
    val tipsHistory: List<MedCoachTipEntity> = emptyList(),
    val showTipsDialog: Boolean = false
)

class MedCoachViewModel(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager,
    private val medInfoRepository: MedInfoRepository = MedInfoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedCoachUiState())
    val uiState: StateFlow<MedCoachUiState> = _uiState.asStateFlow()

    private val geminiModel = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    init {
        loadPatientContext()
    }

    private fun loadPatientContext() {
        val patientId = sessionManager.getLoggedInPatientId() ?: return

        viewModelScope.launch {
            repository.getPatientById(patientId)?.let { patient ->
                _uiState.value = _uiState.value.copy(patientName = patient.name)
            }
        }

        viewModelScope.launch {
            repository.getMedicationsByPatientId(patientId).collect { meds ->
                _uiState.value = _uiState.value.copy(
                    savedMedicationNames = meds.map { it.medicationName }.distinct().sorted()
                )
            }
        }

        viewModelScope.launch {
            repository.getSymptomsByPatientId(patientId).collect { symptoms ->
                _uiState.value = _uiState.value.copy(
                    symptomsHistory = symptoms.sortedByDescending { it.symptomId }
                )
            }
        }

        viewModelScope.launch {
            repository.getTipsByPatientId(patientId).collect { tips ->
                _uiState.value = _uiState.value.copy(tipsHistory = tips)
            }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value, drugError = "")
    }

    fun onSuggestionSelected(value: String) {
        _uiState.value = _uiState.value.copy(query = value, drugError = "")
    }

    fun onSearchClick() {
        val state = _uiState.value
        val query = state.query.trim()

        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = state.copy(drugError = "Please enter a medication name")
                return@launch
            }

            _uiState.value = state.copy(isLoadingDrug = true, drugError = "")

            when (val result = medInfoRepository.searchDrugInfo(query)) {
                is DrugLookupResult.Success -> {
                    _uiState.value = state.copy(
                        brandName = result.info.brandName.orEmpty(),
                        purpose = result.info.purpose.orEmpty().ifBlank { "Not available" },
                        warnings = result.info.warnings.orEmpty().ifBlank { "Not available" },
                        dosageAndAdministration = result.info.dosageAndAdministration.orEmpty().ifBlank { "Not available" },
                        drugError = "",
                        isLoadingDrug = false
                    )
                }

                is DrugLookupResult.Error -> {
                    _uiState.value = state.copy(
                        brandName = "",
                        purpose = "",
                        warnings = "",
                        dosageAndAdministration = "",
                        drugError = result.message,
                        isLoadingDrug = false
                    )
                }
            }
        }
    }

    fun onGenerateTipClick() {
        val patientId = sessionManager.getLoggedInPatientId() ?: return

        viewModelScope.launch {
            val state = _uiState.value

            if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                _uiState.value = state.copy(
                    isGeneratingTip = false,
                    tipError = "Gemini API key is not configured."
                )
                return@launch
            }

            _uiState.value = state.copy(
                isGeneratingTip = true,
                tipError = ""
            )

            val symptomSummary = state.symptomsHistory
                .sortedByDescending { it.symptomId }
                .take(5)
                .joinToString("\n") {
                    "- ${it.category}, Severity ${it.severity}, on ${it.dateTime}"
                }

            val medList = state.savedMedicationNames.joinToString(", ")

            val prompt = """
            You are a medication adherence assistant.

            Generate a short, personalized encouraging message for this patient:

            Patient Name: ${state.patientName.ifBlank { "User" }}

            Medications:
            ${medList.ifBlank { "No medications recorded" }}

            Recent Symptoms:
            ${symptomSummary.ifBlank { "No symptoms recorded" }}

            Instructions:
            - Keep it short (2–4 sentences)
            - Make it personal using their symptoms + medications
            - Be supportive and encouraging
            - Do NOT provide a diagnosis
            - Focus on medication adherence and motivation
        """.trimIndent()

            try {
                val tip = generateWithRetry(prompt)

                if (tip.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingTip = false,
                        tipError = "Gemini returned an empty response. Please try again."
                    )
                    return@launch
                }

                repository.insertMedCoachTip(
                    MedCoachTipEntity(
                        patientId = patientId,
                        tipText = tip
                    )
                )

                _uiState.value = _uiState.value.copy(
                    generatedTip = tip,
                    isGeneratingTip = false,
                    tipError = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingTip = false,
                    tipError = friendlyGeminiError(e)
                )
            }
        }
    }

    private suspend fun generateWithRetry(prompt: String): String {
        var lastError: Exception? = null

        repeat(3) { attempt ->
            try {
                val response = withContext(Dispatchers.IO) {
                    geminiModel.generateContent(
                        content {
                            text(prompt)
                        }
                    )
                }

                return response.text?.trim().orEmpty()
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
                "Unable to generate a tip right now. Please try again."
        }
    }

    fun onShowTipsClick() {
        _uiState.value = _uiState.value.copy(showTipsDialog = true)
    }

    fun onDismissTipsDialog() {
        _uiState.value = _uiState.value.copy(showTipsDialog = false)
    }
}