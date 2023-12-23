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

    fun classifyImage(context: Context, image: Bitmap, callback : (food : String) -> Unit) {
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
            // Handle exception
            Log.e("TensorFlowHelper", "Error loading model: ${e.message}")
            return
        }

        val resizedImage = Bitmap.createScaledBitmap(image, imageSize, imageSize, true)
        Log.d("TensorFlowHelper", "Resized image dimensions: ${resizedImage.width} x ${resizedImage.height}")

        val intValues = IntArray(imageSize * imageSize)
        try {
            resizedImage.getPixels(intValues, 0, resizedImage.width, 0, 0, resizedImage.width, resizedImage.height)
        } catch (e: Exception) {
            Log.e("TensorFlowHelper", "Error in getPixels: ${e.message}")
            return
        }

        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 320, 320, 3), DataType.FLOAT32)
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0
        // iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputFeature0: TensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1000), DataType.FLOAT32)
        interpreter.run(inputFeature0.buffer, outputFeature0.buffer)
        val confidences = outputFeature0.floatArray
        // find the index of the class with the biggest confidence.
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }
        val classes = arrayOf(
            "Alpukat",
            "Telur Rebus",
            "Nasi Putih",
            "Telur Goreng",
            "Dada Ayam",
            "Tumis Kangkung",
            "Tempe Goreng",
            "Salmon",
            "Nasi Merah",
            "Jeruk",
            "Sayur Bayam")
        callback.invoke(classes[maxPos])

        interpreter.close()
    }
}