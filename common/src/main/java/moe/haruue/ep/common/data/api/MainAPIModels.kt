package moe.haruue.ep.common.data.api

import com.google.gson.annotations.SerializedName

/**
 * api wrappers
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class APIResult<T>(
        @SerializedName("code") val code: Int,
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: T
)

data class APIError<T>(
        @SerializedName("code") val code: Int,
        @SerializedName("status") val status: String,
        @SerializedName("message") val message: String,
        @SerializedName("errno") val errno: Int,
        @SerializedName("ref") val ref: String
)
