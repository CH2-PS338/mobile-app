package com.android.trackmealscapstone.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.response.DataItem
import com.android.trackmealscapstone.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

@Composable
fun ActivityLogScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val foodHistory = sharedViewModel.scannedFoodHistory.value
    val apiService = ApiConfig.getApiService(context)

    val mealsState = remember { mutableStateOf<List<DataItem>?>(null) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        val userId = ApiConfig.getUserIdFromStorage(context)
        val authToken = "Bearer ${ApiConfig.getTokenFromStorage(context)}"
        coroutineScope.launch {
            try {
                val response = apiService.getAllUserMeals(userId, authToken)
                if (response.isSuccessful) {
                    mealsState.value = response.body()?.data?.filterNotNull()
                } else {
                    Log.e("ActivityLogError", "Error fetching meals: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ActivityLogException", "Exception fetching meals: ${e.message}")
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        ActivityLogScreenContent(innerPadding, mealsState.value)
    }
}

@Composable
fun ActivityLogScreenContent(paddingValues: PaddingValues, meals: List<DataItem>?) {
    Column {
        Text(
            text = "Your History",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (meals.isNullOrEmpty()) {
                item {
                    Text(
                        text = "No history available.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(meals) { meal ->
                    FoodHistoryItem(
                        foodName = meal.mealsName ?: "Unknown",
                        date = meal.createdAt ?: "Unknown"
                    )
                }
            }
        }
    }
}

@Composable
fun FoodHistoryItem(foodName: String, date: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = foodName, style = MaterialTheme.typography.bodyLarge)
            Text(text = date, style = MaterialTheme.typography.bodyLarge)
        }
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
    }
}