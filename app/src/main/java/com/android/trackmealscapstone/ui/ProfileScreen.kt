@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.trackmealscapstone.ui

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.trackmealscapstone.R
import com.android.trackmealscapstone.api.ApiConfig.getUserNameFromStorage
import com.android.trackmealscapstone.ui.theme.orangePrimary

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    Scaffold(
        topBar = { ProfileTopAppBar(navController = navController, context = context) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        ProfileContent(innerPadding, context)
    }
}

@Composable
fun ProfileTopAppBar(navController: NavController, context: Context) {
    val topPadding = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    SmallTopAppBar(
        title = {},
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.White
        ),
        actions = {
            TextButton(
                onClick = {
                    clearLoginState(context)
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                content = { Text(text = "Logout", color = orangePrimary) }
            )
        },
        modifier = Modifier.padding(top = topPadding)
    )
}

@Composable
fun ProfileContent(paddingValues: PaddingValues, context: Context) {
    val fullName = getUserNameFromStorage(context)

    Column {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Add item for the profile component with the retrieved full name
            item { ProfileAvatar(fullName = fullName) }
            item { CircularGraph(percentage = 0.50F, calories = 10) }
        }
    }
}

@Composable
fun ProfileAvatar(fullName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, CircleShape)
                .padding(3.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.avatar_icon),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(160.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Hello There!",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = fullName,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
fun CircularGraph(percentage: Float, calories: Int) {
    Box(contentAlignment = Alignment.Center) {
        Spacer(modifier = Modifier.height(320.dp))
        Canvas(modifier = Modifier.size(260.dp)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeftOffset = Offset(strokeWidth / 2, strokeWidth / 2)
            val size = Size(radius * 2, radius * 2)
            val sweepAngle = 360 * percentage

            // Draw the background circle
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeftOffset,
                size = size,
                style = Stroke(width = 5.dp.toPx())
            )

            // Draw the progress arc
            drawArc(
                color = orangePrimary,
                startAngle = 90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeftOffset,
                size = size,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$calories Kal",
                style = MaterialTheme.typography.headlineLarge,
                color = orangePrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You spent 50% less calories\nthan other weeks.",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun clearLoginState(context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        remove("JWT_TOKEN")
        remove("REMEMBER_ME")
        apply()
    }
}