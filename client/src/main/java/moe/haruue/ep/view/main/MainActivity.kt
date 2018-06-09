package moe.haruue.ep.view.main

import android.app.Activity
import android.app.ActivityOptions
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.graphics.drawable.toBitmap
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.oasisfeng.condom.CondomContext
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_lot_info_sheet.*
import kotlinx.android.synthetic.main.layout_nav_header.*
import moe.haruue.ep.BuildConfig
import moe.haruue.ep.R
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.model.Member
import moe.haruue.ep.common.model.Spot
import moe.haruue.ep.common.util.hideInputMethod
import moe.haruue.ep.common.util.md5
import moe.haruue.ep.common.util.showInputMethod
import moe.haruue.ep.common.util.toPriceString
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.databinding.ActivityMainBinding
import moe.haruue.ep.model.toLatLng
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.ep.view.account.MemberRepository
import moe.haruue.ep.view.log.OrderListActivity
import moe.haruue.ep.view.order.OrderActivity
import moe.haruue.ep.view.search.SearchActivity
import moe.haruue.util.kotlin.*
import rx.android.schedulers.AndroidSchedulers
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainActivity : AppCompatActivity(), AMapLocationListener {

    companion object {
        val REQ_LOGIN = 0x1
        val REQ_SEARCH = 0x2
        val REQ_ORDER = 0x3
    }

    private object LastRefresh {
        private var latLng: LatLng? = null
        private var time: Long = 0

        fun needRefresh(latLng: LatLng) = this.latLng != latLng || System.currentTimeMillis() - time >= 60_000L

        fun refreshed(latLng: LatLng) {
            time = System.currentTimeMillis()
            this.latLng = latLng
        }

        fun reset() {
            time = 0
        }
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
        set(value) {
            field = value
            if (value) {
                runOnUiThread {
                    toast("定位中...")
                }
            }
        }

    private val navHeaderViews by lazy {
        val v = nav.getHeaderView(0)
        return@lazy object {
            val username by lazy { v.findViewById(R.id.username) as TextView }
            val email by lazy { v.findViewById(R.id.email) as TextView }
            val mobile by lazy { v.findViewById(R.id.mobile) as TextView }
            val cars by lazy { v.findViewById(R.id.cars) as TextView }
        }
    }

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            setLifecycleOwner(this@MainActivity::getLifecycle)
            m = ViewModelProviders.of(this@MainActivity)[MainViewModel::class.java].apply {

            }
        }

        map = MapView(CondomContext.wrap(this@MainActivity, "MapView")).apply {
            layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            isFocusable = true
            isFocusableInTouchMode = true
        }
        coordinator.addView(map, 0)

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(false)
        }
        toolbar.apply {
            title = ""
            subtitle = ""
            setNavigationIcon(R.drawable.ic_menu_gray_24dp)
            setNavigationOnClickListener {
                when {
                    search.hasFocus() -> map.requestFocus() /*search.removeFocus()*/
                    else -> drawer.openDrawer(Gravity.START)
                }
            }
        }
        nav.getHeaderView(0).setPadding(0, statusBarHeight, 0, 0)
        nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.map -> true
                R.id.order -> {
                    startActivity<OrderListActivity>()
                    true
                }
                R.id.account -> {
                    true
                }
                else -> false
            }
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                refreshSearchHistoryView()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        search.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val keyword = v?.text?.toString()?.trim()
                    if (keyword != null && keyword.isNotBlank()) {
                        SearchHistoryRepository.insert(keyword)
                        startActivityForResult<SearchActivity>(
                                requestCode = REQ_SEARCH,
                                options = ActivityOptions.makeSceneTransitionAnimation(
                                        this@MainActivity, toolbar, "toolbar").toBundle()) {
                            putExtra(SearchActivity.EXTRA_KEYWORD, keyword)
                            putExtra(SearchActivity.EXTRA_MY_LOCATION, locationClient.lastKnownLocation.toLatLng())
                        }
                    }
                    exitSearch()
                    v.text = ""
                    true
                }
                else -> false
            }
        }
        map.onCreate(savedInstanceState)

        toast("定位中...")

        map.map.apply {
            isTrafficEnabled = true
            mapType = AMap.MAP_TYPE_NORMAL

            uiSettings.apply {
                isMyLocationButtonEnabled = false
                isCompassEnabled = false
                isScaleControlsEnabled = true
                isZoomControlsEnabled = true
                zoomPosition = AMapOptions.ZOOM_POSITION_RIGHT_CENTER
            }

            animateCamera(CameraUpdateFactory.zoomTo(15.0f))

            setOnMarkerClickListener {
                lotMarkers.forEach { marker, _ ->
                    if (marker == it) {
                        showLotInfo(it)
                    } else {
                        it.hideInfoWindow()
                    }
                }
                true
            }

            setOnInfoWindowClickListener {
//                val lot: Lot? = lotMarkers[it]
//                lot?.run {
//                    startActivity<LotInfoActivity> {
//                        putExtra(LotInfoActivity.EXTRA_LOT, lot)
//                    }
//                }
                showLotInfo(it)
            }

            setOnMapTouchListener {
                if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }

        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

        fabMyLocation.setOnClickListener {
            locationClient.startLocation()
            moveToMyLocation = true
        }

        fabParking.setOnClickListener {
            if (bottomSheet.state != BottomSheetBehavior.STATE_HIDDEN && ::currentLotMarker.isInitialized) {
                map.map.moveCamera(CameraUpdateFactory.newLatLng(currentLotMarker.position))
                map.map.getMapScreenShot(object : AMap.OnMapScreenShotListener {
                    override fun onMapScreenShot(bm: Bitmap?) {}
                    override fun onMapScreenShot(bm: Bitmap?, status: Int) {
                        mapScreenshot.setImageBitmap(bm)
                        mapScreenshot.visibility = View.VISIBLE
                        startActivityForResult<OrderActivity>(
                                requestCode = REQ_ORDER,
                                options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity,
                                        mapScreenshot, "mapScreenshot").toBundle()) {
                            putExtra(OrderActivity.EXTRA_LOT, lotMarkers[currentLotMarker])
                            putExtra(OrderActivity.EXTRA_MAP_SCREENSHOT, bm?.let {
                                val s = ByteArrayOutputStream()
                                it.compress(Bitmap.CompressFormat.WEBP, 50, s)
                                s.toByteArray()
                            })
                        }
                    }
                })
            } else {
                // find a available park lot
                toast("正在寻找附近停车场...")
                thread(start = true) {
                    val myLocation = locationClient.lastKnownLocation.toLatLng()
                    val marker = lotMarkers.filterValues { it.spots.any { it.logId == null } }.keys.toMutableList().minBy {
                        AMapUtils.calculateLineDistance(myLocation, it.position)
                    }
                    runOnUiThread {
                        if (marker != null) {
                            showLotInfo(marker)
                        } else {
                            toast("没找到合适的停车场")
                        }
                    }
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        map.onResume()
        locationClient.startLocation()
        refreshNavHeader()
        search.post {
            exitSearch()
            search.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    enterSearch()
                } else {
                    exitSearch()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LastRefresh.reset()
    }

    private fun refreshNavHeader(forceFetch: Boolean = false) {
        MemberRepository.with(forceFetch) { member: Member, hasError: Boolean, needReLogin: Boolean, msg: String, error: Throwable? ->
            if (!hasError) {
                navHeaderViews.apply {
                    username.text = member.username
                    email.text = if (member.email.isNotBlank()) member.email else "未绑定邮箱"
                    mobile.text = if (member.mobile.isNotBlank()) member.mobile else "未绑定手机"
                    cars.text = if (member.cars.isNotEmpty()) member.cars.joinToString(separator = "\n") { it.id } else "未绑定车辆"
                    val avatarUrlToken = if (member.email.isNotBlank()) {
                        member.email.md5()
                    } else {
                        "no-such-mail-and-no-reply-afcda1c56d5156a4f5w61d1c15@caoyue.com.cn".md5()
                    }
                    Glide.with(this@MainActivity).load("https://www.gravatar.com/avatar/$avatarUrlToken?d=mp").apply(RequestOptions().apply {
                        circleCrop()
                    }).into(avatar)
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
        if (!LastRefresh.needRefresh(location)) return
        MainAPIService.with { it.lotQueryGeographic(longitude = location.longitude, latitude = location.latitude, city = city) }
                .map {
                    it.data.filter { !lotMarkers.containsValue(it) }.map {
                        it to generateMarkerOptionsForLot(it)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .apiSubscribe("MainActivity#refreshLots") {
                    onNext = {
                        val m = map.map
                        it.forEach { (lot, markerOptions) ->
                            lotMarkers[m.addMarker(markerOptions)] = lot
                        }
                        LastRefresh.refreshed(location)
                    }
                    onAPIError = {
                        toast(it.message)
                        Log.w("MAPI", "MainActivity#refreshLots: $it")
                    }
                    onNetworkError = {
                        toast("网络连接失败: ${it.localizedMessage}， 请检查网络连接。")
                        Log.d("MAPI", "MainActivity#refreshLots", it)
                    }
                    onOtherError = {
                        toast("获取停车场信息时发生未知错误: ${it.message}")
                        Log.e("MAPI", "MainActivity#refreshLots", it)
                    }
                }
    }

    fun addLotToMap(lot: Lot): Marker {
        if (lotMarkers.containsValue(lot)) {
            for ((m, l) in lotMarkers) {
                if (l == lot) {
                    return m
                }
            }
            error("lotMarkers contains $lot but we can't find it. lotMarkers: $lotMarkers")
        } else {
            val options = generateMarkerOptionsForLot(lot)
            val marker = map.map.addMarker(options)
            lotMarkers[marker] = lot
            return marker
        }
    }

    private fun generateMarkerOptionsForLot(lot: Lot): MarkerOptions {
        return MarkerOptions().apply {
            position(LatLng(lot.geographic.latitude, lot.geographic.longitude))
            title(lot.name)
            snippet(lot.location)
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

    private fun enterSearch() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_gray_24dp)
        history.visibility = View.VISIBLE
        search.showInputMethod()
        refreshSearchHistoryView()
    }

    private fun refreshSearchHistoryView() {
        SearchHistoryRepository.find(search.text?.toString() ?: "") {
            history.removeAllViews()
            it.map { createSearchHistoryItemView(it.keyword, history) }.forEach { history.addView(it) }
        }
    }

    private fun createSearchHistoryItemView(keyword: String, parent: ViewGroup): View {
        val layout = layoutInflater.inflate(R.layout.item_search_history, parent, false)
        val text = layout.findViewById<TextView>(R.id.keyword)
        text.text = keyword
        layout.setOnClickListener {
            search.setText(keyword)
            search.setSelection(keyword.length)
        }
        return layout
    }

    private fun exitSearch() {
        toolbar.setNavigationIcon(R.drawable.ic_menu_gray_24dp)
        history.visibility = View.GONE
        search.hideInputMethod()
//        search.removeFocus()
        map.requestFocus()
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
            search.hasFocus() -> {
//                search.removeFocus()
                map.requestFocus()
            }
            bottomSheet.state != BottomSheetBehavior.STATE_HIDDEN -> bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
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
            REQ_SEARCH -> {
                if (resultCode == Activity.RESULT_OK) {
                    val lot = data!!.getParcelableExtra<Lot>(SearchActivity.RESULT_LOT)
                    val marker = addLotToMap(lot)
                    showLotInfo(marker)
                }
            }
            REQ_ORDER -> {
                mapScreenshot.visibility = View.GONE
            }
        }
    }

    val bottomSheet by lazy {
        BottomSheetBehavior.from(sheet)
    }

    private lateinit var currentLotMarker: Marker

    fun showLotInfo(marker: Marker) {
        currentLotMarker = marker
        val lot = lotMarkers[marker]
        lotName.text = lot?.name
        lotDescription.text = lot?.description
        lotLocation.text = lot?.location
        lotType.text = lot?.type?.let { Spot.typeStringOf(it) } ?: "暂不可用"
        val availableSpots = lot?.spots?.filter { it.logId == null } ?: listOf()
        lotSpotCount.text = "剩余 ${availableSpots.size} 个车位"
        lotPrice.text = if (availableSpots.size > 0) {
            val price = availableSpots.sumByDouble { it.price } / availableSpots.size
            "平均每小时 ${price.toPriceString()} 元"
        } else {
            "暂不可用"
        }
        bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheet.isHideable = true
        map.map.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
        marker.showInfoWindow()
    }

}