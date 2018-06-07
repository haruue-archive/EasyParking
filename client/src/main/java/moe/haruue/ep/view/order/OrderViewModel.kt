package moe.haruue.ep.view.order

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.toPriceString
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderViewModel : ViewModel() {

    val status = mutableLiveDataOf("")
    val progress = mutableLiveDataOf(0)
    val spotLog = mutableLiveDataOf<Log>()
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
        progress.postValue(1)
        if (car.value.isNullOrBlank()) {
            status.postValue("请选择车辆")
            progress.postValue(0)
            return
        }
        if (spotId.value.isNullOrBlank()) {
            status.postValue("请选择停车位")
            progress.postValue(0)
            return
        }
        progress.postValue(30)
        MainAPIService.with { it.lotParkOrder(car.value!!, lotId.value!!, spotId.value!!) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("OrderViewModel#checkOrder") {
                    onNext = {
                        spotLog.postValue(it.data)
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            needLogin.postValue(true)
                        } else {
                            status.postValue(it.message)
                        }
                    }
                    onNetworkError = {
                        status.postValue("网络错误: ${it.localizedMessage}。请检查网络连接，然后重试。")
                    }
                    onOtherError = {
                        status.postValue("未知错误: ${it.localizedMessage}。请重试。")
                    }
                    onFinally = {
                        progress.postValue(100)
                    }
                }
    }

}