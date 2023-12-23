@file:OptIn(ExperimentalPermissionsApi::class)

package com.android.trackmealscapstone.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.trackmealscapstone.R
import com.android.trackmealscapstone.ui.theme.orangePrimary
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.shouldShowRationale
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import java.util.concurrent.Executor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.api.ApiConfig.getUserIdFromStorage
import com.android.trackmealscapstone.tensorflow.TensorFLowHelper
import com.android.trackmealscapstone.ui.theme.orangeSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScanScreen(navController: NavController) {
    val showNotification = remember { mutableStateOf(false) }
    val scannedFoodName = remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiConfig.getApiService(context)

    val displayResult: (String) -> Unit = { result ->
        scannedFoodName.value = result
        showNotification.value = true
        Log.d("InferenceResult", "Detected food: $result")

        val userId = getUserIdFromStorage(context)
        val authToken = "Bearer ${ApiConfig.getTokenFromStorage(context)}"

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.addMeal(userId, authToken, listOf(result))
                if (response.isSuccessful) {
                    Log.d("AddMealSuccess", "Meal added: ${response.body()?.data}")
                } else {
                    Log.e("AddMealError", "Error adding meal: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("AddMealException", "Exception adding meal: ${e.message}")
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        ScanContent(innerPadding, navController, displayResult)
    }

    if (showNotification.value) {
        FoodScannedDialog(
            scannedFoodName = scannedFoodName.value,
            onDismiss = { showNotification.value = false },
            onViewHistoryClick = { navController.navigate("activity_log") }
        )
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanContent(paddingValues: PaddingValues, navController: NavController, displayResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val cameraController = remember { LifecycleCameraController(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    LaunchedEffect(key1 = Unit) {
        if (!cameraPermissionState.status.isGranted && !cameraPermissionState.status.shouldShowRationale) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.BottomCenter
    ) {
        when {
            cameraPermissionState.status.isGranted -> {
                CameraPreview(cameraController, lifecycleOwner)
            }
            cameraPermissionState.status.shouldShowRationale || !cameraPermissionState.status.isGranted -> {
                NoCameraPermissionScreen(cameraPermissionState)
            }
            else -> {
                Text("Waiting for the camera...")
            }
        }

        FloatingActionButton(
            onClick = {
                if (cameraPermissionState.status.isGranted) {
                    captureImage(cameraController, executor, displayResult, context)
                } else {
                    Log.e("CameraCapture", "Camera permission not granted")
                }
            },
            containerColor = orangePrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Icon(painterResource(id = R.drawable.camera_icon), contentDescription = "Capture")
        }
    }
}

@Composable
fun FoodScannedDialog(
    scannedFoodName: String,
    onDismiss: () -> Unit,
    onViewHistoryClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Food Scanned!",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Based on your picture that we get, it looks like your food is:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = scannedFoodName,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary)
                ) {
                    Text("OK")
                }
                TextButton(
                    onClick = onViewHistoryClick,
                ) {
                    Text(
                        text = "View History",
                        style = MaterialTheme.typography.bodyMedium.copy(color = orangeSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(cameraController: LifecycleCameraController, lifecycleOwner: LifecycleOwner) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .height(200.dp),
        factory = { context ->
            PreviewView(context).apply {
                setBackgroundColor(Color.White.toArgb())
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = PreviewView.ScaleType.FILL_START
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }.also { previewView ->
                previewView.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
            }
        },
        onRelease = {
            cameraController.unbind()
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NoCameraPermissionScreen(cameraPermissionState: PermissionState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera permission is required to continue.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
            Text("Request Permission")
        }
    }
}

fun captureImage(
    cameraController: LifecycleCameraController,
    executor: Executor,
    displayResult: (String) -> Unit,
    context: Context
) {
    cameraController.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = imageProxyToBitmap(image)
            // Use TensorFlowHelper to classify the image
            TensorFLowHelper.classifyImage(context, bitmap) { food ->
                displayResult(food)
            }
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraCapture", "Image capture failed", exception)
        }
    })
}

fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    return Bitmap.createScaledBitmap(bitmap, 320, 320, true)
}