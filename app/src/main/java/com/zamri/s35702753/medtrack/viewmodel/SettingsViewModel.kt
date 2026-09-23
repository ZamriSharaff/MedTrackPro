package com.zamri.s35702753.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val patientId: String = "",
    val fullName: String = "",
    val phoneNumber: String = ""
)

sealed class SettingsEvent {
    data object NavigateToLogin : SettingsEvent()
}

class SettingsViewModel(
    private val repository: MedTrackRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val patientId = sessionManager.getLoggedInPatientId() ?: return

        viewModelScope.launch {
            val patient = repository.getPatientById(patientId)
            _uiState.value = SettingsUiState(
                patientId = patientId,
                fullName = patient?.name ?: "",
                phoneNumber = patient?.phoneNumber ?: ""
            )
        }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _events.emit(SettingsEvent.NavigateToLogin)
        }
    }
}