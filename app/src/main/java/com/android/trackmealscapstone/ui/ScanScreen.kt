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
import java.nio.ByteBuffer
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.ByteOrder
import java.util.concurrent.Executor
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.api.ApiConfig.getUserIdFromStorage
import com.android.trackmealscapstone.ui.theme.orangeSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val labelsArray = arrayOf(
    "telur-rebus",
    "nasi-putih",
    "telur-goreng",
    "dada-ayam",
    "tumis-kangkung",
    "tempe-goreng",
    "salmon",
    "nasi-merah",
    "jeruk",
    "alpukat",
    "sayur-bayam"
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScanScreen(navController: NavController) {
    val tfliteModel = loadModel(LocalContext.current)
    val interpreter = Interpreter(tfliteModel)

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
    val interpreter = remember { Interpreter(loadModel(context)) }

    val processImage: (Bitmap) -> Bitmap = { bitmap ->
        preprocessImage(bitmap)
    }

    val runInference: (Interpreter, Bitmap) -> String = { interpreter, bitmap ->
        runInference(interpreter, bitmap)
    }

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
                    captureImage(
                        cameraController,
                        interpreter,
                        executor,
                        processImage,
                        runInference,
                        displayResult,
                        context)
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
        contentAlignment = Alignment.Center, // Centers the dialog in the Box
        modifier = Modifier
            .fillMaxSize() // Make Box fill the entire screen for centering the dialog
            .background(Color.Black.copy(alpha = 0.5f)) // Optional: Dimmed background
    ) {
        // The dialog box itself
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
                    textAlign = TextAlign.Center // Centers the text
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Scanned food name is displayed here
                Text(
                    text = scannedFoodName,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center // Centers the text
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
    interpreter: Interpreter,
    executor: Executor,
    processImage: (Bitmap) -> Bitmap,
    runInference: (Interpreter, Bitmap) -> String,
    displayResult: (String) -> Unit,
    context: Context
) {
    cameraController.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val bitmap = imageProxyToBitmap(image)
            val processedImage = processImage(bitmap)
            val result = runInference(interpreter, processedImage)
            val authToken = "Bearer ${ApiConfig.getTokenFromStorage(context)}"
            displayResult(result)
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
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun preprocessImage(image: Bitmap): Bitmap {

    return Bitmap.createScaledBitmap(image, 200, 200, true)
}

fun runInference(interpreter: Interpreter, image: Bitmap): String {
    val byteBuffer = convertBitmapToByteBuffer(image)

    // Assuming the model outputs 10 classes
    val output = Array(1) { FloatArray(10) } // Adjusted to 10 classes

    interpreter.run(byteBuffer, output)

    val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
    return labelsArray[maxIndex]
}

fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    val width = 320  // Replace with model's expected width
    val height = 320 // Replace with model's expected height
    val channels = 3 // Replace with model's expected channels (e.g., 3 for RGB)

    val byteBuffer = ByteBuffer.allocateDirect(4 * width * height * channels) // float size * width * height * channels
    byteBuffer.order(ByteOrder.nativeOrder())

    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    val pixels = IntArray(width * height)
    resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

    for (pixel in pixels) {
        byteBuffer.putFloat((pixel shr 16 and 0xFF) / 255.0f) // Red channel
        byteBuffer.putFloat((pixel shr 8 and 0xFF) / 255.0f)  // Green channel
        byteBuffer.putFloat((pixel and 0xFF) / 255.0f)        // Blue channel
    }

    return byteBuffer
}

fun loadModel(context: Context): ByteBuffer {
    val modelFileName = "model-fix-final.tflite"
    val fileDescriptor = context.assets.openFd(modelFileName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}