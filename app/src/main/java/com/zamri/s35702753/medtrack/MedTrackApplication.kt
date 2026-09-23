package com.zamri.s35702753.medtrack

import android.app.Application
import com.zamri.s35702753.medtrack.data.MedTrackDatabase
import com.zamri.s35702753.medtrack.data.MedTrackRepository
import com.zamri.s35702753.medtrack.data.SessionManager

class MedTrackApplication : Application() {

    val database by lazy {
        MedTrackDatabase.getDatabase(this)
    }

    val repository by lazy {
        MedTrackRepository(
            database.patientDao(),
            database.medicationDao(),
            database.symptomDao(),
            database.medCoachTipDao(),
            database.takenStatusDao()
        )
    }

    val sessionManager by lazy {
        SessionManager(this)
    }
}