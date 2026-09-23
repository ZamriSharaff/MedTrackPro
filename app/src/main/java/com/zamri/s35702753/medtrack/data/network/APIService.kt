package com.zamri.s35702753.medtrack.data.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {

    @GET("drug/label.json")
    suspend fun getDrugLabel(
        @Query("search") search: String,
        @Query("limit") limit: Int = 1
    ): Response<DrugLabelResponse>

    companion object {
        private const val BASE_URL = "https://api.fda.gov/"

        fun create(): APIService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(APIService::class.java)
        }
    }
}