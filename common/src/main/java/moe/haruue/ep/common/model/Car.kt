package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Car(
        @SerializedName("id") var id: String,
        @SerializedName("type") var type: Int = 1,
        @SerializedName("logId") var logId: String? = null
) {
    companion object {
        const val TYPE_SMALL = 1
        const val TYPE_MIDDLE = 2
        const val TYPE_LARGE = 3
    }
}