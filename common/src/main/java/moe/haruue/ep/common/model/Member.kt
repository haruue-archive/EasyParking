package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Member(
        @SerializedName("_id") var id: String,
        @SerializedName("username") var username: String,
        @SerializedName("email") private var _email: String?,
        @SerializedName("emailVerified") var isEmailVerified: Boolean = false,
        @SerializedName("mobile") private var _mobile: String?,
        @SerializedName("mobileVerified") var isMobileVerified: Boolean = false,
        @SerializedName("cars") private var _cars: List<Car>?
) {
    var email: String
        get() = _email ?: ""
        set(value) { _email = value }

    var mobile: String
        get() = _mobile ?: ""
        set(value) { _mobile = value }

    var cars: List<Car>
        get() = _cars ?: listOf()
        set(value) { _cars = value }

    override fun equals(other: Any?): Boolean {
        return other is Member && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}