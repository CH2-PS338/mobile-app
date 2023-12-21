package com.android.trackmealscapstone.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.trackmealscapstone.R
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.data.UserLoginData
import com.android.trackmealscapstone.response.LoginResponse
import com.android.trackmealscapstone.ui.theme.orangePrimary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }

    var emailUserHasTyped by remember { mutableStateOf(false) }
    var passwordUserHasTyped by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiConfig.getApiService(context)

    var loginResponse by remember { mutableStateOf<LoginResponse?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_trackmeals),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
        )
        CustomTextField(
            value = email,
            onValueChange = {
                email = it
                emailUserHasTyped = true
            },
            label = "Email",
            validateInput = ::isValidEmailLogin,
            errorMessage = "Invalid email format",
            userHasTyped = emailUserHasTyped
        )
        CustomTextField(
            value = password,
            onValueChange = {
                password = it
                passwordUserHasTyped = true
            },
            label = "Password",
            validateInput = ::isValidPasswordLogin,
            errorMessage = "Password must be at least 8 characters",
            userHasTyped = passwordUserHasTyped
        )
        RememberMeCheckbox(
            checked = rememberMe,
            onCheckedChange = { rememberMe = it }
        )
        CustomButton(
            text = "Log In",
            onClick = {
                if (isValidEmailLogin(email) && isValidPasswordLogin(password)) {
                    coroutineScope.launch {
                        try {
                            val response = apiService.loginUser(UserLoginData(email, password))
                            if (response.isSuccessful && response.body() != null) {
                                val loginResponse = response.body()!!
                                saveTokenToStorage(context, loginResponse.accessToken, rememberMe)
                                navController.navigate("dashboard")
                            } else {
                                showAlert = true
                            }
                        } catch (e: Exception) {
                            showAlert = true
                        }
                    }
                } else {
                    showAlert = true
                }
            }
        )
        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = { Text("Validation Error") },
                text = { Text("Please check your email and password are correct.") },
                confirmButton = {
                    Button(onClick = { showAlert = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        CustomClickableText(
            text = "Forgot your password?",
            onClick = { /* TODO: Handle forgot password logic here */ },
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomClickableText(
            text = "Don't have an account? Register here!",
            onClick = { navController.navigate("register") },
        )
    }
}

@Composable
fun CustomClickableText(
    text: String,
    onClick: () -> Unit,
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(color = orangePrimary, fontWeight = FontWeight.SemiBold)
) {
    ClickableText(
        text = AnnotatedString(text),
        onClick = { onClick() },
        style = MaterialTheme.typography.bodyMedium.copy(color = orangePrimary, fontWeight = FontWeight.SemiBold)
    )
}

private fun saveTokenToStorage(context: Context, token: String?, rememberMe: Boolean) {
    val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("JWT_TOKEN", token)
        putBoolean("REMEMBER_ME", rememberMe)
        apply()
    }
}

private fun saveUserNameToStorage(context: Context, userName: String) {
    val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("USER_NAME", userName)
        apply()
    }
}

fun isValidEmailLogin(email: String): Boolean {
    return email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
}

fun isValidPasswordLogin(password: String): Boolean {
    return password.length >= 8
}