package com.android.trackmealscapstone.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.trackmealscapstone.api.ApiConfig
import kotlinx.coroutines.launch

class HealthFactViewModel(context: Context) : ViewModel() {
    var healthFact: String = "Loading..."
    var healthFactSourceUrl: String? = null
    private val apiService = ApiConfig.getApiService(context)

    init {
        fetchHealthFact()
    }

    private fun fetchHealthFact() {
        viewModelScope.launch {
            try {
                val response = apiService.getFactHealths()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    if (!data.isNullOrEmpty()) {
                        healthFact = data.first()?.fact ?: "No fact available"
                        healthFactSourceUrl = data.first()?.source
                    }
                } else {
                    healthFact = "Failed to load fact"
                }
            } catch (e: Exception) {
                healthFact = "Error: ${e.message}"
            }
        }
    }
}