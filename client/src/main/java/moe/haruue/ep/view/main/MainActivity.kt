package moe.haruue.ep.view.main

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.graphics.drawable.toBitmap
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import moe.haruue.ep.BuildConfig
import moe.haruue.ep.R
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.model.Member
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.databinding.ActivityMainBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.ep.view.account.MemberRepository
import moe.haruue.ep.view.lot.LotInfoActivity
import moe.haruue.util.kotlin.*
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainActivity : AppCompatActivity(), AMapLocationListener {

    companion object {
        val REQ_LOGIN = 0x1
    }

    private val locationClient by lazy {
        AMapLocationClient(this@MainActivity).apply {
            setLocationOption(AMapLocationClientOption().apply {
                locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Transport
                isOnceLocationLatest = true
                isMockEnable = BuildConfig.DEBUG
                setLocationListener(this@MainActivity)
            })
        }
    }

    private lateinit var myLocationMarker: Marker

    private val lotMarkers = mutableMapOf<Marker, Lot>()

    private var moveToMyLocation: Boolean = true  // move to my location when it first located

    private val navHeaderViews by lazy {
        val v = nav.getHeaderView(0)
        return@lazy object {
            val username by lazy { v.findViewById(R.id.username) as TextView }
            val email by lazy { v.findViewById(R.id.email) as TextView }
            val mobile by lazy { v.findViewById(R.id.mobile) as TextView }
            val cars by lazy { v.findViewById(R.id.cars) as TextView }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            setLifecycleOwner(this@MainActivity::getLifecycle)
            m = ViewModelProviders.of(this@MainActivity)[MainViewModel::class.java].apply {

            }
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(false)
        }
        toolbar.apply {
            title = ""
            subtitle = ""
            setNavigationIcon(R.drawable.ic_menu_gray_24dp)
            setNavigationOnClickListener {
                drawer.openDrawer(Gravity.START)
            }
        }
        nav.getHeaderView(0).setPadding(0, statusBarHeight, 0, 0)

        map.onCreate(savedInstanceState)

        map.map.apply {
            isTrafficEnabled = true
            mapType = AMap.MAP_TYPE_NORMAL

            uiSettings.apply {
                isMyLocationButtonEnabled = false
                isCompassEnabled = true
                isScaleControlsEnabled = true
                isZoomControlsEnabled = true
                zoomPosition = AMapOptions.ZOOM_POSITION_RIGHT_CENTER
            }

            animateCamera(CameraUpdateFactory.zoomTo(15.0f))

            setOnMarkerClickListener {
                lotMarkers.forEach { marker, _ ->
                    if (marker == it) {
                        it.showInfoWindow()
                    } else {
                        it.hideInfoWindow()
                    }
                }
                true
            }

            setOnInfoWindowClickListener {
                val lot: Lot? = lotMarkers[it]
                lot?.run {
                    startActivity<LotInfoActivity> {
                        putExtra(LotInfoActivity.EXTRA_LOT, lot)
                    }
                }
            }
        }

        BottomSheetBehavior.from(sheet).isHideable = false

        fab.setOnClickListener {
            locationClient.startLocation()
            moveToMyLocation = true
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        locationClient.startLocation()
        refreshNavHeader()
    }

    private fun refreshNavHeader(forceFetch: Boolean = false) {
        MemberRepository.with(forceFetch) { member: Member, hasError: Boolean, needReLogin: Boolean, msg: String, error: Throwable? ->
            if (!hasError) {
                navHeaderViews.apply {
                    username.text = member.username
                    email.text = if (member.email.isNotBlank()) member.email else "未绑定邮箱"
                    mobile.text = if (member.mobile.isNotBlank()) member.mobile else "未绑定手机"
                    cars.text = if (member.cars.isNotEmpty()) member.cars.joinToString(separator = "\n") else "未绑定车辆"
                }
                return@with
            } else {
                toast(msg)
                if (needReLogin) {
                    toLogin()
                } else {
                    Log.d("MAPI", "onResume: nav header info", error)
                }
            }
        }
    }

    private fun refreshLots(location: LatLng, city: String) {
        MainAPIService.with { it.lotQueryGeographic(longitude = location.longitude, latitude = location.latitude, city = city) }
                .map {
                    it.data.filter { !lotMarkers.containsValue(it) }.map {
                        it to MarkerOptions().apply {
                            position(LatLng(it.geographic.latitude, it.geographic.longitude))
                            title(it.name)
                            snippet(it.location)
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("MainActivity#refreshLots") {
                    onNext = {
                        val m = map.map
                        it.forEach { (lot, markerOptions) ->
                            lotMarkers[m.addMarker(markerOptions)] = lot
                        }
                    }
                    onAPIError = {
                        toast(it.message)
                        Log.w("MAPI", "MainActivity#refreshLogs: $it")
                    }
                    onNetworkError = {
                        toast("网络连接失败: ${it.localizedMessage}， 请检查网络连接。")
                        Log.d("MAPI", "MainActivity#refreshLogs", it)
                    }
                    onOtherError = {
                        toast("获取停车场信息时发生未知错误: ${it.message}")
                        Log.e("MAPI", "MainActivity#refreshLogs", it)
                    }
                }
    }

    override fun onLocationChanged(location: AMapLocation?) {
        if (location == null) {
            Log.d("AMAP", "MainActivity#onLocationChanged(null)")
            return
        }
        if (location.errorCode == 0) {
            location.also {

                val latLng = LatLng(it.latitude, it.longitude)

                refreshLots(latLng, location.city)

                if (::myLocationMarker.isInitialized)  {
                    myLocationMarker.position = latLng
                } else {
                    myLocationMarker = map.map.addMarker(MarkerOptions().apply {
                        position(latLng)
                        icon(BitmapDescriptorFactory.fromBitmap(
                                getDrawable(R.drawable.ic_my_location_mark).toBitmap(dp2px(20f), dp2px(20f))
                        ))
                        draggable(false)
                    })
                }

                if (moveToMyLocation) {
                    moveToMyLocation = false
                    map.map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            }
        } else {
            Log.e("AMAP", "MainActivity#onLocationChanged: errcode=${location.errorCode}, info=${location.errorInfo}")
            toast("定位失败 ${location.errorCode}: ${location.errorInfo}")
        }
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        locationClient.stopLocation()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        map.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        map.onDestroy()
        locationClient.onDestroy()
    }

    override fun onBackPressed() {
        when {
            drawer.isDrawerOpen(Gravity.START) -> drawer.closeDrawer(Gravity.START)
            else -> super.onBackPressed()
        }
    }

    fun toLogin() {
        startActivityForResult<LoginActivity>(REQ_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    refreshNavHeader(true)
                }
            }
        }
    }

}