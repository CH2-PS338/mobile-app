package com.android.trackmealscapstone.api

import com.android.trackmealscapstone.data.UserLoginData
import com.android.trackmealscapstone.data.UserRegistrationData
import com.android.trackmealscapstone.response.AddMealResponse
import com.android.trackmealscapstone.response.FactHealthResponse
import com.android.trackmealscapstone.response.LoginResponse
import com.android.trackmealscapstone.response.LogoutResponse
import com.android.trackmealscapstone.response.RegisterResponse
import com.android.trackmealscapstone.response.TokenResponse
import com.android.trackmealscapstone.response.UserMealResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiService {

    @POST("register")
    suspend fun registerUser(@Body userData: UserRegistrationData): Response<RegisterResponse>

    @POST("login")
    suspend fun loginUser(@Body loginData: UserLoginData): Response<LoginResponse>

    @GET("token")
    suspend fun getToken(): Response<TokenResponse>

    @DELETE("logout")
    suspend fun logoutUser(): Response<LogoutResponse>

    @GET("meals/{id}")
    suspend fun getAllUserMeals(
        @Path("id") userId: Int,
        @Header("Authorization") authToken: String
    ): Response<UserMealResponse>

    @POST("addmeal/{id}")
    suspend fun addMeal(
        @Path("id") userId: Int,
        @Header("Authorization") authToken: String,
        @Body meals_name: List<String>
    ): Response<AddMealResponse>

    @GET()
    suspend fun getFactHealth(

    ) : Response<FactHealthResponse>
}