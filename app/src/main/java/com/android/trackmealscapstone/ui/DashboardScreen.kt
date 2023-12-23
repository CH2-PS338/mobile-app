package com.android.trackmealscapstone.ui

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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.LocalUriHandler


@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val healthFactViewModel = viewModel { HealthFactViewModel(context)}

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        DashboardContent(innerPadding, healthFactViewModel)
    }
}

@Composable
fun DashboardContent(paddingValues: PaddingValues, viewModel: HealthFactViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        item { YourProgressGraph() }
        item { NutritionList() }
        item { HealthFactHeadline(viewModel) }
    }
}

data class NavigationItem(
    val route: String,
    val iconComposable: @Composable () -> Unit,
    val title: String
)

@Composable
fun YourProgressGraph() {
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
                // Sample data for the graph
                val nutritionData = listOf(
                    Pair(50, "Calories"),
                    Pair(70, "Carbs"),
                    Pair(80, "Proteins"),
                    Pair(60, "Fats"),
                    Pair(90, "Minerals")
                )
                val maxData = nutritionData.maxOf { it.first }.toFloat()

                nutritionData.forEach { (value, label) ->
                    BoxWithConstraints(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                        val barWidth = (value / maxData) * constraints.maxWidth
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
                                size = Size(barWidth, 30.dp.toPx()),
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
fun NutritionList() {
    Column {
        Text(
            text = "Amount",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 16.dp)
        )
        val nutritionData = listOf(
            Pair("Calories", "180 kcal"),
            Pair("Carbs", "39.8 g"),
            Pair("Proteins", "3 g"),
            Pair("Fats", "1.47 g"),
            Pair("Minerals", "64 mg"),
        )

        nutritionData.forEach { (nutrition, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = nutrition, style = MaterialTheme.typography.bodyMedium)
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