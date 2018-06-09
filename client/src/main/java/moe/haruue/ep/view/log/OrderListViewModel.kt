package moe.haruue.ep.view.log

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.mutableObservableList
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers
import android.util.Log as ALog

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderListViewModel : ViewModel() {

    companion object {
        const val TAG = "OrderListVM"
    }

    val orders = mutableObservableList<Log>()

    val refreshing = mutableLiveDataOf(false)
    val toast = mutableLiveDataOf<String>()
    val needLogin = mutableLiveDataOf(false)

    fun onRefresh() {
        refreshing.value = true
        MainAPIService.with { it.lotParkList() }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe(TAG) {
                    onNext = {
                        orders.clear()
                        orders.addAll(it.data)
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            needLogin.postValue(true)
                        }
                        toast.postValue(it.message)
                    }
                    onNetworkError = {
                        toast.postValue("网络错误: ${it.localizedMessage}。 请检查网络连接，然后重试。")
                    }
                    onOtherError = {
                        toast.postValue("未知错误: ${it.localizedMessage}。 请重试。")
                    }
                    onFinally = {
                        refreshing.value = false
                    }
                }
    }


}