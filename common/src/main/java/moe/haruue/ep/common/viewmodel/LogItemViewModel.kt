package moe.haruue.ep.common.viewmodel

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.model.Member
import moe.haruue.ep.common.util.mutableLiveDataOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LogItemViewModel : ViewModel() {
    val id = mutableLiveDataOf<String>()
    val member = mutableLiveDataOf<Member>()
    val carId = mutableLiveDataOf<String>()
    val lot = mutableLiveDataOf<Lot>()
    val spotId = mutableLiveDataOf<String>()
    val createTime = mutableLiveDataOf(0L)
    val startTime = mutableLiveDataOf(0L)
    val endTime = mutableLiveDataOf(0L)
    val status = mutableLiveDataOf<Int>()
    val statusText = mutableLiveDataOf<String>()

    var data: Log? = null
        set(value) {
            field = value
            id.postValue(field?.id)
            member.postValue(field?.member)
            carId.postValue(field?.carId)
            lot.postValue(field?.lot)
            spotId.postValue(field?.spotId)
            createTime.postValue(field?.createTime ?: 0)
            startTime.postValue(field?.startTime ?: 0)
            endTime.postValue(field?.endTime ?: 0)
            status.postValue(field?.status)
            statusText.postValue(field?.statusText)
        }
}