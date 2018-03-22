package moe.haruue.ep.manager.viewmodel.main

import android.arch.lifecycle.ViewModel
import moe.haruue.ep.common.util.mutableLiveDataOf

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainViewModel : ViewModel() {

    val page = mutableLiveDataOf(0)

    fun onCreate() {

    }

}