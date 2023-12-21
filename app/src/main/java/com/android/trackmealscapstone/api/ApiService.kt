package com.android.trackmealscapstone.api

import com.android.trackmealscapstone.data.UserLoginData
import com.android.trackmealscapstone.data.UserRegistrationData
import com.android.trackmealscapstone.response.LoginResponse
import com.android.trackmealscapstone.response.LogoutResponse
import com.android.trackmealscapstone.response.RegisterResponse
import com.android.trackmealscapstone.response.TokenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Response

interface ApiService {

    @POST("register")
    suspend fun registerUser(@Body userData: UserRegistrationData): Response<RegisterResponse>

    @POST("login")
    suspend fun loginUser(@Body loginData: UserLoginData): Response<LoginResponse>

    @GET("token")
    suspend fun getToken(): Response<TokenResponse>

    @DELETE("logout")
    suspend fun logoutUser(): Response<LogoutResponse>
}