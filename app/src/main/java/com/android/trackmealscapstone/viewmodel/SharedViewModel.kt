package com.android.trackmealscapstone.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.trackmealscapstone.data.NutritionData
import com.android.trackmealscapstone.data.foodNutritionMap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class SharedViewModel : ViewModel() {
    private val _lastScannedFood = MutableLiveData<NutritionData>()
    val lastScannedFood: LiveData<NutritionData> = _lastScannedFood

    private val _scannedFoodHistory = mutableStateOf<List<String>>(listOf())
    val scannedFoodHistory: State<List<String>> = _scannedFoodHistory

    private val _totalCalories = MutableLiveData<Int>()
    val totalCalories: LiveData<Int> = _totalCalories

    fun updateLastScannedFood(foodName: String) {
        _lastScannedFood.value = foodNutritionMap[foodName] ?: NutritionData(0, 0.0, 0.0, 0.0, 0.0)
    }

    fun updateTotalCalories(calories: Int) {
        _totalCalories.value = calories
    }

    fun addFoodToHistory(foodName: String) {
        _scannedFoodHistory.value = _scannedFoodHistory.value + foodName
    }
}