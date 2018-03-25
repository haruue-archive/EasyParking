package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

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
) {
    var description: String
        get() = _description ?: ""
        set(value) { _description = value }

    var location: String
        get() = _location ?: ""
        set(value) { _location = value }

    var spots: List<Spot>
        get() = _spots ?: listOf()
        set(value) { _spots = value }
}