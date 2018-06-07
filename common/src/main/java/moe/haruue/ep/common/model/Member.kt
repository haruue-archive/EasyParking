package moe.haruue.ep.common.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import moe.haruue.util.kotlin.parcelableCreatorOf
import moe.haruue.util.kotlin.readMutableList

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
) : Parcelable {
    var email: String
        get() = _email ?: ""
        set(value) { _email = value }

    var mobile: String
        get() = _mobile ?: ""
        set(value) { _mobile = value }

    var cars: List<Car>
        get() = _cars ?: listOf()
        set(value) { _cars = value }

    @Suppress("unused")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readString(),
            parcel.readByte() != 0.toByte(),
            parcel.readMutableList())

    override fun equals(other: Any?): Boolean {
        return other is Member && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(username)
        parcel.writeString(_email)
        parcel.writeByte(if (isEmailVerified) 1 else 0)
        parcel.writeString(_mobile)
        parcel.writeByte(if (isMobileVerified) 1 else 0)
        parcel.writeList(_cars)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @Suppress("unused")
        @JvmField val CREATOR = parcelableCreatorOf<Member>()
    }
}