package moe.haruue.ep.common.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

fun Double.toPriceString(): String {
//    val str = toString()
//    var (i, f) = str.split('.')
//    when {
//        f.isEmpty() -> f = "00"
//        f.length == 1 -> f += "0"
//        f.length > 2 -> f = f.substring(0, 2)
//    }
//    return "$i.$f"
    return "%.2f".format(this)
}

fun Long.formatToDateTime(): String {
    if (this == 0L) return "暂不可用"
    return SimpleDateFormat.getDateTimeInstance().format(Date(this))
}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}