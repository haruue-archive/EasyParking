package moe.haruue.ep.manager.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.ActivityMainBinding

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        binding.let {
            it.setLifecycleOwner(this)
        }
    }

}