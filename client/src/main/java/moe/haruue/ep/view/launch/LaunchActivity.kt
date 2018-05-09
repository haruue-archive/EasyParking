package moe.haruue.ep.view.launch

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import moe.haruue.ep.R
import moe.haruue.ep.databinding.ActivityLaunchBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.ep.view.main.MainActivity
import moe.haruue.util.kotlin.startActivity
import moe.haruue.util.kotlin.startActivityForResult

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LaunchActivity : AppCompatActivity() {

    companion object {
        const val REQ_LOGIN = 0x1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityLaunchBinding>(this, R.layout.activity_launch).apply {
            setLifecycleOwner(this@LaunchActivity::getLifecycle)
            m = ViewModelProviders.of(this@LaunchActivity)[LaunchViewModel::class.java].apply {
                confirmed.observe(this@LaunchActivity::getLifecycle) {
                    if (it == true) {
                        toMain()
                        finish()
                    }
                }
                needLogin.observe(this@LaunchActivity::getLifecycle) {
                    if (it == true) {
                        startActivityForResult<LoginActivity>(REQ_LOGIN)
                    }
                }
            }
        }

        binding.m!!.onCreate()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    toMain()
                }
                finish()
            }
        }
    }

    fun toMain() {
        startActivity<MainActivity>()
    }

}