package com.bionics.BionicsSCAN.network

import com.bionics.BionicsSCAN.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BionicInventoryApi {
    
    @GET("inventory")
    suspend fun getInventory(): InventoryResponseDto
    
    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionRequestDto): TransactionResponseDto
    
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = false // Changed to false to omit nulls/defaults
            explicitNulls = false
        }
        
        fun create(): BionicInventoryApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY // Changed to BODY to see error messages
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(BionicAuthInterceptor())
                .addInterceptor(loggingInterceptor)
                .build()
            
            return Retrofit.Builder()
                .baseUrl(BuildConfig.BIONIC_INVENTORY_API_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(BionicInventoryApi::class.java)
        }
    }
}
