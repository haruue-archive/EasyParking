package moe.haruue.ep.view.main

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import kotlinx.android.synthetic.main.activity_main.*
import moe.haruue.ep.R
import moe.haruue.ep.databinding.ActivityMainBinding
import moe.haruue.util.kotlin.statusBarHeight

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            setLifecycleOwner(this@MainActivity::getLifecycle)
            m = ViewModelProviders.of(this@MainActivity)[MainViewModel::class.java].apply {

            }
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(false)
        }
        toolbar.apply {
            title = ""
            subtitle = ""
            setNavigationIcon(R.drawable.ic_menu_gray_24dp)
            setNavigationOnClickListener {
                drawer.openDrawer(Gravity.START)
            }
        }
        nav.getHeaderView(0).setPadding(0, statusBarHeight, 0, 0)
    }

    override fun onBackPressed() {
        when {
            drawer.isDrawerOpen(Gravity.START) -> drawer.closeDrawer(Gravity.START)
            else -> super.onBackPressed()
        }
    }

}