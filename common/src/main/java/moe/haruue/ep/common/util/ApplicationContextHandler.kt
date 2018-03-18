package moe.haruue.ep.common.util

import android.annotation.SuppressLint
import android.content.Context

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
@SuppressLint("StaticFieldLeak")
object ApplicationContextHandler {
    var context: Context? = null
        set(value) {
            if (field == null && value != null) {
                field = value.applicationContext
            }
        }
}