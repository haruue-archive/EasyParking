package moe.haruue.ep.view.order

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import moe.haruue.ep.R
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.databinding.ActivityOrderBinding

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LOT = "lot"
        const val EXTRA_MAP_SCREENSHOT = "map_screenshot"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        val lot = intent.getParcelableExtra<Lot>(EXTRA_LOT)!!
        val mapScreenshotBitmap = intent.getByteArrayExtra(EXTRA_MAP_SCREENSHOT)?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }

        val binding = DataBindingUtil.setContentView<ActivityOrderBinding>(this, R.layout.activity_order).apply {
            m = ViewModelProviders.of(this@OrderActivity)[OrderViewModel::class.java].apply {
                setLifecycleOwner(this@OrderActivity::getLifecycle)
                lotId.value = lot.id
                lotDescription.value = lot.description
                lotLocation.value = lot.location
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

    }

}