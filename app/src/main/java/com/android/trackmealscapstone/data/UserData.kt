package com.android.trackmealscapstone.data

data class UserRegistrationData(
    val name: String,
    val email: String,
    val password: String,
    val confirm_password: String
)

data class UserLoginData(
    val email: String,
    val password: String
)