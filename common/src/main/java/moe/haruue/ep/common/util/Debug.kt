package moe.haruue.ep.common.util

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

private fun Context.debugMode(): Boolean {
    val info = packageManager.getApplicationInfo(packageName, 0)
    return info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}

fun debug(context: Context? = null, r: () -> Unit) {
    ApplicationContextHandler.context = context
    if (isDebug) {
        r()
    }
}

fun release(context: Context? = null, r: () -> Unit) {
    ApplicationContextHandler.context = context
    if (!isDebug) {
        r()
    }
}

var isDebug: Boolean = false
    get() = field || ApplicationContextHandler.context?.debugMode() ?: false
