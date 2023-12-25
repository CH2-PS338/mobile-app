package com.android.trackmealscapstone.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.trackmealscapstone.data.NutritionData
import com.android.trackmealscapstone.data.foodNutritionMap

class SharedViewModel : ViewModel() {
    private val _lastScannedFood = MutableLiveData<NutritionData>()
    val lastScannedFood: LiveData<NutritionData> = _lastScannedFood

    fun updateLastScannedFood(foodName: String) {
        _lastScannedFood.value = foodNutritionMap[foodName] ?: NutritionData(0, 0.0, 0.0, 0.0, 0.0)
    }
}