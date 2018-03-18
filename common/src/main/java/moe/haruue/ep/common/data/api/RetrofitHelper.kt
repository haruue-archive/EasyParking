package moe.haruue.ep.common.data.api

import android.content.Context
import moe.haruue.ep.common.data.cookie.FileCookieJar
import moe.haruue.ep.common.util.ApplicationContextHandler
import moe.haruue.ep.common.util.debug
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
private lateinit var client: OkHttpClient
private var retrofitMap = hashMapOf<String, Retrofit>()

private fun buildClient(context: Context) {
    if (!::client.isInitialized) {
        client = with(OkHttpClient.Builder()) {
            cookieJar(FileCookieJar(context))
            followRedirects(true)
            followSslRedirects(true)
            readTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            connectTimeout(10, TimeUnit.SECONDS)
            debug(context) {
                addInterceptor(HttpLoggingInterceptor())
            }
            build()
        }
    }
}

private fun buildRetrofit(context: Context,
                          baseUrl: String,
                          init: Retrofit.Builder.() -> Unit = {}) = with(Retrofit.Builder()) {
    buildClient(context)
    client(client)
    baseUrl(baseUrl)
    addConverterFactory(GsonConverterFactory.create())
    addCallAdapterFactory(RxJavaCallAdapterFactory.create())
    init()
    build()
}

fun <T> createRetrofitService(context: Context,
                      service: Class<T>,
                      baseUrl: String,
                      retrofitInit: Retrofit.Builder.() -> Unit = {}): T {
    var retrofit = retrofitMap[baseUrl]
    if (retrofit == null) {
        retrofit = buildRetrofit(context, baseUrl, retrofitInit)
        retrofitMap[baseUrl] = retrofit
    }
    return retrofit!!.create(service)
}

open class APIServiceHolder<S>(
        val clazz: Class<S>,
        val baseUrl: String
) {
    private var v1: S? = null

    @Suppress("MemberVisibilityCanBePrivate")
    fun getServiceSync(context: Context? = null): S {
        ApplicationContextHandler.context = context
        if (v1 == null) {
            synchronized(this) {
                if (v1 == null) {
                    v1 = createRetrofitService(context!!.applicationContext,
                            clazz, baseUrl)
                }
            }
        }
        return v1!!
    }

    fun <T> with(context: Context? = null, block: (service: S) -> Observable<T>): Observable<T> {
        return Observable.unsafeCreate<S> {
            try {
                it.onNext(getServiceSync(context))
            } catch (e: Throwable) {
                it.onError(e)
            }
            it.onCompleted()
        }.flatMap { block(it) }.subscribeOn(Schedulers.io())
    }
}