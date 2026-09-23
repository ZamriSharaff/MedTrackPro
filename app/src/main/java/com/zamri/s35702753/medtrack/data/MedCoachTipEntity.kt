package com.zamri.s35702753.medtrack.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class MedCoachTipEntity(
    @PrimaryKey(autoGenerate = true)
    val tipId: Int = 0,
    val patientId: String,
    val tipText: String,
    val createdAt: Long = System.currentTimeMillis()
)