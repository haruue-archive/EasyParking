package moe.haruue.ep.view.password

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import moe.haruue.ep.R
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.databinding.ActivityModifyPasswordBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.ep.view.oldPassword.ModifyPasswordViewModel
import moe.haruue.util.kotlin.startActivity

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class ModifyPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityModifyPasswordBinding>(this@ModifyPasswordActivity, R.layout.activity_modify_password).apply {
            setLifecycleOwner(this@ModifyPasswordActivity::getLifecycle)
            m = ViewModelProviders.of(this@ModifyPasswordActivity)[ModifyPasswordViewModel::class.java].apply {
                confirmed.observe(this@ModifyPasswordActivity::getLifecycle) {
                    if (it == true) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
                needLogin.observe(this@ModifyPasswordActivity::getLifecycle) {
                    if (it == true) {
                        toast("请先登录")
                        startActivity<LoginActivity>()
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

        setResult(Activity.RESULT_CANCELED)

    }

}