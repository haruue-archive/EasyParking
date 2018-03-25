package moe.haruue.ep.manager.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_status.*
import moe.haruue.ep.common.data.subscriber.apiSubscribe
import moe.haruue.ep.common.model.Spot
import moe.haruue.ep.common.util.AdapterArrayListListener
import moe.haruue.ep.common.util.mutableObservableList
import moe.haruue.ep.common.viewmodel.SpotItemViewModel
import moe.haruue.ep.manager.R
import moe.haruue.ep.manager.data.api.MainAPIService
import moe.haruue.ep.manager.databinding.FragmentStatusBinding
import moe.haruue.ep.manager.databinding.ItemSpotBinding
import moe.haruue.ep.manager.viewmodel.manager.ManagerRepository
import moe.haruue.ep.manager.viewmodel.status.StatusViewModel
import moe.haruue.util.kotlin.support.startActivity
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class StatusFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    lateinit var binding: FragmentStatusBinding
    val viewModel = StatusViewModel()
    val adapter = Adapter(this::toSpotSummary, this::toAddSpot)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_status, container, false)
        binding.m = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.adapter = adapter
        list.layoutManager = GridLayoutManager(context, 4)
        onRefresh()
        swipe.setOnRefreshListener(this)
    }

    override fun onRefresh() {
        swipe.isRefreshing = true
        ManagerRepository.receive { manager ->
            if (manager == null) {
                toLogin()
            } else {
                MainAPIService.with { it.info(manager.id) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .apiSubscribe("StatusFragment#onRefresh") {
                            onNext = {
                                adapter.list.clear()
                                adapter.list.addAll(it.spots)
                            }
                            onAPIError = {
                                if (it.code == 401 || it.errno == 70001 /*noSuchLot*/) {
                                    toLogin()
                                }
                            }
                            onFinally = {
                                swipe.isRefreshing = false
                            }
                        }
            }
        }
    }

    fun toLogin() {
        startActivity<LaunchActivity>()
        activity?.finish()
    }

    fun toSpotSummary(spot: Spot) {
        startActivity<SpotActivity> {
            putExtra(SpotActivity.EXTRA_SPOT, spot)
        }
    }

    fun toAddSpot() {
        startActivity<SpotEditActivity>()
    }

    class Adapter(
            val onSpotSummaryCallback: (spot: Spot) -> Unit,
            val onAddSpot: () -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val VT_ITEM = 1
            const val VT_ADD = 2
        }

        val list by lazy {
            val l = mutableObservableList<Spot>()
            l.addOnListChangedCallback(AdapterArrayListListener(this@Adapter))
            return@lazy l
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
                    VT_ITEM -> {
                        val binding = DataBindingUtil.inflate<ItemSpotBinding>(
                                LayoutInflater.from(parent.context),
                                R.layout.item_spot, parent, false)
                        binding.m = SpotItemViewModel()
                        ViewHolder(binding)
                    }
                    VT_ADD -> {
                        AddViewHolder(parent)
                    }
                    else -> error("unknown view type: $viewType")
                }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is ViewHolder -> {
                    val data = list[position]
                    holder.binding.m?.data = data
                    if (data == null) {
                        holder.hide()
                    } else {
                        holder.show()
                    }
                    holder.itemView.setOnClickListener {
                        holder.itemView.setOnClickListener {
                            holder.binding.m?.data?.let {
                                onSpotSummaryCallback(it)
                            } ?: Log.e("StatusFragment", "" +
                                    "error in onBindViewHolder#item click listener, " +
                                    "viewmodel == ${holder.binding.m}, spot == $it, " +
                                    "position == $position",
                                    NullPointerException("viewmodel == ${holder.binding.m}, " +
                                            "spot == $it, "))
                        }
                    }
                }
                is AddViewHolder -> {
                    holder.itemView.setOnClickListener {
                        onAddSpot()
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size + 1
        }

        override fun getItemViewType(position: Int) = when {
            position < list.size -> VT_ITEM
            else -> VT_ADD
        }

        class ViewHolder(
                val binding: ItemSpotBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun hide() {
                itemView.visibility = View.GONE
            }

            fun show() {
                itemView.visibility = View.VISIBLE
            }
        }

        class AddViewHolder(
                val parent: ViewGroup
        ) : RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_spot_add, parent, false)
        )
    }

}

