package com.android.trackmealscapstone.response

import com.google.gson.annotations.SerializedName

data class UserMealResponse(

	@field:SerializedName("data")
	val data: List<DataItem?>? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataItem(

	@field:SerializedName("createdAt")
	val createdAt: String? = null,

	@field:SerializedName("mealId")
	val mealId: Int? = null,

	@field:SerializedName("meals_name")
	val mealsName: String? = null
)
