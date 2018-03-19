package moe.haruue.ep.manager.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
data  class Manager(
        @SerializedName("id") var id: String,
        @SerializedName("password") var password: String
)