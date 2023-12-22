package com.android.trackmealscapstone.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private const val BASE_URL = "https://backend-dot-capstone-trackmeals-405419.et.r.appspot.com"

    fun getApiService(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer ${getTokenFromStorage(context)}")
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ApiService::class.java)
    }

    fun getTokenFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("JWT_TOKEN", "") ?: ""
    }

    fun getUserIdFromStorage(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        // Using a default value of -1 or any other value to indicate that the user ID is not found.
        return sharedPreferences.getInt("userId", 1)
    }

    fun getUserNameFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("USER_NAME", "User") ?: "User"
    }
}