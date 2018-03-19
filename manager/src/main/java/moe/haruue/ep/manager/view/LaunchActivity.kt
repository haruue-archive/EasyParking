package moe.haruue.ep.manager.view

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.ActivityLaunchBinding
import moe.haruue.ep.manager.viewmodel.launch.LaunchViewModel
import moe.haruue.util.kotlin.startActivity
import moe.haruue.util.kotlin.startActivityForResult

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LaunchActivity : AppCompatActivity() {

    companion object {
        const val REQ_LOGIN = 1
    }

    private val handler = Handler()
    private lateinit var viewModel: LaunchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityLaunchBinding>(this, R.layout.activity_launch)
        viewModel = ViewModelProviders.of(this)[LaunchViewModel::class.java]

        binding?.let {
            it.m = viewModel
            it.setLifecycleOwner(this)
        }

        viewModel.needLogin.observe(this::getLifecycle) { b: Boolean? ->
            if (b == true) {
                toLogin()
            }
        }
        viewModel.confirmed.observe(this::getLifecycle) {
            if (it == true) {
                toMain()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_LOGIN -> {
                if (requestCode == Activity.RESULT_OK) {
                    // do nothing
                } else {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onCreate()
    }

    private fun toLogin() {
        handler.postDelayed({
            startActivityForResult<LoginActivity>(REQ_LOGIN)
        }, 800)
    }

    private fun toMain() {
        handler.postDelayed({
            startActivity<MainActivity>()
            finish()
        }, 800)
    }
}