package moe.haruue.ep.common.util

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

fun Double.toPriceString(): String {
    val str = toString()
    var (i, f) = str.split('.')
    when {
        f.isEmpty() -> f = "00"
        f.length == 1 -> f += "0"
        f.length > 2 -> f = f.substring(0, 2)
    }
    return "$i.$f"
}
