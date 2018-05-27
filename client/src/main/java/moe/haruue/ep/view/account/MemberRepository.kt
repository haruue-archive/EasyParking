package moe.haruue.ep.view.account

import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.exception.APIErrorException
import moe.haruue.ep.common.model.Member
import moe.haruue.ep.data.api.MainAPIService
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
object MemberRepository {

    lateinit var member: Member

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

        if (!refresh && ::member.isInitialized) {
            cb(member)
        }

        val errMember = Member("ERR", "ERR", "ERR", false, "0", false, listOf())

        MainAPIService.with { it.accountInfo() }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("MemberRepository#with") {
                    onNext = {
                        member = it.data
                        cb(member)
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



}