package moe.haruue.ep.view.account

import android.arch.lifecycle.ViewModel
import android.util.Log
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class AccountViewModel : ViewModel() {


    val username = mutableLiveDataOf<String>()
    val password = mutableLiveDataOf<String>()

    val usernameError = mutableLiveDataOf<String>()
    val passwordError = mutableLiveDataOf<String>()

    val usernameEnable = mutableLiveDataOf(false)
    val passwordEnable = mutableLiveDataOf(false)
    val loginEnable = mutableLiveDataOf(false)

    val progress = mutableLiveDataOf(0)
    val status = mutableLiveDataOf("")
    val confirmed = mutableLiveDataOf(false)

    val needRegister = mutableLiveDataOf(false)

    fun checkLogin() {
        progress.postValue(0)
        usernameError.postValue("")
        passwordError.postValue("")
        confirmed.value = false
        if (username.value.isNullOrBlank()) {
            usernameError.postValue("用户名不能为空")
            return
        }
        if (password.value.isNullOrBlank()) {
            passwordError.postValue("密码不能为空")
            return
        }
        status.postValue("正在与服务器通信...")
        progress.postValue(30)
        MainAPIService.with { it.accountLogin(username.value!!, password.value!!, true) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LoginViewModel#checkLogin") {
                    onNext = {
                        confirmed.postValue(true)
                    }
                    onComplete = {
                        progress.postValue(100)
                        status.postValue("登录成功")
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            passwordError.postValue(it.message)
                        } else {
                            when (it.ref) {
                                "username" -> usernameError.postValue(it.message)
                                "password" -> passwordError.postValue(it.message)
                                else -> status.postValue("")
                            }
                        }
                        status.postValue("登录失败: ${it.message}\n请重试")
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

    fun checkRegister() {
        progress.postValue(0)
        usernameError.postValue("")
        passwordError.postValue("")
        confirmed.value = false
        if (username.value.isNullOrBlank()) {
            usernameError.postValue("用户名不能为空")
            return
        }
        if (password.value.isNullOrBlank()) {
            passwordError.postValue("密码不能为空")
            return
        }
        status.postValue("正在与服务器通信...")
        progress.postValue(30)
        MainAPIService.with { it.accountRegister(username.value!!, password.value!!) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LoginViewModel#checkRegister") {
                    onNext = {
                        confirmed.postValue(true)
                    }
                    onComplete = {
                        progress.postValue(100)
                        status.postValue("登录成功")
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            passwordError.postValue(it.message)
                        } else {
                            when (it.ref) {
                                "username" -> usernameError.postValue(it.message)
                                "password" -> passwordError.postValue(it.message)
                                else -> status.postValue("")
                            }
                        }
                        status.postValue("注册失败: ${it.message}\n请重试")
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

    fun toRegister() {
        needRegister.postValue(true)
    }

}