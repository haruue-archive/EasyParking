package moe.haruue.ep.view.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_account_preferences.*
import moe.haruue.ep.R
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.util.debug
import moe.haruue.ep.data.api.MainAPIService
import moe.haruue.ep.view.email.ModifyEmailActivity
import moe.haruue.ep.view.password.ModifyPasswordActivity
import moe.haruue.util.kotlin.support.startActivityForResult
import moe.shizuku.preference.PreferenceFragment
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class AccountPreferencesActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AccountPreferencesA"
    }

    val fragment by lazy { AccountPreferencesFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_preferences)

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
        }
        toolbar.apply {
            setNavigationOnClickListener { finish() }
        }

        setResult(Activity.RESULT_CANCELED)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
        }.commit()

    }

    class AccountPreferencesFragment : PreferenceFragment() {

        companion object {
            const val REQ_EMAIL = 0x1
            const val REQ_MOBILE = 0x2
            const val REQ_CAR = 0x3
            const val REQ_PASSWORD = 0x4
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.account_preferences, "root")

            findPreference("password").setOnPreferenceClickListener {
                startActivityForResult<ModifyPasswordActivity>(REQ_PASSWORD)
                true
            }

            findPreference("email").setOnPreferenceClickListener {
                startActivityForResult<ModifyEmailActivity>(REQ_EMAIL)
                true
            }

            findPreference("car").setOnPreferenceClickListener {
                true
            }

            findPreference("logout").setOnPreferenceClickListener {
                AlertDialog.Builder(context!!).apply {
                    setTitle("确认")
                    setMessage("确定要登出吗？")
                    setPositiveButton("确定") { _, _ -> doLogout() }
                    setNegativeButton("取消") { _, _ -> /*nothing*/ }
                }.create().show()
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                REQ_CAR, REQ_EMAIL, REQ_MOBILE -> {
                    if (resultCode == Activity.RESULT_OK) {
                        activity?.setResult(Activity.RESULT_OK)
                    }
                }
                REQ_PASSWORD -> {
                    if (resultCode == Activity.RESULT_OK) {
                        activity?.apply {
                            setResult(Activity.RESULT_OK)
                            activity?.finish()
                        }
                    }
                }
            }
        }

        fun doLogout() {
            activity!!.progressContainer.visibility = View.VISIBLE
            MainAPIService.with { it.accountLogout() }
                    .observeOn(AndroidSchedulers.mainThread())
                    .apiSubscribe("$TAG#doLogout") {
                        onError = {
                            debug {
                                Log.w(TAG, "error when logout, cache will be clear manually", it)
                            }
                        }
                        onFinally = {
                            activity!!.apply {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
        }
    }

}
