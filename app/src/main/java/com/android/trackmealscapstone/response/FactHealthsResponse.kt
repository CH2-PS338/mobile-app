package com.android.trackmealscapstone.response

import com.google.gson.annotations.SerializedName

data class FactHealthsResponse(

	@field:SerializedName("data")
	val data: List<DataFact?>? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class DataFact(

	@field:SerializedName("fact")
	val fact: String? = null,

	@field:SerializedName("factId")
	val factId: Int? = null,

	@field:SerializedName("source")
	val source: String? = null
)
