package com.android.trackmealscapstone.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.trackmealscapstone.R
import com.android.trackmealscapstone.ui.theme.TrackMealsCapstoneTheme
import com.android.trackmealscapstone.ui.theme.orangePrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.orange_primary)
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
                    composable("dashboard") { DashboardScreen(navController) }
                    composable("scan") { ScanScreen(navController) }
                    composable("activity_log") { ActivityLogScreen(navController) }
                    composable("profile") { ProfileScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }
                }
            }
        }
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
    userHasTyped: Boolean
) {
    val isError = !validateInput(value) && userHasTyped

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
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

@Preview(showBackground = true, apiLevel = 31)
@Composable
fun DefaultPreview() {
    TrackMealsCapstoneTheme {
        val navController = rememberNavController()
        ScanScreen(navController)
    }
}

private fun isUserLoggedIn(sharedPreferences: SharedPreferences): Boolean {
    val rememberMe = sharedPreferences.getBoolean("REMEMBER_ME", false)
    val token = sharedPreferences.getString("JWT_TOKEN", null)
    return rememberMe && !token.isNullOrEmpty()
}