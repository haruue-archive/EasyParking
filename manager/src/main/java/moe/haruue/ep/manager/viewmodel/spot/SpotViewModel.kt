package moe.haruue.ep.manager.viewmodel.spot

import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.viewmodel.SpotItemViewModel
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
open class SpotViewModel : SpotItemViewModel() {

    val log = mutableLiveDataOf<Log>()
    val progress = mutableLiveDataOf(0)
    val needLogin = mutableLiveDataOf(false)
    val error = mutableLiveDataOf<String>()

    fun onCreate() {
    }

    fun loadLog() {
        needLogin.postValue(false)
        progress.postValue(0)
        ManagerRepository.receive { manager ->
            progress.postValue(10)
            if (manager == null) {
                needLogin.postValue(true)
            } else {
                val logId = logId.value
                if (logId != null)
                MainAPIService.with { it.log(manager.id, manager.password, logId) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .apiSubscribe("SpotViewModel#loadLog") {
                            onStart = {
                                progress.postValue(30)
                            }
                            onNext = {
                                progress.postValue(70)
                                log.postValue(it.data)
                            }
                            onAPIError = {
                                if (it.code == 401 || it.errno == 70001 /*noSuchLot*/) {
                                    needLogin.postValue(true)
                                } else {
                                    error.postValue(it.message)
                                }
                            }
                            onFinally = {
                                progress.postValue(100)
                            }
                        }
            }
        }

    }

}