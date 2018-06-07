package moe.haruue.ep.common.model

import android.os.Parcel
import android.os.Parcelable
import moe.haruue.util.kotlin.parcelableCreatorOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class GeographicLocation(
        var longitude: Double,
        var latitude: Double
): Parcelable {

    companion object {
        @JvmField val CREATOR = parcelableCreatorOf<GeographicLocation>()
    }

    @Suppress("unused") /*Parcelable*/
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(longitude)
        parcel.writeDouble(latitude)
    }

    override fun describeContents() = 0

}