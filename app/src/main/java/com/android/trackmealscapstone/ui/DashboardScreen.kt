package com.android.trackmealscapstone.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.trackmealscapstone.ui.theme.orangePrimary
import com.android.trackmealscapstone.viewmodel.HealthFactViewModel
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.platform.LocalUriHandler
import com.android.trackmealscapstone.data.NutritionData
import com.android.trackmealscapstone.data.foodNutritionMap

@Composable
fun DashboardScreen(navController: NavController, scannedFoodName: String? = null) {
    val context = LocalContext.current
    val healthFactViewModel = viewModel {
        HealthFactViewModel(context.applicationContext as Context)
    }

    val nutritionData = if (scannedFoodName != null) {
        foodNutritionMap[scannedFoodName] ?: NutritionData(0, 0.0, 0.0, 0.0, 0.0)
    } else {
        NutritionData(0, 0.0, 0.0, 0.0, 0.0)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        DashboardContent(innerPadding, healthFactViewModel, nutritionData)
    }
}

@Composable
fun DashboardContent(paddingValues: PaddingValues, viewModel: HealthFactViewModel, nutritionData: NutritionData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item { YourProgressGraph(nutritionData) }
        item { NutritionList(nutritionData) }
        item { HealthFactHeadline(viewModel) }
    }
}

data class NavigationItem(
    val route: String,
    val iconComposable: @Composable () -> Unit,
    val title: String
)

@Composable
fun YourProgressGraph(nutritionData: NutritionData) {
    Column {
        Text(
            text = "Your progress",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 16.dp)
        )
        Box(
            modifier = Modifier
                .padding(16.dp)
                .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(16.dp))
                .background(color = Color.White, shape = RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val nutritionValues = listOf(
                    nutritionData.calories.toDouble(),
                    nutritionData.proteins,
                    nutritionData.fats,
                    nutritionData.carbs,
                    nutritionData.minerals
                )
                val maxData = nutritionValues.maxOf { it }
                val nutritionLabels = listOf("Calories", "Proteins", "Fats", "Carbs", "Minerals")

                nutritionLabels.zip(nutritionValues).forEach { (label, value) ->
                    BoxWithConstraints(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                        val barWidth = (value / maxData) * constraints.maxWidth.toDouble()
                        Canvas(modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth()) {
                            drawRoundRect(
                                color = Color.LightGray,
                                size = Size(constraints.maxWidth.toFloat(), 30.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                            drawRoundRect(
                                color = orangePrimary,
                                topLeft = Offset.Zero,
                                size = Size(barWidth.toFloat(), 30.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }
                        Text(
                            text = "$value",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Text(
                        text = label,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionList(nutritionData: NutritionData) {
    Column {
        Text(
            text = "Amount",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 16.dp)
        )
        // Creating a list of Pair<String, String> for nutrition labels and their values
        val nutritionInfoList = listOf(
            "Calories" to "${nutritionData.calories} kcal",
            "Proteins" to "${nutritionData.proteins} g",
            "Fats" to "${nutritionData.fats} g",
            "Carbs" to "${nutritionData.carbs} g",
            "Minerals" to "${nutritionData.minerals} mg"
        )

        nutritionInfoList.forEach { (nutritionLabel, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = nutritionLabel, style = MaterialTheme.typography.bodyMedium)
                Text(text = amount, style = MaterialTheme.typography.bodyMedium)
            }
            Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(start = 16.dp, end = 16.dp))
        }
    }
}

@Composable
fun HealthFactHeadline(viewModel: HealthFactViewModel) {
    val uriHandler = LocalUriHandler.current

    Spacer(modifier = Modifier.height(16.dp))
    Column {
        Text(
            text = "Fact Health",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 16.dp)
        )
        Text(
            text = viewModel.healthFact,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        viewModel.healthFactSourceUrl?.let { url ->
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Black, fontSize = 14.sp)) {
                    append("URL: ")
                }
                withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline, fontSize = 14.sp)) {
                    pushStringAnnotation(tag = "URL", annotation = url)
                    append(url)
                    pop()
                }
            }
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Blue),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}