package com.android.trackmealscapstone.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.trackmealscapstone.api.ApiConfig
import com.android.trackmealscapstone.viewmodel.ProfileViewModel
import com.android.trackmealscapstone.viewmodel.ProfileViewModelFactory
import com.android.trackmealscapstone.viewmodel.SharedViewModel

class GallerySelectionActivity : ComponentActivity() {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val apiService = ApiConfig.getApiService(this)
        val factory = ProfileViewModelFactory(apiService)
        profileViewModel = ViewModelProvider(this, factory).get(ProfileViewModel::class.java)
        val sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        val calories = sharedViewModel.totalCalories.value ?: 0

        setupImagePickerLauncher()

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "profile") {
                composable("profile") {
                    ProfileScreen(
                        navController = navController,
                        onChangePictureClick = { selectImageFromGallery() },
                        profileViewModel = profileViewModel,
                        sharedViewModel = sharedViewModel,
                        caloriesConsumed = calories
                    )
                }
            }
        }
    }

    private fun setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { selectedUri ->
                handleImageSelection(selectedUri)
            }
        }
    }

    private fun selectImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun handleImageSelection(uri: Uri) {
        // Retrieve user ID and Auth Token from shared preferences
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("USER_ID", -1) // Replace with your logic to get the user ID
        val authToken = sharedPreferences.getString("AUTH_TOKEN", null) // Allow null as a default value

        if (userId != -1 && !authToken.isNullOrBlank()) {
            profileViewModel.uploadProfileImage(this, uri, userId, authToken)
            profileViewModel.updateProfileImage(uri)
            profileViewModel.saveProfileImageUri(this, uri)
        } else {
            // Handle invalid userID or authToken
            // Log or alert user
        }
    }
}