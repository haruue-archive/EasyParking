package moe.haruue.ep.common.data.api

import android.content.Context
import moe.haruue.ep.common.data.cookie.FileCookieJar
import moe.haruue.ep.common.util.debug
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
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