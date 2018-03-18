package moe.haruue.ep.common.util

import android.content.Context
import android.support.annotation.IntDef
import android.support.annotation.StringRes
import android.widget.Toast

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
@Retention(AnnotationRetention.SOURCE)
annotation class Duration

fun Context.toast(msg: String, @Duration length: Int = Toast.LENGTH_SHORT): Toast
    = Toast.makeText(this.applicationContext, msg, length).also { it.show() }

fun Context.toast(@StringRes resId: Int, @Duration length: Int = Toast.LENGTH_SHORT): Toast
        = Toast.makeText(this.applicationContext, resId, length).also { it.show() }
