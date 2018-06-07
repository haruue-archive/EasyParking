package moe.haruue.ep.view.log

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_order_info.*
import moe.haruue.ep.R
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.util.debug
import moe.haruue.ep.common.util.release
import moe.haruue.ep.databinding.ActivityOrderInfoBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.util.kotlin.startActivityForResult
import moe.haruue.util.kotlin.toast
import android.util.Log as ALog

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderInfoActivity : AppCompatActivity() {

    lateinit var binding: ActivityOrderInfoBinding

    companion object {
        const val EXTRA_LOG = "log"
        const val EXTRA_LOG_ID = "log_id"

        const val REQ_LOGIN = 0x1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityOrderInfoBinding>(this@OrderInfoActivity, R.layout.activity_order_info).apply {
            setLifecycleOwner(this@OrderInfoActivity::getLifecycle)
            m = ViewModelProviders.of(this@OrderInfoActivity)[OrderInfoViewModel::class.java].apply {
                val log = intent.getParcelableExtra<Log>(EXTRA_LOG)
                if (log != null) {
                    setLog(log)
                } else {
                    id.value = intent.getStringExtra(EXTRA_LOG_ID)!!
                    refresh()
                }
                needLogin.observe(this@OrderInfoActivity::getLifecycle) {
                    if (it == true) {
                        toLogin()
                    }
                }
                refreshing.observe(this@OrderInfoActivity::getLifecycle) {
                    swipe.isRefreshing = it == true
                }
                toast.observe(this@OrderInfoActivity::getLifecycle) {
                    if (it != null && it.isNotBlank()) {
                        toast(it)
                    }
                }
                fatal.observe(this@OrderInfoActivity::getLifecycle) {
                    if (it == true) {
                        finish()
                    }
                }
                dialogConfirm.observe(this@OrderInfoActivity::getLifecycle) {
                    if (it != null) {
                        AlertDialog.Builder(this@OrderInfoActivity).apply {
                            setTitle("确认？")
                            setMessage(it.first)
                            setPositiveButton("确定") { _, _ -> it.second() }
                            setNegativeButton("取消") { _, _ -> /*nothing*/ }
                        }.create().show()
                    }
                }
                scanQrCode.observe(this@OrderInfoActivity::getLifecycle) {
                    if (it != null) {
                        IntentIntegrator(this@OrderInfoActivity).apply {
                            setOrientationLocked(true)
                            setPrompt(it.first)
                            setBeepEnabled(false)
                        }.initiateScan()
                    }
                }
            }
        }

        setSupportActionBar(toolbar)
        toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    fun refresh() {
        swipe.isRefreshing = true
        binding.m!!.onRefresh()
    }

    fun toLogin() {
        startActivityForResult<LoginActivity>(REQ_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    refresh()
                } else {
                    finish()
                }
            }
        }
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.apply {
            if (contents != null) {
                val callback = binding.m?.scanQrCode?.value?.second
                if (callback != null) {
                    callback(contents)
                    // prevent from calling again when activity resume
                    binding.m?.scanQrCode?.value = null
                } else {
                    debug {
                        error("binding.m?.scanQrCode?.value?.second == null")
                    }
                    release {
                        ALog.e("OrderInfoActivity", "binding.m?.scanQrCode?.value == null")
                        toast("发生错误，请重试。")
                    }
                }
            } else { // Cancelled
                binding.m?.progress?.value = 0
            }
        }
    }

}


