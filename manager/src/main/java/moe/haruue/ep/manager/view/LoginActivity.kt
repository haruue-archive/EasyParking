package moe.haruue.ep.manager.view

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.ActivityLoginBinding
import moe.haruue.ep.manager.viewmodel.login.LoginViewModel

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login)
        val viewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]

        binding.let {
            it.m = viewModel
            it.setLifecycleOwner(this)
        }

        viewModel.confirmed.observe(this::getLifecycle) {
            if (it == true) {
                toMain()
            }
        }
        viewModel.onCreate()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    fun toMain() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}