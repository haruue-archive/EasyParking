package moe.haruue.ep.view.email

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import moe.haruue.ep.R
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.databinding.ActivityModifyEmailBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.util.kotlin.startActivity

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class ModifyEmailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityModifyEmailBinding>(this@ModifyEmailActivity, R.layout.activity_modify_email).apply {
            setLifecycleOwner(this@ModifyEmailActivity::getLifecycle)
            m = ViewModelProviders.of(this@ModifyEmailActivity)[ModifyEmailViewModel::class.java].apply {
                confirmed.observe(this@ModifyEmailActivity::getLifecycle) {
                    if (it == true) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
                needLogin.observe(this@ModifyEmailActivity::getLifecycle) {
                    if (it == true) {
                        toast("请先登录")
                        startActivity<LoginActivity>()
                    }
                }
                onCreate()
            }
        }

        setSupportActionBar(toolbar)
        toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        setResult(Activity.RESULT_CANCELED)

    }

}