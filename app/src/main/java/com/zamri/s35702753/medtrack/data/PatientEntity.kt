package com.zamri.s35702753.medtrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PatientEntity(
    @PrimaryKey
    val patientId: String,
    val phoneNumber: String,
    val name: String,
    val password: String
)
