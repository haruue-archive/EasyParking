package moe.haruue.ep.view.account

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import moe.haruue.ep.R
import moe.haruue.ep.databinding.ActivityLoginBinding
import moe.haruue.util.kotlin.startActivityForResult

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val REQ_REGISTER = 0x1
    }

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityLoginBinding>(this, R.layout.activity_login).apply {
            setLifecycleOwner(this@LoginActivity::getLifecycle)
            account = ViewModelProviders.of(this@LoginActivity)[AccountViewModel::class.java].apply {
                confirmed.observe(this@LoginActivity::getLifecycle) {
                    if (it == true) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
                needRegister.observe(this@LoginActivity::getLifecycle) {
                    if (it == true) {
                        startActivityForResult<RegisterActivity>(REQ_REGISTER)
                    }
                }
            }
        }

        setSupportActionBar(toolbar)

        setResult(Activity.RESULT_CANCELED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_REGISTER -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.apply {
                        val username = data.getStringExtra("username")
                        val password = data.getStringExtra("password")
                        binding.account?.username?.postValue(username)
                        binding.account?.password?.postValue(password)
                        binding.account?.checkLogin()
                    }
                }
            }
        }
    }

}