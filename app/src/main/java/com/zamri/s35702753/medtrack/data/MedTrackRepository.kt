package com.zamri.s35702753.medtrack.data

import kotlinx.coroutines.flow.Flow

class MedTrackRepository(
    private val patientDao: PatientDao,
    private val medicationDao: MedicationDao,
    private val symptomDao: SymptomDao,
    private val medCoachTipDao: MedCoachTipDao,
    private val takenStatusDao: TakenStatusDao
) {

    suspend fun insertPatients(patients: List<PatientEntity>) {
        patientDao.insertPatients(patients)
    }

    suspend fun insertPatient(patient: PatientEntity) {
        patientDao.insertPatient(patient)
    }

    suspend fun insertMedications(medications: List<MedicationEntity>) {
        medicationDao.insertMedications(medications)
    }

    suspend fun insertMedication(medication: MedicationEntity) {
        medicationDao.insertMedication(medication)
    }

    suspend fun insertSymptoms(symptoms: List<SymptomEntity>) {
        symptomDao.insertSymptoms(symptoms)
    }

    suspend fun insertSymptom(symptom: SymptomEntity) {
        symptomDao.insertSymptom(symptom)
    }

    suspend fun insertMedCoachTip(tip: MedCoachTipEntity) {
        medCoachTipDao.insertTip(tip)
    }

    suspend fun getPatientById(patientId: String): PatientEntity? {
        return patientDao.getPatientById(patientId)
    }

    suspend fun getPatientByPhone(phoneNumber: String): PatientEntity? {
        return patientDao.getPatientByPhone(phoneNumber)
    }

    suspend fun updatePassword(patientId: String, password: String) {
        patientDao.updatePassword(patientId, password)
    }

    suspend fun generateNextPatientId(): String {
        val max = patientDao.getAllPatients()
            .mapNotNull { it.patientId.removePrefix("P").toIntOrNull() }
            .maxOrNull() ?: 1010

        return "P${max + 1}"
    }

    fun getMedicationsByPatientId(patientId: String): Flow<List<MedicationEntity>> {
        return medicationDao.getMedicationsByPatientId(patientId)
    }

    fun getSymptomsByPatientId(patientId: String): Flow<List<SymptomEntity>> {
        return symptomDao.getSymptomsByPatientId(patientId)
    }

    fun getTipsByPatientId(patientId: String): Flow<List<MedCoachTipEntity>> {
        return medCoachTipDao.getTipsByPatientId(patientId)
    }

    suspend fun setTakenStatus(status: TakenStatusEntity) {
        takenStatusDao.insert(status)
    }

    fun getTodayTakenStatuses(patientId: String, date: String): Flow<List<TakenStatusEntity>> {
        return takenStatusDao.getTodayStatus(patientId, date)
    }

    suspend fun clearOldTakenData(patientId: String, date: String) {
        takenStatusDao.clearOldDays(patientId, date)
    }

    suspend fun getTotalPatients(): Int {
        return patientDao.getTotalPatients()
    }

    suspend fun getTotalMedications(): Int {
        return medicationDao.getTotalMedications()
    }

    suspend fun getAverageMedicationsPerPatient(): Double {
        val patients = patientDao.getTotalPatients()
        val meds = medicationDao.getTotalMedications()
        val avg = if (patients == 0) 0.0 else meds.toDouble() / patients
        return Math.round(avg * 100) / 100.0
    }

    suspend fun getMostCommonSymptom(): String {
        return symptomDao.getMostCommonCategory() ?: "N/A"
    }

    suspend fun getAverageSymptomSeverity(): Float {
        val avg = symptomDao.getAverageSeverity() ?: 0f
        return Math.round(avg * 100) / 100f
    }

    suspend fun getAllPatients(): List<PatientEntity> {
        return patientDao.getAllPatients()
    }

    suspend fun getAllMedications(): List<MedicationEntity> {
        return medicationDao.getAllMedications()
    }

    suspend fun getAllSymptoms(): List<SymptomEntity> {
        return symptomDao.getAllSymptoms()
    }
}