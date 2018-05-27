package moe.haruue.ep.view.order

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.toPriceString

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderViewModel : ViewModel() {

    val status = mutableLiveDataOf("")
    val progress = mutableLiveDataOf(0)
    val confirmed = mutableLiveDataOf(false)
    val needLogin = mutableLiveDataOf(false)

    val car = mutableLiveDataOf("")

    val lotId = mutableLiveDataOf<String>()
    val lotName = mutableLiveDataOf<String>()
    val lotDescription = mutableLiveDataOf<String>()
    val lotLocation = mutableLiveDataOf<String>()

    val spotId = mutableLiveDataOf("")
    var spotPrice: Double = 0.0
        set(value) {
            field = value
            spotPriceText.postValue("每小时 ${value.toPriceString()} 元")
        }
    val spotPriceText = mutableLiveDataOf<String>()
    var lotAveragePrice: Double = 0.0
        set(value) {
            field = value
            spotPriceText.postValue("平均每小时 ${value.toPriceString()} 元")
        }

    fun checkOrder() {
        // TODO: check order here
    }

}