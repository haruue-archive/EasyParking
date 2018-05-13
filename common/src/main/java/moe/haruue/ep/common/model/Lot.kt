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
data class Lot(
        @SerializedName("_id") var id: String,
        @SerializedName("name") var name: String,
        @SerializedName("description") private var _description: String?,
        @SerializedName("city") var city: String,
        @SerializedName("location") private var _location: String?,
        @SerializedName("geographic") var geographic: GeographicLocation,
        @SerializedName("type") var type: Int,
        @SerializedName("spots") private var _spots: List<Spot>?
) : Parcelable {

    companion object {
        @JvmField val CREATOR = parcelableCreatorOf<Lot>()
    }

    override fun equals(other: Any?): Boolean {
        return other is Lot && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    var description: String
        get() = _description ?: ""
        set(value) { _description = value }

    var location: String
        get() = _location ?: ""
        set(value) { _location = value }

    var spots: List<Spot>
        get() = _spots ?: listOf()
        set(value) { _spots = value }

    @Suppress("unused") /*Parcelable*/
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(GeographicLocation::class.java.classLoader),
            parcel.readInt(),
            parcel.readMutableList<Spot>())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(_description)
        parcel.writeString(city)
        parcel.writeString(_location)
        parcel.writeParcelable(geographic, flags)
        parcel.writeInt(type)
        parcel.writeList(_spots)
    }

    override fun describeContents() = 0

}