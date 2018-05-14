package moe.haruue.ep.view.launch

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
        const val REQ_PERMISSION_LOCATION = 0x2
        const val PERMISSION_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION
    }

    lateinit var binding: ActivityLaunchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityLaunchBinding>(this, R.layout.activity_launch).apply {
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
                onCheckLocationPermission = this@LaunchActivity::onCheckLocationPermission
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

    lateinit var onCheckLocationPermissionCallback: (granted: Boolean) -> Unit

    fun onCheckLocationPermission(callback: (granted: Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                callback(true)
            } else {
                onCheckLocationPermissionCallback = callback
                requestPermissions(arrayOf(PERMISSION_LOCATION), REQ_PERMISSION_LOCATION)
            }
        } else {
            callback(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_PERMISSION_LOCATION -> {
                for ((i, p) in permissions.withIndex()) {
                    if (p == PERMISSION_LOCATION) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            onCheckLocationPermissionCallback(true)
                        } else {
                            onCheckLocationPermissionCallback(false)
                            AlertDialog.Builder(this@LaunchActivity).apply {
                                title = "授权遭拒"
                                setMessage("没有位置权限，本应用将无法运行，请授予本应用位置权限！")
                                setPositiveButton("去授予权限") { _, _ ->
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", packageName, null)
                                    }
                                    startActivity(intent)
                                }
                                setNegativeButton("退出应用") { _, _ ->
                                    finish()
                                    System.exit(0)
                                }

                            }.create().show()
                        }
                    }
                }
            }
        }


    }

    fun toMain() {
        startActivity<MainActivity>()
    }

}