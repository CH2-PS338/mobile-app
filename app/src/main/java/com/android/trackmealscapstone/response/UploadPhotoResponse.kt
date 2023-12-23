package com.android.trackmealscapstone.response

import com.google.gson.annotations.SerializedName

data class UploadPhotoResponse(

	@field:SerializedName("data")
	val data: DataUploadPhoto? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataUploadPhoto(

	@field:SerializedName("profilePic")
	val profilePic: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null
)
