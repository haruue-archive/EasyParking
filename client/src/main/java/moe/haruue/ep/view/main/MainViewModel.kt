package moe.haruue.ep.view.main

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.util.mutableLiveDataOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainViewModel : ViewModel() {

    val city = mutableLiveDataOf("")
    val latitude = mutableLiveDataOf(0.0)
    val longitude = mutableLiveDataOf(0.0)



}