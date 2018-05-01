package moe.haruue.ep.manager.view

import android.app.Activity
import android.app.ProgressDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Spot
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.databinding.ActivitySpotBinding
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository
import moe.haruue.ep.manager.viewmodel.spot.SpotViewModel
import moe.haruue.util.kotlin.startActivity
import moe.haruue.util.kotlin.startActivityForResult
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SpotActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SPOT = "spot"
        const val REQ_EDIT = 1
    }

    lateinit var binding: ActivitySpotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val spot = intent.getParcelableExtra<Spot>(EXTRA_SPOT) ?: kotlin.run {
            finish()
            return
        }

        binding = DataBindingUtil.setContentView<ActivitySpotBinding>(this,
                R.layout.activity_spot).apply {
            m = ViewModelProviders.of(this@SpotActivity)[SpotViewModel::class.java].apply {
                data = spot
                needLogin.observe(this@SpotActivity::getLifecycle) {
                    if (it == true) {
                        toast("请先登录")
                        startActivity<LoginActivity>()
                        finish()
                    }
                }
            }
            setLifecycleOwner(this@SpotActivity::getLifecycle)
            toolbar.title = "车位信息"
            pager.apply {
                adapter = PagerAdapter(supportFragmentManager)
                tabs.setupWithViewPager(this)
            }
        }

        setSupportActionBar(toolbar)
        setResult(Activity.RESULT_CANCELED)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.spot, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        R.id.action_edit -> {
            toEdit()
            true
        }
        R.id.action_delete -> {
            doDelete()
            true
        }
        else -> false
    }

    fun toEdit() {
        startActivityForResult<SpotEditActivity>(REQ_EDIT) {
            putExtra(SpotEditActivity.EXTRA_SPOT, binding.m!!.data)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_EDIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    fun doDelete() {
        ManagerRepository.receive { manager ->
            if (manager == null) {
                binding.m!!.needLogin.postValue(true)
                return@receive
            } else {
                val progress = ProgressDialog(this@SpotActivity).apply {
                    title = "正在与服务器通信"
                }
                MainAPIService.with { it.removeSpot(manager.id, manager.password, binding.m!!.data!!.id) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .apiSubscribe("SpotActivity#doDelete") {
                            onStart = {
                                progress.show()
                            }
                            onComplete = {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            onError = {
                                onAPIError = {
                                    if (it.code == 401 || it.errno == 70001 /*noSuchLot*/) {
                                        binding.m!!.needLogin.postValue(true)
                                    } else {
                                        toast("发生错误: ${it.message}")
                                    }
                                }
                            }
                            onFinally = {
                                progress.dismiss()
                            }
                        }
            }
        }

    }

    class PagerAdapter(
            fm: FragmentManager
    ) : FragmentPagerAdapter(fm) {

        val infoFragment by lazy { SpotInfoFragment() }
        val logFragment by lazy { SpotLogFragment() }

        override fun getItem(position: Int) = when(position) {
            0 -> infoFragment
            1 -> logFragment
            else -> throw IndexOutOfBoundsException(
                    "fragment count: $count , pager position: $position")
        }

        override fun getPageTitle(position: Int): CharSequence? = when (position) {
            0 -> "信息"
            1 -> "日志"
            else -> throw IndexOutOfBoundsException(
                    "fragment count: $count , pager position: $position")
        }

        override fun getCount() = 2
    }


}