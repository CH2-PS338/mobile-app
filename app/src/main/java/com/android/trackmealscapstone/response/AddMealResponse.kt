package com.android.trackmealscapstone.response

import com.google.gson.annotations.SerializedName

data class AddMealResponse(

	@field:SerializedName("data")
	val data: UserMealData? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class UserMealData(

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("meals")
	val meals: Meals? = null
)

data class Meals(

	@field:SerializedName("minerals")
	val minerals: Int? = null,

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("mealId")
	val mealId: Int? = null,

	@field:SerializedName("carbs")
	val carbs: Int? = null,

	@field:SerializedName("fats")
	val fats: Int? = null,

	@field:SerializedName("proteins")
	val proteins: Int? = null,

	@field:SerializedName("meals_name")
	val mealsName: String? = null,

	@field:SerializedName("calories")
	val calories: Int? = null,

	@field:SerializedName("updatedAt")
	val updatedAt: String? = null
)
