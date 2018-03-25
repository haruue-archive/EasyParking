package moe.haruue.ep.common.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import moe.haruue.util.kotlin.parcelableCreatorOf

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
) : Parcelable {

    @Suppress("unused")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(location)
        parcel.writeDouble(price)
        parcel.writeInt(type)
        parcel.writeString(logId)
    }

    override fun describeContents() = 0

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = parcelableCreatorOf<Spot>()
    }

}