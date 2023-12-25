package com.android.trackmealscapstone.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.trackmealscapstone.api.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileViewModel(private val apiService: ApiService) : ViewModel() {

    var profileImageUri by mutableStateOf<Uri?>(null)
        private set

    fun updateProfileImage(uri: Uri) {
        profileImageUri = uri
    }

    fun uploadProfileImage(context: Context, uri: Uri, userId: Int, authToken: String) {
        val file = convertUriToFile(context, uri)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

        viewModelScope.launch {
            try {
                val response = apiService.updatePhotoProfile(userId, "Bearer $authToken", part)
                if (response.isSuccessful) {
                    saveProfileImageUri(context, uri)
                } else {
                    // handle upload error
                    val errorBody = response.errorBody()?.string()
                    Log.e("UploadError", errorBody ?: "Unknown Error")
                }
            } catch (e: Exception) {
                // handle exception
                Log.e("UploadException", e.message ?: "Unknown Exception")
            }
        }
    }

    private fun convertUriToFile(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "uploadImage")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    fun saveProfileImageUri(context: Context, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("PROFILE_IMAGE_URI", uri.toString())
            apply()
        }
    }

    fun loadProfileImageUri(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("PROFILE_IMAGE_URI", null)
        uriString?.let {
            profileImageUri = Uri.parse(it)
        }
    }
}