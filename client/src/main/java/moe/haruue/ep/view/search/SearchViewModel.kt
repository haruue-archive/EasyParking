package moe.haruue.ep.view.search

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.model.LatLng
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.util.mutableLiveDataOf
import moe.haruue.ep.common.util.mutableObservableList
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.model.toLatLng
import moe.haruue.ep.view.main.SearchHistoryRepository
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SearchViewModel : ViewModel() {

    val keyword = mutableLiveDataOf<String>()
    val error = mutableLiveDataOf<String>()
    val result = mutableObservableList<Lot>()
    lateinit var myLocation: LatLng
    val onProgress = mutableLiveDataOf(false)

    fun doSearch() {

        onProgress.postValue(true)

        val kw = keyword.value?.trim()

        if (kw == null || kw.isBlank()) {
            error.postValue("搜索词不能为空")
            return
        }

        SearchHistoryRepository.insert(kw)

        MainAPIService.with { it.lotQueryName(kw) }
                .observeOn(Schedulers.io())
                .map {
                    val lots = it.data
                    lots.sortedBy {
                        val location = it.geographic.toLatLng()
                        AMapUtils.calculateLineDistance(myLocation, location)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("SearchViewModel#doSearch()") {
                    onNext = {
                        result.clear()
                        result.addAll(it)
                    }
                    onAPIError = {
                        error.postValue(it.message)
                        Log.w("MAPI", "SearchViewModel#doSearch(): $it")
                    }
                    onNetworkError = {
                        error.postValue("网络连接失败: ${it.localizedMessage}， 请检查网络连接。")
                        Log.d("MAPI", "SearchViewModel#doSearch()", it)
                    }
                    onOtherError = {
                        error.postValue("搜索时发生未知错误: ${it.message}")
                        Log.e("MAPI", "SearchViewModel#doSearch()", it)
                    }
                    onFinally = {
                        onProgress.postValue(false)
                    }
                }


    }

}