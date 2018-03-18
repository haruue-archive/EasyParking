package moe.haruue.ep.common.util

import android.arch.lifecycle.MutableLiveData

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

fun <T> mutableLiveDataOf(default: T? = null): MutableLiveData<T> {
    val data = MutableLiveData<T>()
    if (default != null) {
        data.value = default
    }
    return data
}