package moe.haruue.ep.manager.viewmodel.launch

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LaunchViewModel : ViewModel() {

    val id = mutableLiveDataOf<String>()
    val progress = mutableLiveDataOf(0)
    val statusId = mutableLiveDataOf(R.string.loading)

    val confirmed = mutableLiveDataOf(false)
    val needLogin = mutableLiveDataOf(false)
    val needRetry = mutableLiveDataOf(false)

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
        statusId.postValue(R.string.load_cached_manager)
        ManagerRepository.receive { manager ->
            progress.postValue(30)
            if (manager == null) {
                progress.postValue(100)
                needLogin.postValue(true)
                statusId.postValue(R.string.login_needed)
            } else {
                id.postValue(manager.id)
                statusId.postValue(R.string.load_connect_server)
                MainAPIService.with {
                    it.token(manager.id, manager.password, "test")
                }
                        .apiSubscribe("LaunchViewModel#checkLogin") {
                            onNext = {
                                confirmed.postValue(true)
                            }
                            onComplete = {
                                statusId.postValue(R.string.login_success)
                            }
                            onAPIError = {
                                if (it.code == 401 || it.errno == 70001 /*noSuchLot*/) {
                                    needLogin.postValue(true)
                                    statusId.postValue(R.string.login_needed)
                                } else {
                                    needRetry.postValue(true)
                                    statusId.postValue(R.string.login_failed)
                                }
                            }
                            onNetworkError = {
                                needRetry.postValue(true)
                                statusId.postValue(R.string.login_failed)
                            }
                            onOtherError = {
                                needRetry.postValue(true)
                                statusId.postValue(R.string.login_failed)
                            }
                            onFinally = {
                                progress.postValue(100)
                            }
                        }
            }
        }
    }

}