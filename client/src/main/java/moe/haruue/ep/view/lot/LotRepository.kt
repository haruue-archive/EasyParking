package moe.haruue.ep.view.lot

import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.exception.APIErrorException
import moe.haruue.ep.common.model.GeographicLocation
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
object LotRepository {

    fun with(id: String, callback: (lot: Lot, hasError: Boolean, message: String, error: Throwable?) -> Unit) {
        /** forward stub, because kotlin has not supported use default value above */
        fun cb(lot: Lot, hasError: Boolean = false, message: String = "", error: Throwable? = null) {
            callback(lot, hasError,  message, error)
        }

        val errLot = Lot("ERR", "ERR", "ERR", "ERR", "ERR", GeographicLocation(0.0, 0.0), 0, listOf())

        MainAPIService.with { it.lotInfo(id) }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("LotRepository#with") {
                    onNext = {
                        cb(it.data)
                    }
                    onAPIError = {
                        cb(errLot, true, it.message, APIErrorException(it))
                    }
                    onNetworkError = {
                        cb(errLot, true, it.localizedMessage, it)
                    }
                    onOtherError = {
                        cb(errLot, true, it.localizedMessage, it)
                    }
                }

    }

}