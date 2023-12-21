package com.android.trackmealscapstone.ui

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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ActivityLogScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        ActivityLogScreenContent(innerPadding)
    }
}

@Composable
fun ActivityLogScreenContent(paddingValues: PaddingValues) {
    Column {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item { ActivityLogList() }
        }
    }
}

@Composable
fun ActivityLogList() {
    Column {
        Text(
            text = "Your History",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        val nutritionData = listOf(
            Pair("Kacang", "21/12/2023"),
            Pair("Sop Ayam", "20/12/2023"),
            Pair("Salad", "19/12/2023"),
            Pair("Opor Ayam", "18/12/2023"),
            Pair("Rendang", "17/12/2023"),
            Pair("Ayam Geprek", "16/12/2023")
        )

        nutritionData.forEach { (nutrition, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = nutrition, style = MaterialTheme.typography.bodyLarge)
                Text(text = amount, style = MaterialTheme.typography.bodyLarge)
            }
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
        }
    }
}