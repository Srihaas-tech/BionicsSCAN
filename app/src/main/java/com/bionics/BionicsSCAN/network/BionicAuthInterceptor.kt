package com.bionics.BionicsSCAN.network

import com.bionics.BionicsSCAN.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class BionicAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requestWithAuth = originalRequest.newBuilder()
            .header("x-api-token", BuildConfig.BIONIC_INVENTORY_API_KEY)
            .header("Content-Type", "application/json")
            .build()
        
        return chain.proceed(requestWithAuth)
    }
}
