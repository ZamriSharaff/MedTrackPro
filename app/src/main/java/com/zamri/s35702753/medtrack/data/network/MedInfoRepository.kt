package com.zamri.s35702753.medtrack.data.network

import java.io.IOException

class MedInfoRepository(
    private val apiService: APIService = APIService.create()
) {

    suspend fun searchDrugInfo(medName: String): DrugLookupResult {
        val query = medName.trim()
        if (query.isBlank()) {
            return DrugLookupResult.Error("Please enter a medication name")
        }

        return try {
            val response = apiService.getDrugLabel(
                search = """openfda.brand_name:"$query"""",
                limit = 1
            )

            if (!response.isSuccessful) {
                return when (response.code()) {
                    404 -> DrugLookupResult.Error("No drug information found for \"$query\".")
                    else -> DrugLookupResult.Error("Unable to retrieve drug information right now.")
                }
            }

            val item = response.body()?.results?.firstOrNull()
                ?: return DrugLookupResult.Error("No drug information found for \"$query\".")

            val info = DrugInfo(
                brandName = item.openfda?.brandName?.firstOrNull() ?: query,
                purpose = item.purpose?.firstOrNull(),
                warnings = item.warnings?.firstOrNull(),
                dosageAndAdministration = item.dosageAndAdministration?.firstOrNull()
            )

            DrugLookupResult.Success(info)
        } catch (_: IOException) {
            DrugLookupResult.Error("Network unavailable. Please check your connection.")
        } catch (_: Exception) {
            DrugLookupResult.Error("Something went wrong while loading drug information.")
        }
    }
}