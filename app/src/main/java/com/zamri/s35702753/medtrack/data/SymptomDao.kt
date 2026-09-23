package com.zamri.s35702753.medtrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<SymptomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Query("SELECT * FROM SymptomEntity WHERE patientId = :patientId")
    fun getSymptomsByPatientId(patientId: String): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM SymptomEntity")
    suspend fun getAllSymptoms(): List<SymptomEntity>

    @Query("SELECT AVG(severity) FROM SymptomEntity")
    suspend fun getAverageSeverity(): Float?

    @Query("""
        SELECT category 
        FROM SymptomEntity 
        GROUP BY category 
        ORDER BY COUNT(category) DESC 
        LIMIT 1
    """)
    suspend fun getMostCommonCategory(): String?
}