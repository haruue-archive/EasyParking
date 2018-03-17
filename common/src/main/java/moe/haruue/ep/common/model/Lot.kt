package moe.haruue.ep.common.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data class Lot(
        @SerializedName("_id") var id: String,
        @SerializedName("name") var name: String,
        @SerializedName("description") var description: String = "",
        @SerializedName("city") var city: String,
        @SerializedName("location") var location: String = "",
        @SerializedName("geographic") var geographic: GeographicLocation,
        @SerializedName("type") var type: Int,
        @SerializedName("spots") var spots: List<Spot> = listOf()
)