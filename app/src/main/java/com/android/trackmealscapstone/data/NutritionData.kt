package com.android.trackmealscapstone.data

data class NutritionData(
    val calories: Int,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val minerals: Double
)

val foodNutritionMap = mapOf(
    "Nasi Putih" to NutritionData(180, 3.0, 0.3, 39.8, 56.7),
    "Telur Rebus" to NutritionData(155, 13.0, 11.0, 1.1, 0.0),
    "Telur Goreng" to NutritionData(196, 14.0, 15.0, 0.9, 0.0),
    "Dada Ayam" to NutritionData(150, 28.74, 3.0, 0.0, 0.0),
    "Tumis Kangkung" to NutritionData(39, 2.6, 2.7, 3.1, 0.0),
    "Tempe Goreng" to NutritionData(336, 20.0, 28.0, 7.8, 42.9),
    "Salmon" to NutritionData(179, 19.93, 10.43, 0.0, 71.64),
    "Nasi Merah" to NutritionData(149, 2.8, 0.4, 32.5, 64.0),
    "Jeruk" to NutritionData(45, 0.9, 0.2, 11.2, 87.2),
    "Alpukat" to NutritionData(85, 0.9, 6.5, 7.7, 84.3),
    "Sayur Bayam" to NutritionData(16, 0.9, 0.4, 2.9, 94.5)
)