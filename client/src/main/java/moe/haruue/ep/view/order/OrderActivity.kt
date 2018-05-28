package moe.haruue.ep.view.order

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_order.*
import moe.haruue.ep.R
import moe.haruue.ep.common.model.Car
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.model.Spot
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.databinding.ActivityOrderBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.ep.view.account.MemberRepository
import moe.haruue.ep.view.car.AddCarActivity
import moe.haruue.ep.view.lot.LotRepository
import moe.haruue.util.kotlin.startActivity
import moe.shizuku.preference.widget.SimpleMenuPopupWindow

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LOT = "lot"
        const val EXTRA_MAP_SCREENSHOT = "map_screenshot"
    }

    lateinit var binding: ActivityOrderBinding

    val cars = mutableListOf<Car>()
    val carsSelectPopupWindow by lazy {
        SimpleMenuPopupWindow(this@OrderActivity).apply {
            setOnItemClickListener {
                when (it) {
                    cars.size -> toAddCar()
                    else -> onCarSelected(cars[it])
                }
                dismiss()
            }
        }
    }

    lateinit var lot: Lot

    val spots = mutableListOf<Spot>()

    val spotsSelectPopupWindow: SimpleMenuPopupWindow by lazy {
        SimpleMenuPopupWindow(this@OrderActivity).apply {
            setOnItemClickListener {
                onSpotSelected(spots[it])
                dismiss()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        lot = intent.getParcelableExtra<Lot>(EXTRA_LOT)!!
        val mapScreenshotBitmap = intent.getByteArrayExtra(EXTRA_MAP_SCREENSHOT)?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }

        binding = DataBindingUtil.setContentView<ActivityOrderBinding>(this, R.layout.activity_order).apply {
            m = ViewModelProviders.of(this@OrderActivity)[OrderViewModel::class.java].apply {
                setLifecycleOwner(this@OrderActivity::getLifecycle)
                lotId.value = lot.id
                lotName.value = lot.name
                lotDescription.value = lot.description
                lotLocation.value = lot.location
                lotAveragePrice = if (lot.spots.isNotEmpty()) {
                    lot.spots.sumByDouble { it.price } / lot.spots.size
                } else {
                    0.0
                }
                confirmed.observe(this@OrderActivity::getLifecycle) {
                    if (it == true) {
                        onSuccess()
                    }
                }
                needLogin.observe(this@OrderActivity::getLifecycle) {
                    if (it == true) {
                        toLogin()
                    }
                }
            }
        }

        if (mapScreenshotBitmap != null) {
            mapScreenshot.setImageBitmap(mapScreenshotBitmap)
        } else {
            mapScreenshot.visibility = View.INVISIBLE
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.apply {
            setNavigationOnClickListener { finish() }
        }

        carSelect.setOnClickListener {
            carsSelectPopupWindow.show(carSelect, carSelect.parent as View, 0)
        }

        spotSelect.setOnClickListener {
            if (spots.size > 0) {
                spotsSelectPopupWindow.show(spotSelect, spotSelect.parent as View, 0)
            } else {
                toast("暂无可用停车位")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MemberRepository.with { member, hasError, needReLogin, message, error ->
            if (!hasError) {
                cars.clear()
                cars.addAll(member.cars)
                val c = cars.map { it.id }.toMutableList()
                c.add("添加车牌号...")
                carsSelectPopupWindow.setEntries(c.toTypedArray())
                carsSelectPopupWindow.requestMeasure()
            } else {
                if (needReLogin) {
                    toLogin()
                } else {
                    toast("获取用户信息失败: $message")
                    Log.d("OrderActivity", "onResume#MemberRepo.with{}", error)
                }
            }
        }

        LotRepository.with(lot.id) { lot, hasError, message, error ->
            if (!hasError) {
                val availableSpots = lot.spots.filter { it.logId == null }
                spots.clear()
                spots.addAll(availableSpots)
                val s = availableSpots.map { it.id }
                spotsSelectPopupWindow.setEntries(s.toTypedArray())
            } else {
                toast("更新停车场信息失败: $message")
                Log.d("OrderActivity", "onResume#LotRepo.with{}", error)
            }
        }

    }

    fun toAddCar() {
        startActivity<AddCarActivity>()
    }

    fun onCarSelected(car: Car) {
        binding.m!!.car.postValue(car.id)
    }

    fun onSpotSelected(spot: Spot) {
        binding.m!!.spotId.postValue(spot.id)
        binding.m!!.spotPrice = spot.price
    }

    fun toLogin() {
        startActivity<LoginActivity>()
    }

    fun onSuccess() {
        // TODO: go to order info page here
    }

}