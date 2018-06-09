package moe.haruue.ep.view.log

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_order_list.*
import kotlinx.android.synthetic.main.item_order.view.*
import moe.haruue.ep.R
import moe.haruue.ep.common.model.Log
import moe.haruue.ep.common.util.AdapterArrayListListener
import moe.haruue.ep.common.util.formatToDateTime
import moe.haruue.ep.common.util.toPriceString
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.databinding.ActivityOrderListBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.util.kotlin.startActivity
import moe.haruue.util.kotlin.startActivityForResult
import android.util.Log as ALog

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class OrderListActivity : AppCompatActivity() {

    companion object {
        const val REQ_LOGIN = 0x1
    }

    lateinit var binding: ActivityOrderListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityOrderListBinding>(this, R.layout.activity_order_list).apply {
            setLifecycleOwner(this@OrderListActivity::getLifecycle)
            m = ViewModelProviders.of(this@OrderListActivity)[OrderListViewModel::class.java].apply {
                toast.observe(this@OrderListActivity::getLifecycle) {
                    if (it != null && it.isNotBlank()) {
                        toast(it)
                    }
                }
                needLogin.observe(this@OrderListActivity::getLifecycle) {
                    if (it == true) {
                        toLogin()
                    }
                }
                list.adapter = Adapter(this@OrderListActivity, orders)
            }
        }

        setSupportActionBar(toolbar)
        toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.m?.onRefresh()
    }

    fun toLogin() {
        startActivityForResult<LoginActivity>(REQ_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    binding.m?.onRefresh()
                } else {
                    finish()
                }
            }
        }
    }

    class Adapter(
            val context: Context,
            val list: ObservableList<Log>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        init {
            list.addOnListChangedCallback(AdapterArrayListListener(this))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.apply {
                fee.text = "￥${item.fee.toPriceString()}"
                paid.text = if (item.paid) {"已支付"} else {"未支付"}
                carId.text = item.carId
                summary.text = "${item.lot.name} ${item.spotId}"
                status.text = when(item.status) {
                    Log.STATUS_ORDERED -> "${item.createTime.formatToDateTime()} ${item.statusText}"
                    Log.STATUS_PARKED -> "${item.startTime.formatToDateTime()} ${item.statusText}"
                    Log.STATUS_REMOVED -> "${item.endTime.formatToDateTime()} ${item.statusText}"
                    Log.STATUS_CANCELED -> "${item.endTime.formatToDateTime()} ${item.statusText}"
                    else -> "${item.createTime.formatToDateTime()} 未知状态"
                }
                itemView.setOnClickListener {
                    context.startActivity<OrderInfoActivity> {
                        putExtra(OrderInfoActivity.EXTRA_LOG, list[position])
                    }
                }
            }

        }

        override fun getItemCount() = list.size

        class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        ) {
            val fee = itemView.fee!!
            val paid = itemView.paid!!
            val carId = itemView.carId!!
            val summary = itemView.summary!!
            val status = itemView.status!!
        }

    }

}