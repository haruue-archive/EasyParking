package moe.haruue.ep.view.account

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import moe.haruue.ep.R
import moe.haruue.ep.databinding.ActivityRegisterBinding

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityRegisterBinding>(this, R.layout.activity_register).apply {
            setLifecycleOwner(this@RegisterActivity::getLifecycle)
            account = ViewModelProviders.of(this@RegisterActivity)[AccountViewModel::class.java].apply {
                confirmed.observe(this@RegisterActivity::getLifecycle) {
                    if (it == true) {
                        val result = Intent().apply {
                            putExtra("username", username.value)
                            putExtra("password", password.value)
                        }
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                }
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.apply {
            setNavigationOnClickListener { finish() }
        }

        setResult(Activity.RESULT_CANCELED)

    }

}