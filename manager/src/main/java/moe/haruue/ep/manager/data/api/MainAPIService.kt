package moe.haruue.ep.manager.data.api

import moe.haruue.ep.common.data.api.APIResult
import moe.haruue.ep.common.data.api.APIServiceHolder
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.manager.BuildConfig
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 * main api service
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
interface MainAPIService {

    companion object
        : APIServiceHolder<MainAPIService>(MainAPIService::class.java, BuildConfig.SERVER_URL)

    @POST("lot/manage/token")
    @FormUrlEncoded
    fun token(@Field("id") id: String,
              @Field("password") password: String,
              @Field("extra") extra: String): Observable<APIResult<String>>

    @POST("lot/manage/log")
    @FormUrlEncoded
    fun log(@Field("id") id: String,
              @Field("password") password: String,
              @Field("logId") logId: String): Observable<APIResult<Log>>

    @POST("lot/info")
    @FormUrlEncoded
    fun info(@Field("id") id: String): Observable<APIResult<Lot>>

    @POST("lot/manage/spot/add")
    @FormUrlEncoded
    fun addSpot(@Field("id") id: String,
                @Field("password") password: String,
                @Field("spotId") spotId: String,
                @Field("location") location: String,
                @Field("price") price: Double,
                @Field("type") type: Int): Observable<APIResult<Lot>>

    @POST("lot/manage/spot/remove")
    @FormUrlEncoded
    fun removeSpot(@Field("id") id: String,
                   @Field("password") password: String,
                   @Field("spotId") spotId: String): Observable<APIResult<Lot>>

    @POST("lot/manage/spot/status")
    @FormUrlEncoded
    fun updateSpotStatus(@Field("id") id: String,
                         @Field("password") password: String,
                         @Field("spotId") spotId: String,
                         @Field("status") newStatus: String): Observable<APIResult<Lot>>

}
