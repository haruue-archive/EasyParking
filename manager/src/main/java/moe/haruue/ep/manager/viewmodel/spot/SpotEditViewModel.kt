package moe.haruue.ep.manager.viewmodel.spot

import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.toPriceString
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository
import rx.android.schedulers.AndroidSchedulers
import kotlin.math.roundToInt

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SpotEditViewModel : SpotViewModel() {

    val idError = mutableLiveDataOf<String>()
    val locationError = mutableLiveDataOf<String>()
    val priceError = mutableLiveDataOf<String>()
    val priceText = mutableLiveDataOf(price.value?.toString())

    val ok = mutableLiveDataOf(false)

    private val PRICE_TEXT_OBSERVER = { it: Double? ->
        priceText.postValue(it?.toPriceString())
    }

    init {
        price.observeForever(PRICE_TEXT_OBSERVER)
    }

    override fun onCleared() {
        super.onCleared()
        price.removeObserver(PRICE_TEXT_OBSERVER)
    }

    fun onSelectCarType(type: Int) {
        this.type.postValue(type)
    }

    fun addSpot() {
        progress.postValue(1)
        val spotId = id.value
        if (spotId.isNullOrBlank()) {
            idError.postValue("停车位编号不能为空")
            progress.postValue(0)
            return
        }
        progress.postValue(2)
        val location = location.value
        if (location.isNullOrBlank()) {
            locationError.postValue("停车位位置不能为空")
            progress.postValue(0)
            return
        }
        progress.postValue(3)
        val priceText = priceText.value
        if (priceText.isNullOrBlank()) {
            priceError.postValue("时价不能为空")
            progress.postValue(0)
            return
        }
        progress.postValue(4)
        var price = priceText!!.toDoubleOrNull()
        if (price == null) {
            priceError.postValue("价格格式不合法")
            progress.postValue(0)
            return
        }
        progress.postValue(5)
        price = price.times(100).roundToInt().toDouble().div(100)
        this.price.value = price
        this.priceText.value = price.toPriceString()
        val type = type.value
        progress.postValue(6)
        ManagerRepository.receive { manager ->
            progress.postValue(10)
            if (manager == null) {
                needLogin.postValue(true)
            } else {
                MainAPIService.with { it.addSpot(manager.id, manager.password, spotId!!, location!!, price, type!!) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .apiSubscribe("SpotEditViewModel#addSpot") {
                            onStart = {
                                progress.postValue(30)
                            }
                            onNext = {
                                ok.postValue(true)
                                progress.postValue(70)
                            }
                            onAPIError = {
                                if (it.code == 401 || it.errno == 70001 /*noSuchLot*/) {
                                    needLogin.postValue(true)
                                } else {
                                    error.postValue(it.message)
                                }
                            }
                            onFinally = {
                                progress.postValue(100)
                            }
                        }
            }
        }

    }


}