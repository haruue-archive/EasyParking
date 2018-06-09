package moe.haruue.ep.view.log

import android.arch.lifecycle.ViewModel
import android.net.Uri
import moe.haruue.ep.common.data.api.APIError
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.util.formatToDateTime
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.toPriceString
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.view.account.MemberRepository
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import android.util.Log as ALog

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderInfoViewModel : ViewModel() {

    companion object {
        const val TAG = "OrderInfoVM"
    }

    val id = mutableLiveDataOf<String>()
    val carId = mutableLiveDataOf<String>()
    val lotName = mutableLiveDataOf<String>()
    val lotLocation = mutableLiveDataOf<String>()
    val spotId = mutableLiveDataOf<String>()
    val spotLocation = mutableLiveDataOf<String>()
    var createTime: Long? = null
        set(value) {
            field = value
            createTimeText.postValue(createTime?.formatToDateTime() ?: "不可用")
        }
    val createTimeText = mutableLiveDataOf<String>()
    var startTime: Long? = null
        set(value) {
            field = value
            startTimeText.postValue(startTime?.formatToDateTime() ?: "不可用")
        }
    val startTimeText = mutableLiveDataOf<String>()
    var endTime: Long? = null
        set(value) {
            field = value
            endTimeText.postValue(endTime?.formatToDateTime() ?: "不可用")
        }
    val endTimeText = mutableLiveDataOf<String>()
    var price: Double = 0.0
        set(value) {
            field = value
            priceText.postValue("${value.toPriceString()} 元/小时")
        }
    val priceText = mutableLiveDataOf<String>()
    var fee: Double? = 0.0
        set(value) {
            field = value
            feeText.postValue(value?.let { "${value.toPriceString()} 元" } ?: "暂不可用")
        }
    val feeText = mutableLiveDataOf<String>()
    val status = mutableLiveDataOf<Int>()
    val statusText = mutableLiveDataOf<String>()
    val paid = mutableLiveDataOf<Boolean>()

    val refreshing = mutableLiveDataOf(false)
    val progress = mutableLiveDataOf(0)
    val needLogin = mutableLiveDataOf(false)
    val toast = mutableLiveDataOf<String>()
    val fatal = mutableLiveDataOf(false)
    val dialogConfirm = mutableLiveDataOf<Pair<String, () -> Unit>>()
    val scanQrCode = mutableLiveDataOf<Pair<String, (String) -> Unit>>()

    fun setLog(log: Log) {
        id.value = log.id
        carId.value = log.carId
        lotName.value = log.lot.name
        lotLocation.value = log.lot.location
        spotId.value = log.spotId
        spotLocation.value = log.spot?.location ?: "暂不可用"
        createTime = log.createTime
        startTime = log.startTime
        endTime = log.endTime
        price = log.price
        fee = log.fee
        status.value = log.status
        statusText.value = log.statusText
        paid.value = log.paid
    }

    fun onRefresh() {
        refreshing.postValue(true)
        MemberRepository.fetchAsync()
        MainAPIService.with { it.lotParkRefresh(id.value!!) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("OrderInfoActivity") {
                    onNext = {
                        setLog(it.data)
                    }
                    onAPIError = ::onAPIError
                    onNetworkError = ::onNetworkError
                    onOtherError = ::onOtherError
                    onFinally = {
                        refreshing.postValue(false)
                        progress.value = 100
                    }
                }
    }

    fun onCancelOrder() {
        progress.value = 0
        dialogConfirm.value = "取消订单仍然会收取等待时间所造成的费用，轻触「确定」以继续。" to {
            progress.value = 30
            MainAPIService.with { it.lotParkCancel(id.value!!) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .apiSubscribe("$TAG#onCancelOrder") {
                        onNext = {
                            setLog(it.data)
                            progress.value = 80
                        }
                        onAPIError = ::onAPIError
                        onNetworkError = ::onNetworkError
                        onOtherError = ::onOtherError
                        onFinally = {
                            onRefresh()
                        }
                    }
            Unit
        }
    }

    fun onPark() {
        progress.value = 1
        scanQrCode.value = "请扫描停车二维码" to { qrString ->
            progress.value = 10
            Observable.just(qrString)
                    .map {
                        try {
                            val token = Uri.parse(qrString).getQueryParameter("token")
                            if (token.isNullOrBlank()) {
                                throw MalformedQrCodeException("can't obtain valid token from qrCode($qrString)", NullPointerException("token == null"))
                            }
                            return@map token
                        } catch (e: Exception) {
                            throw MalformedQrCodeException("can't parse uri from qrCode($qrString)", e)
                        }
                    }
                    .flatMap { token ->
                        MainAPIService.with { it.lotParkPark(id.value!!, token) }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .apiSubscribe("$TAG#onPark") {
                        onNext = {
                            progress.value = 80
                            setLog(it.data)
                        }
                        onAPIError = ::onAPIError
                        onNetworkError = ::onNetworkError
                        onOtherError = ::onOtherError
                        onFinally = {
                            onRefresh()
                        }
                    }
        }
    }

    fun onRemove() {
        progress.value = 1
        scanQrCode.value = "请扫描取车二维码" to { qrString ->
            progress.value = 10
            Observable.just(qrString)
                    .map {
                        Uri.parse(qrString).getQueryParameter("token")
                    }
                    .flatMap { token ->
                        MainAPIService.with { it.lotParkRemove(id.value!!, token) }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .apiSubscribe("$TAG#onPark") {
                        onNext = {
                            progress.value = 80
                            setLog(it.data)
                        }
                        onAPIError = ::onAPIError
                        onNetworkError = ::onNetworkError
                        onOtherError = ::onOtherError
                        onFinally = {
                            onRefresh()
                        }
                    }
        }
    }

    fun onPay() {
        toast.postValue("暂不能Pay")
    }

    fun onAPIError(error: APIError) {
        if (error.code == 401) {
            needLogin.postValue(true)
        }
        toast.postValue(error.message)
        if (error.errno == 90001 /*noSuchLog*/) {
            fatal.postValue(true)
        }
    }
    fun onNetworkError(error: IOException) {
        ALog.w(TAG, "onNetworkError", error)
        toast.postValue("网络错误: ${error.localizedMessage}。请检查网络连接，并重试。")
    }
    fun onOtherError(error: Throwable) {
        ALog.e(TAG, "onOtherError", error)
        when(error) {
            is MalformedQrCodeException -> toast.postValue("二维码无效")
            else -> toast.postValue("未知错误: ${error.localizedMessage}。请重试。")
        }
    }
    private class MalformedQrCodeException(msg: String, cause: Throwable) : RuntimeException(msg, cause)

}