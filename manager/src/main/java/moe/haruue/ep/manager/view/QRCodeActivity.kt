package moe.haruue.ep.manager.view

import android.animation.ObjectAnimator
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_qrcode.*
import moe.haruue.ep.common.util.debug
import moe.haruue.ep.common.util.release
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.ActivityQrcodeBinding
import moe.haruue.ep.manager.viewmodel.qrcode.QRCodeViewModel
import moe.haruue.util.kotlin.startActivity

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class QRCodeActivity : AppCompatActivity() {

    val handler = Handler {
        when (it.what) {
            MSG_REFRESH -> {
                viewModel.updateQRCode()
                true
            }
            else -> false
        }
    }

    lateinit var viewModel: QRCodeViewModel

    companion object {
        const val EXTRA_EXTRA = "extra"
        const val EXTRA_PARK = "park"
        const val EXTRA_REMOVE = "remove"
        const val MSG_REFRESH = 623
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityQrcodeBinding>(this, R.layout.activity_qrcode)
        var extra = intent?.getStringExtra(EXTRA_EXTRA)

        if (extra.isNullOrBlank()) {
            debug {
                error("QRCodeActivity started without extra")
            }
            release {
                extra = "park"
            }
            finish()
        }

        viewModel = QRCodeViewModel(extra!!)

        binding.let {
            it.m = viewModel
            it.setLifecycleOwner(this)
        }

        viewModel.title.value = when (extra) {
            EXTRA_PARK -> "停车请扫码"
            EXTRA_REMOVE -> "取车请扫码"
            else -> ""
        }

        viewModel.loginFailed.observe(this::getLifecycle) {
            if (it == true) {
                toast("请先登录")
                startActivity<LaunchActivity>()
                finish()
            }
        }

        viewModel.allowAutoNext.observe(this::getLifecycle) {
            if (it == true) {
                handler.sendEmptyMessageDelayed(MSG_REFRESH, 30_000)
                progress.progress
                val animator = ObjectAnimator.ofInt(progress, "progress", 100, 0)
                animator.apply {
                    duration = 30_000
                }.start()
            }
        }

        viewModel.onCreate()

        container.setOnLongClickListener {
            handler.sendEmptyMessage(MSG_REFRESH)
        }
    }

}