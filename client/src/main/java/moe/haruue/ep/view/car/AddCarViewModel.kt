package moe.haruue.ep.view.car

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Car
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.view.account.MemberRepository
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class AddCarViewModel : ViewModel() {


    val car = mutableLiveDataOf("")
    val carError = mutableLiveDataOf<String>()

    val confirmEnable = mutableLiveDataOf(false)

    val progress = mutableLiveDataOf(0)
    val status = mutableLiveDataOf("")
    val confirmed = mutableLiveDataOf(false)

    val needLogin = mutableLiveDataOf(false)

    fun checkAddCar() {
        progress.postValue(0)
        carError.postValue("")
        confirmed.value = false
        if (car.value.isNullOrBlank()) {
            carError.postValue("车牌号不能为空")
            return
        }
        status.postValue("正在与服务器通信...")
        progress.postValue(30)
        MainAPIService.with { it.accountCarAdd(car.value!!, Car.TYPE_SMALL) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LoginViewModel#checkLogin") {
                    onNext = {
                        MemberRepository.member = it.data
                        confirmed.postValue(true)
                    }
                    onComplete = {
                        progress.postValue(100)
                        status.postValue("成功添加，正在同步数据...")
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            needLogin.postValue(true)
                        } else {
                            when (it.ref) {
                                "car", "carId" -> car.postValue(it.message)
                                else -> status.postValue(it.message)
                            }
                        }
                        status.postValue("添加车辆失败: ${it.message}\n请重试")
                    }
                    onNetworkError = {
                        status.postValue("网络错误: ${it.message}\n请检查网络连接，然后重试")
                    }
                    onOtherError = {
                        status.postValue("未知错误: ${it.message}\n请重试或更新应用")
                    }
                    onFinally = {
                        progress.postValue(100)
                    }
                }
    }

}