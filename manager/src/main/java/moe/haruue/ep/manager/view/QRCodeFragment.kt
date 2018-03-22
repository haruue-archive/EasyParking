package moe.haruue.ep.manager.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_qrcode.*
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.databinding.FragmentQrcodeBinding
import moe.haruue.util.kotlin.support.startActivity

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class QRCodeFragment : Fragment() {

    lateinit var binding: FragmentQrcodeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qrcode, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entry.setOnClickListener {
            startActivity<QRCodeActivity> {
                putExtra(QRCodeActivity.EXTRA_EXTRA, QRCodeActivity.EXTRA_PARK)
            }
        }
        exit.setOnClickListener {
            startActivity<QRCodeActivity> {
                putExtra(QRCodeActivity.EXTRA_EXTRA, QRCodeActivity.EXTRA_REMOVE)
            }
        }
    }

}