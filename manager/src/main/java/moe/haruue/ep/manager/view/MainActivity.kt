package moe.haruue.ep.manager.view

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.ActivityMainBinding
import moe.haruue.ep.manager.viewmodel.main.MainViewModel

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]

        toolbar.title = getString(R.string.app_name)

        binding.let {
            it.setLifecycleOwner(this)
            it.m = viewModel
        }

        viewModel.onCreate()

        val adapter = PagerAdapter(supportFragmentManager)
        pager.adapter = adapter
        tabs.setupWithViewPager(pager, true)
    }

    class PagerAdapter(
            fm: FragmentManager
    ) : FragmentPagerAdapter(fm) {

        val statusFragment by lazy { StatusFragment() }

        val qrCodeFragment by lazy { QRCodeFragment() }

        override fun getItem(position: Int) = when (position) {
            0 -> statusFragment
            1 -> qrCodeFragment
            else -> throw IndexOutOfBoundsException(
                    "fragment count: 2 , pager position: $position")
        }

        override fun getCount() = 2

        override fun getPageTitle(position: Int) = when (position) {
            0 -> "状态"
            1 -> "二维码"
            else -> throw IndexOutOfBoundsException(
                    "fragment count: 2 , pager position: $position")
        }

    }

}