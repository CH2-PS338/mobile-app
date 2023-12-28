package com.android.trackmealscapstone.tensorflow

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object TensorFLowHelper {

    val imageSize = 320
    const val MIN_CONFIDENCE = 0.5f // Define a threshold for confidence

    fun detectObjects(context: Context, image: Bitmap, callback: (detectedObjects: List<DetectedObject>) -> Unit) {
        val modelFile = "model-fix-final.tflite"
        val interpreter: Interpreter

        try {
            context.assets.openFd(modelFile).use { fileDescriptor ->
                val modelBuffer: MappedByteBuffer = fileDescriptor.createInputStream().channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fileDescriptor.startOffset,
                    fileDescriptor.declaredLength
                )
                interpreter = Interpreter(modelBuffer)
            }
        } catch (e: IOException) {
            Log.e("TensorFlowHelper", "Error loading model: ${e.message}")
            return
        }

        val resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)
        val outputScores = Array(1) { FloatArray(10) }
        val outputs = mapOf(
            0 to outputScores // Update this based on the model's actual output
        )

        interpreter.runForMultipleInputsOutputs(arrayOf(byteBuffer), outputs)

        // Process the output to extract detected objects
        val detectedObjects = mutableListOf<DetectedObject>()
        for (i in outputScores[0].indices) {
            val score = outputScores[0][i]
            if (score > MIN_CONFIDENCE) {
                val classLabel = decodeLabel(i) // Assuming the index represents the class
                detectedObjects.add(DetectedObject(classLabel, FloatArray(4), score)) // Placeholder for boundingBox
            }
        }

        callback.invoke(detectedObjects)

        interpreter.close()
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (value in intValues) {
            val r = ((value shr 16) and 0xFF) / 255.0f
            val g = ((value shr 8) and 0xFF) / 255.0f
            val b = (value and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }

    private fun decodeLabel(classIdx: Int): String {
        // Replace this with your actual labels
        val classes = arrayOf(
            "Nasi Putih", "Telur Rebus", "Telur Goreng", "Dada Ayam", "Tumis Kangkung",
            "Tempe Goreng", "Salmon", "Nasi Merah", "Jeruk", "Alpukat", "Sayur Bayam"
        )
        return classes.getOrElse(classIdx) { "Unknown" }
    }

    data class DetectedObject(val label: String, val boundingBox: FloatArray, val confidence: Float)
}