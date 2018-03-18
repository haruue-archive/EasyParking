@file:Suppress("MemberVisibilityCanBePrivate")

package moe.haruue.ep.common.data.subscriber

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import moe.haruue.ep.common.data.api.APIError
import moe.haruue.ep.common.util.ApplicationContextHandler
import moe.haruue.ep.common.util.debug
import moe.haruue.ep.common.util.release
import moe.haruue.ep.common.util.toast
import retrofit2.HttpException
import rx.Subscriber
import java.io.IOException

        /**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

typealias OnErrorCallback<T> = (e: T) -> Unit
typealias OnNextCallback<T> = (t: T) -> Unit
typealias OnCompleteCallback = () -> Unit


open class APISubscriber<T> private constructor(
        context: Context?,
        val where: String,
        val onStartCallback: OnCompleteCallback?,
        val onNextCallback: OnNextCallback<T>?,
        val onErrorCallback: OnErrorCallback<Throwable>?,
        val onAPIErrorCallback: OnErrorCallback<APIError>?,
        val onNetworkErrorCallback: OnErrorCallback<IOException>?,
        val onOtherErrorCallback: OnErrorCallback<Throwable>?,
        val onCompleteCallback: OnCompleteCallback?,
        val onFinallyCallback: OnCompleteCallback? = null
): Subscriber<T>() {

    init {
        ApplicationContextHandler.context = context
    }

    companion object {
        val TAG = APISubscriber::class.java.simpleName
        val gson = Gson()
    }

    class Builder<T> {
        var context: Context? = null
            set(value) {
                field = value?.applicationContext
            }
        var where: String = ""
        var onStartCallback: OnCompleteCallback? = null
        var onNextCallback: OnNextCallback<T>? = null
        var onErrorCallback: OnErrorCallback<Throwable>? = null
        var onAPIErrorCallback: OnErrorCallback<APIError>? = null
        var onNetworkErrorCallback: OnErrorCallback<IOException>? = null
        var onOtherErrorCallback: OnErrorCallback<Throwable>? = null
        var onCompleteCallback: OnCompleteCallback? = null
        var onFinallyCallback: OnCompleteCallback? = null

        fun build(): APISubscriber<T> {
            return APISubscriber(
                    context,
                    where,
                    onStartCallback,
                    onNextCallback,
                    onErrorCallback,
                    onAPIErrorCallback,
                    onNetworkErrorCallback,
                    onOtherErrorCallback,
                    onCompleteCallback,
                    onFinallyCallback
            )
        }
    }

    override fun onStart() {
        onStartCallback?.invoke() ?: logd("onStart()")
    }

    fun onAPIError(e: APIError) {
        onAPIErrorCallback?.invoke(e)
                ?: toastDebug("Unhandled APIError: code=%d, errno=%d, %s",
                        e.code, e.errno, e.message)
        logde("APIError: %s", null, e)
        if (e.ref == "toast") {
            toast(e.message)
        }
    }

    fun onNetworkError(e: IOException) {
        onNetworkErrorCallback?.invoke(e) ?: release {
            loge("Unhandled Network Error", e)
        }
        logde("Network Error", e)
        release {
            toast("网络异常，请检查网络连接${ if (e.localizedMessage != null) {
                ": " + e.localizedMessage
            } else { "" } }")
        }
        toastDebug("Network Error: %s", e)
    }

    fun onOtherError(e: Throwable) {
        onOtherErrorCallback?.invoke(e) ?: release {
            loge("Unhandled Error", e)
        }
        logde("Error", e)
        toastDebug("Error: %s", e)
    }

    override fun onNext(t: T) {
        onNextCallback?.invoke(t) ?: logd("Ignored: onNext(%s)", t)
    }

    fun onFinally() {
        onFinallyCallback?.invoke() ?: logd("onFinally()")
    }

    override fun onCompleted() {
        onCompleteCallback?.invoke() ?: logd("onComplete()")
        onFinally()
    }

    override fun onError(e: Throwable) {
        onErrorCallback?.invoke(e)
        when (e) {
            is HttpException -> onAPIError(e.readAPIError())
            is IOException -> onNetworkError(e)
            else -> onOtherError(e)
        }
        onFinally()
    }

    private fun HttpException.readAPIError(): APIError {
        val body = this.response().errorBody()
        if (body != null) {
            val bodyString = body.string()
            if (!bodyString.isNullOrBlank()) {
                val err = gson.fromJson(bodyString, APIError::class.java)
                if (err.errno > 0) {
                    return err
                }
            }
            return APIError(this.code(), "error", "服务器返回未知错误: ${code()} $bodyString", -1, "toast")
        }
        return APIError(this.code(), "error", "服务器返回未知错误: ${code()}", -1, "toast")
    }

    private fun logd(f: String, vararg args: Any?) {
        debug {
            Log.d(TAG, "$where: ${f.format(args)}")
        }
    }

    private fun logde(f: String, throwable: Throwable? = null, vararg args: Any?) {
        debug {
            loge(f.format(args), throwable)
        }
    }

    private fun loge(msg: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.e(TAG, "$where: $msg")
        } else {
            Log.e(TAG, "$where: $msg", throwable)
        }
    }

    private fun toast(msg: String) {
        ApplicationContextHandler.context?.toast(msg, Toast.LENGTH_LONG)
                ?: loge("Can't toast for context == null: $msg")
    }

    private fun toastDebug(f: String, vararg args: Any?) {
        debug {
            toast("$TAG: ${f.format(args)}")
        }
    }
}