package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.MedicationEntity
import com.zamri.s35702753.medtrack.data.SessionManager
import com.zamri.s35702753.medtrack.data.TakenStatusEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val patientId: String = "",
    val patientName: String = "",
    val medications: List<MedicationEntity> = emptyList(),
    val takenMap: Map<Int, Boolean> = emptyMap(),
    val todayDate: String = ""
)

class HomeViewModel(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val patientId = sessionManager.getLoggedInPatientId()
        if (!patientId.isNullOrBlank()) {
            loadHomeData(patientId)
        }
    }

    private fun today(): String = LocalDate.now().toString()

    private fun loadHomeData(patientId: String) {
        val date = today()

        viewModelScope.launch {
            repository.clearOldTakenData(patientId, date)
            val patient = repository.getPatientById(patientId)
            val name = patient?.name ?: patientId

            _uiState.value = _uiState.value.copy(
                patientId = patientId,
                patientName = name,
                todayDate = date
            )

            repository.getMedicationsByPatientId(patientId)
                .collect { meds ->

                    _uiState.value = _uiState.value.copy(medications = meds)

                    repository.getTodayTakenStatuses(patientId, date)
                        .collect { takenList ->

                            val map = takenList.associate {
                                it.medicationId to it.isTaken
                            }

                            _uiState.value = _uiState.value.copy(
                                takenMap = map
                            )
                        }
                }
        }
    }

    fun toggleTaken(medicationId: Int, isTaken: Boolean) {
        viewModelScope.launch {
            val patientId = _uiState.value.patientId
            val date = _uiState.value.todayDate

            repository.setTakenStatus(
                TakenStatusEntity(
                    patientId = patientId,
                    medicationId = medicationId,
                    date = date,
                    isTaken = isTaken
                )
            )
        }
    }
}