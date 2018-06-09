package moe.haruue.ep.view.email

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.view.account.MemberRepository
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class ModifyEmailViewModel : ViewModel() {


    val email = mutableLiveDataOf("")
    val emailError = mutableLiveDataOf<String>()

    val confirmEnable = mutableLiveDataOf(false)

    val progress = mutableLiveDataOf(0)
    val status = mutableLiveDataOf("")
    val confirmed = mutableLiveDataOf(false)

    val needLogin = mutableLiveDataOf(false)

    fun onCreate() {
        MemberRepository.with { member, hasError, needReLogin, message, error ->
            if (!hasError) {
                if (member.email.isNotBlank()) {
                    email.value = member.email
                }
            }
        }
    }

    fun checkModifyEmail() {
        progress.postValue(0)
        emailError.postValue("")
        confirmed.value = false
        if (email.value.isNullOrBlank()) {
            emailError.postValue("邮箱地址不能为空")
            return
        }
        status.postValue("正在与服务器通信...")
        progress.postValue(30)
        MainAPIService.with { it.accountUpdateEmail(email.value!!) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LoginViewModel#checkLogin") {
                    onNext = {
                        MemberRepository.update(it.data)
                        confirmed.postValue(true)
                    }
                    onComplete = {
                        progress.postValue(100)
                        status.postValue("绑定邮箱成功，正在同步数据...")
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            needLogin.postValue(true)
                        } else {
                            when (it.ref) {
                                "email" -> emailError.postValue(it.message)
                                else -> status.postValue(it.message)
                            }
                        }
                        status.postValue("绑定邮箱失败: ${it.message}\n请重试")
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