package com.zamri.s35702753.medtrack.data.network

import com.google.gson.annotations.SerializedName

data class DrugLabelResponse(
    val results: List<DrugLabelItem>? = null
)

data class DrugLabelItem(
    val openfda: OpenFdaFields? = null,
    val purpose: List<String>? = null,
    val warnings: List<String>? = null,
    @SerializedName("dosage_and_administration")
    val dosageAndAdministration: List<String>? = null
)

data class OpenFdaFields(
    @SerializedName("brand_name")
    val brandName: List<String>? = null
)

data class DrugInfo(
    val brandName: String? = null,
    val purpose: String? = null,
    val warnings: String? = null,
    val dosageAndAdministration: String? = null
)

sealed class DrugLookupResult {
    data class Success(val info: DrugInfo) : DrugLookupResult()
    data class Error(val message: String) : DrugLookupResult()
}