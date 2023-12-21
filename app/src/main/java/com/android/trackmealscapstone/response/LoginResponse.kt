package com.android.trackmealscapstone.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("accessToken")
	val accessToken: String? = null
)
