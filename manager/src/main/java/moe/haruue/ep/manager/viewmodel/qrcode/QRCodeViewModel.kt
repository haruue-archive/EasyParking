package moe.haruue.ep.manager.viewmodel.qrcode

import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.manager.BuildConfig
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository
import rx.android.schedulers.AndroidSchedulers
import java.net.URLEncoder

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class QRCodeViewModel(
        ext: String
) : ViewModel() {

    val title = mutableLiveDataOf("")
    val extra = mutableLiveDataOf(ext)
    val progress = mutableLiveDataOf(0)
    val error = mutableLiveDataOf<String>()
    val loginFailed = mutableLiveDataOf(false)
    val allowAutoNext = mutableLiveDataOf(false)
    val qrcode = mutableLiveDataOf<Bitmap>()

    fun onCreate() {
        updateQRCode()
    }

    fun updateQRCode() {
        progress.postValue(0)
        allowAutoNext.postValue(false)
        error.postValue("")
        ManagerRepository.receive { manager ->
            progress.postValue(10)
            if (manager == null) {
                onLoginFailed()
            } else {
                MainAPIService.with { it.token(manager.id, manager.password, extra.value!!) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            progress.postValue(70)
                            if (!it.message.isBlank()) {
                                val encoder = BarcodeEncoder()
                                val content = "${BuildConfig.SERVER_URL}download?token=${URLEncoder.encode(it.data, "UTF-8")}"
                                return@map encoder.encodeBitmap(
                                        content,
                                        BarcodeFormat.QR_CODE, 400, 400)
                            }
                            error("api returned a empty token")
                        }
                        .apiSubscribe("QRCodeViewModel#updateQRCode") {
                            onNext = {
                                qrcode.postValue(it)
                            }
                            onComplete = {
                                allowAutoNext.postValue(true)
                            }
                            onAPIError = {
                                if (it.code == 401 || it.errno == 70001 /*noSuchLot*/) {
                                    onLoginFailed()
                                } else {
                                    error.postValue("错误: ${it.message}")
                                }
                            }
                            onNetworkError = {
                                error.postValue("网络错误: ${it.localizedMessage}")
                            }
                            onOtherError = {
                                error.postValue("未知错误: ${it.localizedMessage}")
                            }
                            onError = {
                                allowAutoNext.postValue(false)
                            }
                            onFinally = {
                                progress.postValue(100)
                            }
                        }
            }
        }
    }

    fun onLoginFailed() {
        loginFailed.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        allowAutoNext.postValue(false)
    }

}