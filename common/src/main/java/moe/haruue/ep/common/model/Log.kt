package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Log(
        @SerializedName("_id") var id: String,
        @SerializedName("memberId") var memberId: String,
        @SerializedName("member") var member: Member,
        @SerializedName("carId") var carId: String,
        @SerializedName("lotId") var lotId: String,
        @SerializedName("lot") var lot: Lot,
        @SerializedName("spotId") var spotId: String,
        @SerializedName("create") var createTime: Long,
        @SerializedName("start") var startTime: Long = 0,
        @SerializedName("end") var endTime: Long = 0,
        @SerializedName("price") var price: Double,
        @SerializedName("fee") var fee: Int = 0,
        @SerializedName("status") var status: Int,
        @SerializedName("paid") var paid: Boolean = false
) {
    companion object {
        const val STATUS_ORDERED = 0
        const val STATUS_PARKED = 1
        const val STATUS_REMOVED = 2
        const val STATUS_CANCELED = 3
    }
}