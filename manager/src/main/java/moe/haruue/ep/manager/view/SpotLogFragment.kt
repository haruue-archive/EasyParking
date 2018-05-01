package moe.haruue.ep.manager.view

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.FragmentSpotLogBinding
import moe.haruue.ep.manager.viewmodel.spot.SpotViewModel

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SpotLogFragment : Fragment() {

    lateinit var binding: FragmentSpotLogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_spot_log, container, false)
        binding.apply {
            setLifecycleOwner(this@SpotLogFragment::getLifecycle)
            m = ViewModelProviders.of(activity!!)[SpotViewModel::class.java].apply {
                error.observe(activity!!::getLifecycle) {
                    it?.run { activity!!.toast(it) }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}