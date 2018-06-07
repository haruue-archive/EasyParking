package moe.haruue.ep.common.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import moe.haruue.util.kotlin.parcelableCreatorOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Car(
        @SerializedName("id") var id: String,
        @SerializedName("type") var type: Int = 1,
        @SerializedName("logId") var logId: String? = null
) : Parcelable {
    @Suppress("unused")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readString())

    @Suppress("unused")
    companion object {
        const val TYPE_SMALL = 1
        const val TYPE_MIDDLE = 2
        const val TYPE_LARGE = 3

        @Suppress("unused")
        @JvmField val CREATOR = parcelableCreatorOf<Car>()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeInt(type)
        parcel.writeString(logId)
    }

    override fun describeContents(): Int {
        return 0
    }
}