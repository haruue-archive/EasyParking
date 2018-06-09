package moe.haruue.ep.view.oldPassword

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class ModifyPasswordViewModel : ViewModel() {


    val oldPassword = mutableLiveDataOf("")
    val oldPasswordError = mutableLiveDataOf<String>()
    val newPassword = mutableLiveDataOf("")
    val newPasswordError = mutableLiveDataOf<String>()

    val confirmEnable = mutableLiveDataOf(false)

    val progress = mutableLiveDataOf(0)
    val status = mutableLiveDataOf("")
    val confirmed = mutableLiveDataOf(false)

    val needLogin = mutableLiveDataOf(false)

    fun checkModifyPassword() {
        progress.postValue(0)
        oldPasswordError.postValue("")
        newPasswordError.postValue("")
        confirmed.value = false
        if (oldPassword.value.isNullOrBlank()) {
            oldPasswordError.postValue("原密码不能为空")
            return
        }
        if (newPassword.value.isNullOrBlank()) {
            newPasswordError.postValue("新密码不能为空")
            return
        }
        status.postValue("正在与服务器通信...")
        progress.postValue(30)
        MainAPIService.with { it.accountUpdatePassword(oldPassword.value!!, newPassword.value!!) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LoginViewModel#checkLogin") {
                    onNext = {
                        confirmed.postValue(true)
                    }
                    onComplete = {
                        progress.postValue(100)
                        status.postValue("修改密码成功，正在同步数据...")
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            needLogin.postValue(true)
                        } else {
                            when (it.ref) {
                                "password" -> {
                                    if (it.errno == 40004 /*passwordError*/) {
                                        oldPasswordError.postValue("原密码错误")
                                    } else {
                                        newPasswordError.postValue(it.message)
                                    }
                                }
                                else -> status.postValue(it.message)
                            }
                        }
                        status.postValue("修改密码失败: ${it.message}\n请重试")
                    }
                    onNetworkError = {
                        status.postValue("网络错误: ${it.localizedMessage}\n请检查网络连接，然后重试")
                    }
                    onOtherError = {
                        status.postValue("未知错误: ${it.localizedMessage}\n请重试或更新应用")
                    }
                    onFinally = {
                        progress.postValue(100)
                    }
                }
    }

}