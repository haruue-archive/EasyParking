package moe.haruue.ep.manager.view

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import moe.haruue.ep.common.model.Spot
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.ActivitySpotEditBinding
import moe.haruue.ep.manager.viewmodel.spot.SpotEditViewModel
import moe.haruue.util.kotlin.startActivity

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SpotEditActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SPOT = "spot"
    }

    lateinit var binding: ActivitySpotEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val spot = intent.getParcelableExtra<Spot>(EXTRA_SPOT)

        binding = DataBindingUtil.setContentView<ActivitySpotEditBinding>(this,
                R.layout.activity_spot_edit).apply {
            m = ViewModelProviders.of(this@SpotEditActivity)[SpotEditViewModel::class.java].apply {
                spot?.let { data = it }
                needLogin.observe(this@SpotEditActivity::getLifecycle) {
                    if (it == true) {
                        toast("请先登录")
                        startActivity<LoginActivity>()
                        finish()
                    }
                }
                error.observe(::getLifecycle) {
                    it?.run { toast(it) }
                }
                ok.observe(::getLifecycle) {
                    if (it == true) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        setResult(Activity.RESULT_CANCELED)
                    }
                }
            }
            setLifecycleOwner(this@SpotEditActivity::getLifecycle)
            toolbar.title = spot?.let { "编辑停车位信息" } ?: "创建停车位信息"
            spot?.run { idEdit.isEnabled = false }
            setSupportActionBar(toolbar)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.spot_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_save -> {
            binding.m?.addSpot()
            true
        }
        else -> false
    }

}
