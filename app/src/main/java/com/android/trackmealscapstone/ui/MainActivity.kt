package com.android.trackmealscapstone.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.trackmealscapstone.R
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.ui.theme.TrackMealsCapstoneTheme
import com.android.trackmealscapstone.ui.theme.orangePrimary
import com.android.trackmealscapstone.viewmodel.ProfileViewModel
import com.android.trackmealscapstone.viewmodel.ProfileViewModelFactory
import com.android.trackmealscapstone.viewmodel.SharedViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType

class MainActivity : ComponentActivity() {
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.orange_primary)

        val apiService = ApiConfig.getApiService(this)
        val factory = ProfileViewModelFactory(apiService)
        profileViewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        profileViewModel.loadProfileImageUri(this)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { profileViewModel.updateProfileImage(it) }
        }

        val sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        setContent {
            TrackMealsCapstoneTheme {
                val navController = rememberNavController()

                val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

                val startDestination = if (isUserLoggedIn(sharedPreferences)) {
                    "dashboard"
                } else {
                    "login"
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("dashboard") {
                        DashboardScreen(navController, sharedViewModel, "")
                    }

                    composable(
                        "dashboardWithFood/{scannedFoodName}",
                        arguments = listOf(navArgument("scannedFoodName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val scannedFoodName = backStackEntry.arguments?.getString("scannedFoodName") ?: ""
                        DashboardScreen(navController, sharedViewModel, scannedFoodName)
                    }

                    composable("scan") {
                        ScanScreen(navController, sharedViewModel)
                    }

                    composable("activity_log") {
                        ActivityLogScreen(navController, sharedViewModel)
                    }

                    composable("profile") {
                        val calories = sharedViewModel.totalCalories.value ?: 0  // Provide a default value
                        ProfileScreen(
                            navController = navController,
                            onChangePictureClick = { selectImageFromGallery() },
                            profileViewModel = profileViewModel,
                            sharedViewModel = sharedViewModel,
                            caloriesConsumed = calories
                        )
                    }

                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                }
            }
        }
    }

    private fun selectImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem("dashboard", { Icon(painterResource(id = R.drawable.home_icon), contentDescription = "Dashboard") }, "Dashboard"),
        NavigationItem("scan", { Icon(painterResource(id = R.drawable.qr_code_icon), contentDescription = "Scan") }, "Scan"),
        NavigationItem("activity_log", { Icon(painterResource(id = R.drawable.history_icon), contentDescription = "Activity Log") }, "Activity Log"),
        NavigationItem("profile", { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") }, "Profile"),
    )
    NavigationBar(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        val currentRoute = navController.currentDestination?.route
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = { item.iconComposable() },
                label = { Text(item.title) },
                selected = selected,
                onClick = { if (currentRoute != item.route) navController.navigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (selected) orangePrimary else MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = if (selected) orangePrimary else MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    validateInput: (String) -> Boolean,
    errorMessage: String,
    userHasTyped: Boolean,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val isError = !validateInput(value) && userHasTyped

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation
    )

    if (isError) {
        Text(
            text = errorMessage,
            color = Color.Red,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
    ) {
        Text(text = text, color = Color.White)
    }
}

@Composable
fun RememberMeCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(text = "Remember Me")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TrackMealsCapstoneTheme {
        RegisterScreen(navController = rememberNavController())
    }
}

private fun isUserLoggedIn(sharedPreferences: SharedPreferences): Boolean {
    val rememberMe = sharedPreferences.getBoolean("REMEMBER_ME", false)
    val token = sharedPreferences.getString("JWT_TOKEN", null)
    return rememberMe && !token.isNullOrEmpty()
}