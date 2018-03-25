package moe.haruue.ep.common.viewmodel

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.model.Spot
import moe.haruue.ep.common.util.mutableLiveDataOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SpotItemViewModel : ViewModel() {

    val id = mutableLiveDataOf<String>()
    val location = mutableLiveDataOf<String>()
    val price = mutableLiveDataOf<Double>()
    val type = mutableLiveDataOf<Int>()
    val logId = mutableLiveDataOf<String>()

    var data: Spot? = null
        set(value) {
            field = value
            if (value != null) {
                id.postValue(value.id)
                location.postValue(value.id)
                price.postValue(value.price)
                type.postValue(value.type)
                logId.postValue(value.logId)
            } else {
                id.postValue(null)
                location.postValue(null)
                price.postValue(null)
                type.postValue(null)
                logId.postValue(null)
            }
        }
}
