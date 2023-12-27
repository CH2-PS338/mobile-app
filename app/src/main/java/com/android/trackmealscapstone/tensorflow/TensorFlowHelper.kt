package com.android.trackmealscapstone.tensorflow

import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.content.Context
import android.util.Log

object TensorFLowHelper {

    val imageSize = 320

    fun classifyImage(context: Context, image: Bitmap, callback: (food: String) -> Unit) {
        val modelFile = "model-fix-final.tflite"
        val interpreter: Interpreter

        try {
            context.assets.openFd(modelFile).use { fileDescriptor ->
                val inputStream = fileDescriptor.createInputStream()
                val fileChannel = inputStream.channel
                val startOffset = fileDescriptor.startOffset
                val declaredLength = fileDescriptor.declaredLength
                val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
                interpreter = Interpreter(modelBuffer)
            }
        } catch (e: IOException) {
            Log.e("TensorFlowHelper", "Error loading model: ${e.message}")
            return
        }

        val resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 11), DataType.FLOAT32)
        interpreter.run(inputFeature0.buffer, outputFeature0.buffer)

        // Decode the output into a human-readable label.
        val label = decodeOutput(outputFeature0.floatArray)
        callback.invoke(label)

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

    private fun decodeOutput(confidences: FloatArray): String {
        val classes = arrayOf(
            "Nasi Putih",
            "Telur Rebus",
            "Telur Goreng",
            "Dada Ayam",
            "Tumis Kangkung",
            "Tempe Goreng",
            "Salmon",
            "Nasi Merah",
            "Jeruk",
            "Alpukat",
            "Sayur Bayam"
        )

        val maxPos = confidences.indices.maxByOrNull { confidences[it] } ?: -1
        return if (maxPos == -1) "Unknown" else classes[maxPos]
    }
}