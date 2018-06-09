package moe.haruue.ep.view.car

import android.arch.lifecycle.ViewModel
import android.util.Log
import moe.haruue.ep.common.data.api.APIError
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Car
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.mutableObservableList
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.view.account.MemberRepository
import rx.android.schedulers.AndroidSchedulers
import java.io.IOException

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class CarListViewModel : ViewModel() {

    companion object {
        const val TAG = "CarListVM"
    }

    val cars = mutableObservableList<Car>()

    val refreshing = mutableLiveDataOf(false)
    val toast = mutableLiveDataOf<String>()
    val needLogin = mutableLiveDataOf(false)

    fun onRefresh(fetch: Boolean = false) {
        refreshing.value = true
        MemberRepository.with(fetch) { member, hasError, needReLogin, message, error ->
            if (!hasError) {
                cars.clear()
                cars.addAll(member.cars)
            } else {
                toast.postValue(message)
                if (needReLogin) {
                    needLogin.postValue(true)
                }
                Log.d(TAG, "onRefresh: error in fetch member info", error)
            }
            refreshing.value = false
        }
    }

    fun onRemoveCar(carId: String) {
        MainAPIService.with { it.accountCarDelete(carId) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("$TAG#onRemoveCar") {
                    onNext = {
                        MemberRepository.update(it.data)
                        cars.removeAll { it.id == carId }
                    }
                    onAPIError = ::onAPIError
                    onNetworkError = ::onNetworkError
                    onOtherError = ::onOtherError
                    onError = {
                        val index = cars.indexOfFirst { it.id == carId }
                        cars[index] = cars[index]
                    }
                }
    }

    fun onAPIError(error: APIError) {
        if (error.code == 401) {
            needLogin.postValue(true)
        }
        toast.postValue(error.message)
    }
    fun onNetworkError(error: IOException) {
        Log.w(TAG, "onNetworkError", error)
        toast.postValue("网络错误: ${error.localizedMessage}。请检查网络连接，并重试。")
    }
    fun onOtherError(error: Throwable) {
        Log.e(TAG, "onOtherError", error)
        toast.postValue("未知错误: ${error.localizedMessage}。请重试。")
    }

}