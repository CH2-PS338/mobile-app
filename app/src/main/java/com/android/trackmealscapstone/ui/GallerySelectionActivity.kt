package com.android.trackmealscapstone.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.trackmealscapstone.viewmodel.ProfileViewModel

class GallerySelectionActivity : ComponentActivity() {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImageUri(uri)
        }

        setContent {
            val navController = rememberNavController()
            profileViewModel = viewModel()

            NavHost(navController, startDestination = "profile") {
                composable("profile") {
                    // Here we pass the lambda to ProfileScreen
                    ProfileScreen(navController, onChangePictureClick = { selectImageFromGallery() }, viewModel = profileViewModel)
                }
            }
        }
    }

    private fun selectImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun handleImageUri(uri: Uri?) {
        uri?.let {
            profileViewModel.uploadProfileImage(this, it)
        }
    }
}
