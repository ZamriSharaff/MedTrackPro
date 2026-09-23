package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.SessionManager
import com.zamri.s35702753.medtrack.data.SymptomEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class SymptomTrendPoint(
    val dateLabel: String,
    val count: Int,
    val averageSeverity: Float
)

data class SymptomsUiState(
    val symptomCategory: String = "",
    val severity: Float = 0f,
    val additionalNotes: String = "",
    val date: String = "",
    val time: String = "",
    val error: String = "",
    val symptoms: List<SymptomEntity> = emptyList(),
    val trendPoints: List<SymptomTrendPoint> = emptyList()
)

sealed class SymptomsEvent {
    data class ShowSnackbar(val message: String) : SymptomsEvent()
}

class SymptomsViewModel(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymptomsUiState())
    val uiState: StateFlow<SymptomsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SymptomsEvent>()
    val events: SharedFlow<SymptomsEvent> = _events

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val patientId = sessionManager.getLoggedInPatientId() ?: return

        viewModelScope.launch {
            repository.getSymptomsByPatientId(patientId)
                .catch { emit(emptyList()) }
                .collect { symptoms ->
                    val sorted = symptoms.sortedByDescending { it.dateTime }
                    _uiState.value = _uiState.value.copy(
                        symptoms = sorted,
                        trendPoints = buildTrendPoints(sorted)
                    )
                }
        }
    }

    fun onCategoryChange(value: String) {
        _uiState.value = _uiState.value.copy(symptomCategory = value, error = "")
    }

    fun onSeverityChange(value: Float) {
        _uiState.value = _uiState.value.copy(severity = value, error = "")
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(additionalNotes = value)
    }

    fun onDateChange(value: String) {
        _uiState.value = _uiState.value.copy(date = value, error = "")
    }

    fun onTimeChange(value: String) {
        _uiState.value = _uiState.value.copy(time = value, error = "")
    }

    fun onClearClick() {
        _uiState.value = _uiState.value.copy(
            symptomCategory = "",
            severity = 0f,
            additionalNotes = "",
            date = "",
            time = "",
            error = ""
        )
    }

    fun onSaveClick() {
        val state = _uiState.value

        viewModelScope.launch {
            val error = validate(state)
            if (error.isNotBlank()) {
                _uiState.value = state.copy(error = error)
                return@launch
            }

            val patientId = sessionManager.getLoggedInPatientId()
            if (patientId.isNullOrBlank()) {
                _uiState.value = state.copy(error = "No active session found")
                return@launch
            }

            repository.insertSymptom(
                SymptomEntity(
                    patientId = patientId,
                    category = state.symptomCategory,
                    severity = state.severity.toInt(),
                    notes = state.additionalNotes.trim(),
                    dateTime = "${state.date} ${state.time}"
                )
            )

            _uiState.value = state.copy(
                symptomCategory = "",
                severity = 0f,
                additionalNotes = "",
                date = "",
                time = "",
                error = ""
            )

            _events.emit(SymptomsEvent.ShowSnackbar("Symptom logged successfully"))
        }
    }

    private fun validate(state: SymptomsUiState): String {
        if (
            state.symptomCategory.isBlank() ||
            state.severity !in 1f..10f ||
            state.date.isBlank() ||
            state.time.isBlank()
        ) {
            return "Please fill all required fields"
        }

        if (state.additionalNotes.length > 200) {
            return "Maximum 200 characters for notes"
        }

        return ""
    }

    private fun buildTrendPoints(symptoms: List<SymptomEntity>): List<SymptomTrendPoint> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val labelFormatter = DateTimeFormatter.ofPattern("dd MMM")
        val today = LocalDate.now()

        val last7Days = (6L downTo 0L).map { offset ->
            today.minusDays(offset)
        }

        return last7Days.map { day ->
            val daySymptoms = symptoms.filter { symptom ->
                val symptomDate = symptom.dateTime.substringBefore(" ").trim()
                try {
                    LocalDate.parse(symptomDate, formatter) == day
                } catch (_: DateTimeParseException) {
                    false
                }
            }

            val count = daySymptoms.size
            val avgSeverity = if (count == 0) {
                0f
            } else {
                daySymptoms.map { it.severity }.average().toFloat()
            }

            SymptomTrendPoint(
                dateLabel = day.format(labelFormatter),
                count = count,
                averageSeverity = avgSeverity
            )
        }
    }
}