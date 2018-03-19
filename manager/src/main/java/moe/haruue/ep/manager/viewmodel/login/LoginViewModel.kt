package moe.haruue.ep.manager.viewmodel.login

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.model.Manager
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LoginViewModel : ViewModel() {

    val id = mutableLiveDataOf<String>()
    val password = mutableLiveDataOf<String>()

    val idError = mutableLiveDataOf<String?>()
    val passwordError = mutableLiveDataOf<String?>()

    val idEnable = mutableLiveDataOf(false)
    val passwordEnable = mutableLiveDataOf(false)
    val loginEnable = mutableLiveDataOf(false)

    val progress = mutableLiveDataOf(0)
    val fakeprogress = mutableLiveDataOf(30)
    val statusId = mutableLiveDataOf(0)
    val confirmed = mutableLiveDataOf(false)

    fun onCreate() {
        progress.postValue(0)
        statusId.postValue(R.string.load_cached_manager)
        ManagerRepository.receive {
            progress.postValue(30)
            if (it != null) {
                id.postValue(it.id)
            }
            progress.postValue(100)
            idEnable.postValue(true)
            passwordEnable.postValue(true)
            loginEnable.postValue(true)
            statusId.postValue(R.string.tip_enter_id_password)
        }

    }

    fun checkLogin() {
        progress.postValue(0)
        idError.postValue("")
        passwordError.postValue("")
        confirmed.value = false
        if (id.value.isNullOrBlank()) {
            idError.postValue("停车场 ID 不能为空")
            return
        }
        if (password.value.isNullOrBlank()) {
            passwordError.postValue("密码不能为空")
            return
        }
        statusId.postValue(R.string.load_connect_server)
        progress.postValue(30)
        MainAPIService.with {
            it.token(id.value!!, password.value!!, "test")
        }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LoginViewModel#checkLogin") {
                    onNext = {
                        ManagerRepository.save(Manager(id.value!!, password.value!!))
                        confirmed.postValue(true)
                    }
                    onComplete = {
                        progress.postValue(100)
                        statusId.postValue(R.string.login_success)
                    }
                    onAPIError = {
                        if (it.code == 401) {
                            passwordError.postValue(it.message)
                        } else if (it.errno == 70001 /*noSuchLot*/) {
                            idError.postValue(it.message)
                        } else {
                            when (it.ref) {
                                "lot", "id" -> idError.postValue(it.message)
                                "password" -> passwordError.postValue(it.message)
                            }
                        }
                        statusId.postValue(R.string.login_failed)
                    }
                    onNetworkError = {
                        statusId.postValue(R.string.login_failed)
                    }
                    onOtherError = {
                        statusId.postValue(R.string.login_failed)
                    }
                    onFinally = {
                        progress.postValue(100)
                    }
                }
    }
}