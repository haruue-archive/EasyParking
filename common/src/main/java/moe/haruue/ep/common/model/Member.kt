package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Member(
        @SerializedName("_id") var id: String,
        @SerializedName("username") var username: String,
        @SerializedName("email") var email: String = "",
        @SerializedName("emailVerified") var isEmailVerified: Boolean = false,
        @SerializedName("mobile") var mobile: String = "",
        @SerializedName("mobileVerified") var isMobileVerified: Boolean = false,
        @SerializedName("cars") var cars: List<Car> = listOf()
)