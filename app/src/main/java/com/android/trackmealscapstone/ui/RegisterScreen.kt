package com.android.trackmealscapstone.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.trackmealscapstone.R
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.data.UserRegistrationData
import kotlinx.coroutines.launch

@Composable
fun RegisterScreenTopBar(onCloseClicked: () -> Unit, onLoginClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClicked) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close"
            )
        }

        CustomClickableText(
            text = "Login",
            onClick = { onLoginClicked() },
        )
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiConfig.getApiService(context)

    var nameUserHasTyped by remember { mutableStateOf(false) }
    var emailUserHasTyped by remember { mutableStateOf(false) }
    var passwordUserHasTyped by remember { mutableStateOf(false) }
    var confirmpasswordUserHasTyped by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RegisterScreenTopBar(
            onCloseClicked = { navController.popBackStack() },
            onLoginClicked = { navController.navigate("login") }
        )

        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
                .padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = name,
            onValueChange = {
                name = it
                nameUserHasTyped = true
            },
            label = "Name",
            validateInput = ::isValidNameRegister,
            errorMessage = "Full name must be at least 5 to 30 characters",
            userHasTyped = nameUserHasTyped
        )

        CustomTextField(
            value = email,
            onValueChange = {
                email = it
                emailUserHasTyped = true
            },
            label = "Email",
            validateInput = ::isValidEmailRegister,
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
            validateInput = ::isValidPasswordRegister,
            errorMessage = "Password must be at least 8 characters",
            userHasTyped = passwordUserHasTyped,
            trailingIcon = {
                val image = if (passwordVisible)
                    painterResource(id = R.drawable.visibility_icon)
                else
                    painterResource(id = R.drawable.visibility_off_icon)

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = image, contentDescription = "Toggle Password Visibility")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        CustomTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmpasswordUserHasTyped = true
            },
            label = "Confirm Password",
            validateInput = { isValidConfirmPasswordRegister(password, confirmPassword) },
            errorMessage = "Password do not match",
            userHasTyped = confirmpasswordUserHasTyped,
            trailingIcon = {
                val image = if (confirmPasswordVisible)
                    painterResource(id = R.drawable.visibility_icon)
                else
                    painterResource(id = R.drawable.visibility_off_icon)

                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(painter = image, contentDescription = "Toggle Password Visibility")
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomButton(
            text = "Sign Up",
            onClick = {
                if (isValidNameRegister(name) && isValidEmailRegister(email) && isValidPasswordRegister(password) && isValidConfirmPasswordRegister(password, confirmPassword)) {
                    coroutineScope.launch {
                        try {
                            val userRegistrationData = UserRegistrationData(name, email, password, confirmPassword)
                            val response = apiService.registerUser(userRegistrationData)
                            if (response.isSuccessful && response.body() != null) {
                                saveUserNameToStorage(context, name)
                                navController.navigate("login")
                            } else {
                                // Handle the error here. For example, showing the alert dialog with the message.
                                showAlert = true
                            }
                        } catch (e: Exception) {
                            // Handle the exception here. For example, showing the alert dialog with a generic error message.
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
                title = { Text("Invalid Input") },
                text = { Text("Please check your inputs and try again.") },
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
            onClick = { /* TODO: Handle forgot password here */ }
        )
    }
}

private fun saveUserNameToStorage(context: Context, userName: String) {
    val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("USER_NAME", userName)
        apply()
    }
}

fun isValidNameRegister(name: String): Boolean {
    return name.length in 5..30
}

fun isValidEmailRegister(email: String): Boolean {
    return email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
}

fun isValidPasswordRegister(password: String): Boolean {
    return password.length >= 8
}

fun isValidConfirmPasswordRegister(originalPassword: String, confirmPassword: String): Boolean {
    return originalPassword == confirmPassword
}