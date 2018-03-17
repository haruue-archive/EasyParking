package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Spot(
        @SerializedName("_id") val id: String,
        @SerializedName("location") val location: String,
        @SerializedName("price") val price: Double,
        @SerializedName("type") val type: Int,
        @SerializedName("logId") val logId: String? = null
)