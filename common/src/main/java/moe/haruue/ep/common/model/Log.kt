package moe.haruue.ep.common.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import moe.haruue.util.kotlin.parcelableCreatorOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Log(
        @SerializedName("_id") var id: String,
        @SerializedName("memberId") var memberId: String,
        @SerializedName("member") var member: Member,
        @SerializedName("carId") var carId: String,
        @SerializedName("car") var car: Car?,
        @SerializedName("lotId") var lotId: String,
        @SerializedName("lot") var lot: Lot,
        @SerializedName("spotId") var spotId: String,
        @SerializedName("spot") var spot: Spot?,
        @SerializedName("create") var createTime: Long,
        @SerializedName("start") var startTime: Long = 0,
        @SerializedName("end") var endTime: Long = 0,
        @SerializedName("price") var price: Double,
        @SerializedName("fee") var fee: Double = 0.0,
        @SerializedName("status") var status: Int,
        @SerializedName("paid") var paid: Boolean = false
) : Parcelable {
    @Suppress("unused")
    companion object {
        const val STATUS_ORDERED = 0
        const val STATUS_PARKED = 1
        const val STATUS_REMOVED = 2
        const val STATUS_CANCELED = 3

        @Suppress("unused")
        @JvmField val CREATOR = parcelableCreatorOf<Log>()
    }

    var statusText: String?
        get() {
            return when (status) {
                0 -> "预约中"
                1 -> "停放中"
                2 -> "已移除"
                3 -> "已取消"
                else -> "未知"
            }
        }
        set(value) {
            status = when (value) {
                "预约中" -> 0
                "停放中" -> 1
                "已移除" -> 2
                "已取消" -> 3
                else -> throw IllegalArgumentException("unexpected statusText: $value")
            }
        }

    @Suppress("unused")
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Member::class.java.classLoader),
            parcel.readString(),
            parcel.readParcelable(Car::class.java.classLoader),
            parcel.readString(),
            parcel.readParcelable(Lot::class.java.classLoader),
            parcel.readString(),
            parcel.readParcelable(Spot::class.java.classLoader),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte())

    override fun equals(other: Any?): Boolean {
        return other is Log && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(memberId)
        parcel.writeParcelable(member, flags)
        parcel.writeString(carId)
        parcel.writeParcelable(car, flags)
        parcel.writeString(lotId)
        parcel.writeParcelable(lot, flags)
        parcel.writeString(spotId)
        parcel.writeParcelable(spot, flags)
        parcel.writeLong(createTime)
        parcel.writeLong(startTime)
        parcel.writeLong(endTime)
        parcel.writeDouble(price)
        parcel.writeDouble(fee)
        parcel.writeInt(status)
        parcel.writeByte(if (paid) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

}