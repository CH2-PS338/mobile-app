package com.android.trackmealscapstone.api

import android.content.Context
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
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
        val jwtToken = sharedPreferences.getString("JWT_TOKEN", null) ?: return -1 // Return an invalid ID if token is not present

        val payload = jwtToken.split(".")[1] // Get the Payload part
        val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
        val decodedString = String(decodedBytes, Charsets.UTF_8)

        return try {
            val jsonObject = JSONObject(decodedString)
            // Assuming the user ID field is named "userId" and is a string that can be parsed as an integer
            jsonObject.getString("userId").toInt()
        } catch (e: Exception) {
            -1 // Return an invalid ID in case of error
        }
    }

    fun getUserNameFromStorage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("USER_NAME", "User") ?: "User"
    }
}