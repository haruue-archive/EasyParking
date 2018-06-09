package moe.haruue.ep.view.account

import android.util.Log
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.exception.APIErrorException
import moe.haruue.ep.common.model.Member
import moe.haruue.ep.common.util.debug
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
object MemberRepository {

    private var member: Member? = null

    fun fetchAsync() {
        with(true) { member, _, _, _, _ ->
            debug {
                Log.d("MemberRepository", "MemberRepository#fetchAsync: member = $member")
            }
        }
    }

    fun with(
            refresh: Boolean = false,
            callback: (member: Member,
                       hasError: Boolean,
                       needReLogin: Boolean,
                       message: String,
                       error: Throwable?) -> Unit) {
        /** forward stub, because kotlin has not supported use default value above */
        fun cb(member: Member, hasError: Boolean = false, needReLogin: Boolean = false, message: String = "", error: Throwable? = null) {
            callback(member, hasError, needReLogin, message, error)
        }

        val cm = member
        if (!refresh && cm != null) {
            cb(cm)
            return
        }

        val errMember = Member("ERR", "ERR", "ERR", false, "0", false, listOf())

        MainAPIService.with { it.accountInfo() }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("MemberRepository#with") {
                    onNext = {
                        val m = it.data
                        member = m
                        cb(m)
                    }
                    onAPIError = {
                        val needReLogin = it.code == 401
                        cb(errMember, true, needReLogin, it.message, APIErrorException(it))
                    }
                    onNetworkError = {
                        cb(errMember, true, false, it.localizedMessage, it)
                    }
                    onOtherError = {
                        cb(errMember, true, false, it.localizedMessage, it)
                    }
                }

    }

    fun clear() {
        member = null
    }

    fun update(m: Member) {
        this.member = m
    }


}