package moe.haruue.ep.view.order

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.util.mutableLiveDataOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderViewModel : ViewModel() {

    val car = mutableLiveDataOf("")

    val lotId = mutableLiveDataOf<String>()
    val lotDescription = mutableLiveDataOf<String>()
    val lotLocation = mutableLiveDataOf<String>()

    val spotId = mutableLiveDataOf<String>()
    val spotLocation = mutableLiveDataOf<String>()



}