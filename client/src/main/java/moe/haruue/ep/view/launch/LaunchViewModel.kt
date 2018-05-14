package moe.haruue.ep.view.launch

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LaunchViewModel : ViewModel() {

    val id = mutableLiveDataOf<String>()
    val progress = mutableLiveDataOf(0)
    val status = mutableLiveDataOf("请稍候...")

    val confirmed = mutableLiveDataOf(false)
    val needLogin = mutableLiveDataOf(false)
    val needRetry = mutableLiveDataOf(false)

    var onCheckLocationPermission: (cb: (granted: Boolean) -> Unit) -> Unit = {
        error("Please set LaunchViewModel#onCheckLocationPermission() and check permission in callback")
    }

    fun onCreate() {
        checkLogin()
    }

    fun setNeedLogin() {
        needLogin.postValue(true)
    }

    fun checkLogin() {
        confirmed.value = false
        needLogin.value = false
        needRetry.value = false
        progress.postValue(10)
        status.postValue("检查权限...")
        onCheckLocationPermission {
            if (it) {
                status.postValue("正在与服务器通讯...")
                MainAPIService.with { it.accountRefresh() }
                        .observeOn(AndroidSchedulers.mainThread())
                        .apiSubscribe("LaunchViewModel#checkLogin") {
                            onNext = {
                                confirmed.postValue(true)
                            }
                            onComplete = {
                                status.postValue("登录成功...")
                            }
                            onAPIError = {
                                if (it.code == 401) {
                                    needLogin.postValue(true)
                                    status.postValue("正在跳转到登录界面...")
                                } else {
                                    needRetry.postValue(true)
                                    status.postValue("身份认证出错: ${it.message}\n请点击下方按钮登录或重试。")
                                }
                            }
                            onNetworkError = {
                                needRetry.postValue(true)
                                status.postValue("网络错误: ${it.localizedMessage}\n请检查网络连接，然后点击下方按钮重试。")
                            }
                            onOtherError = {
                                needRetry.postValue(true)
                                status.postValue("未知错误: ${it.localizedMessage}\n请点击下方按钮登录或重试。")
                            }
                            onFinally = {
                                progress.postValue(100)
                            }
                        }
            } else {
                status.postValue("授权遭拒：没有位置权限本应用将无法运行！\n请授予权限，然后点击下方按钮重试。")
                progress.postValue(100)
                needRetry.postValue(true)
            }
        }
    }


}
