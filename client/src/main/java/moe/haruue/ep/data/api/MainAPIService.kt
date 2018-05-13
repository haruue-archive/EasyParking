package moe.haruue.ep.data.api

import moe.haruue.ep.BuildConfig
import moe.haruue.ep.common.data.api.APIResult
import moe.haruue.ep.common.data.api.APIServiceHolder
import moe.haruue.ep.common.model.Car
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.model.Member
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
interface MainAPIService {

    companion object
        : APIServiceHolder<MainAPIService>(MainAPIService::class.java, BuildConfig.SERVER_URL)

    @POST("account/login")
    @FormUrlEncoded
    fun accountLogin(@Field("username") username: String,
                     @Field("password") password: String,
                     @Field("persist") persist: Boolean = true): Observable<APIResult<Member>>

    @POST("account/logout")
    fun accountLogout(): Observable<APIResult<Nothing>>

    @POST("account/refresh")
    fun accountRefresh(): Observable<APIResult<Member>>

    @POST("account/info")
    fun accountInfo(): Observable<APIResult<Member>>

    @POST("account/register")
    @FormUrlEncoded
    fun accountRegister(@Field("username") username: String,
                        @Field("password") password: String): Observable<APIResult<Member>>

    @POST("account/update/email")
    @FormUrlEncoded
    fun accountUpdateEmail(@Field("email") email: String): Observable<APIResult<Member>>

    @POST("account/update/info")
    @FormUrlEncoded
    fun accountUpdateInfo(@Field("what") what: String,
                          @Field("value") value: String): Observable<APIResult<Member>>

    @POST("account/update/password")
    @FormUrlEncoded
    fun accountUpdatePassword(@Field("old") old: String,
                              @Field("new") new: String): Observable<APIResult<Nothing>>

    @POST("account/car/add")
    @FormUrlEncoded
    fun accountCarAdd(@Field("id") id: String,
                      @Field("type") type: String): Observable<APIResult<Member>>

    @POST("account/car/delete")
    @FormUrlEncoded
    fun accountCarDelete(@Field("id") id: String): Observable<APIResult<Member>>

    @POST("account/car/info")
    @FormUrlEncoded
    fun accountCarInfo(@Field("id") id: String): Observable<APIResult<Car>>

    @POST("lot/info")
    @FormUrlEncoded
    fun lotInfo(@Field("id") id: String): Observable<APIResult<Lot>>

    @POST("lot/query/geographic")
    @FormUrlEncoded
    fun lotQueryGeographic(@Field("longitude") longitude: Double,
                           @Field("latitude") latitude: Double,
                           @Field("city") city: String = ""): Observable<APIResult<List<Lot>>>

    @POST("lot/query/name")
    @FormUrlEncoded
    fun lotQueryName(@Field("name") name: String): Observable<APIResult<List<Lot>>>

    @POST("lot/park/order")
    @FormUrlEncoded
    fun lotParkOrder(@Field("carId") carId: String,
                     @Field("lotId") lotId: String,
                     @Field("spotId") spotId: String): Observable<APIResult<Log>>

    @POST("lot/park/park")
    @FormUrlEncoded
    fun lotParkPark(@Field("logId") logId: String,
                    @Field("token") token: String): Observable<APIResult<Log>>

    @POST("lot/park/remove")
    @FormUrlEncoded
    fun lotParkRemove(@Field("logId") logId: String,
                      @Field("token") token: String): Observable<APIResult<Log>>

    @POST("lot/park/cancel")
    @FormUrlEncoded
    fun lotParkCancel(@Field("logId") logId: String): Observable<APIResult<Log>>

}