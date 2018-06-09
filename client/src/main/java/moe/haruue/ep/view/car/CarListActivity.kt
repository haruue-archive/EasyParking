package moe.haruue.ep.view.car

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.activity_car_list.*
import kotlinx.android.synthetic.main.item_car.view.*
import moe.haruue.ep.R
import moe.haruue.ep.common.model.Car
import moe.haruue.ep.common.util.AdapterArrayListListener
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.databinding.ActivityCarListBinding
import moe.haruue.ep.view.account.LoginActivity
import moe.haruue.util.kotlin.startActivityForResult

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class CarListActivity : AppCompatActivity() {

    companion object {
        const val REQ_LOGIN = 0x1
        const val REQ_ADD_CAR = 0x2
    }

    lateinit var binding: ActivityCarListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView<ActivityCarListBinding>(this, R.layout.activity_car_list).apply {
            setLifecycleOwner(this@CarListActivity::getLifecycle)
            m = ViewModelProviders.of(this@CarListActivity)[CarListViewModel::class.java].apply {
                toast.observe(this@CarListActivity::getLifecycle) {
                    if (it != null && it.isNotBlank()) {
                        toast(it)
                    }
                }
                needLogin.observe(this@CarListActivity::getLifecycle) {
                    if (it == true) {
                        toLogin()
                    }
                }
                list.adapter = Adapter(cars, this@CarListActivity::onRemoveCar)
                onRefresh()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_car_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.add -> {
            startActivityForResult<AddCarActivity>(REQ_ADD_CAR)
            true
        }
        else -> false
    }

    fun onRemoveCar(car: Car) {
        binding.m?.onRemoveCar(car.id)
    }

    fun toLogin() {
        startActivityForResult<LoginActivity>(REQ_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_LOGIN -> {
                if (resultCode == Activity.RESULT_OK) {
                    binding.m?.onRefresh(true)
                } else {
                    finish()
                }
            }
            REQ_ADD_CAR -> {
                if (resultCode == Activity.RESULT_OK) {
                    binding.m?.onRefresh(true)
                }
            }
        }
    }

    class Adapter(
            val list: ObservableList<Car>,
            val onRemoveCarCallback: (Car) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        init {
            list.addOnListChangedCallback(AdapterArrayListListener(this))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                carId.text = list[position].id
                deleteButton.visibility = View.VISIBLE
                progress.visibility = View.GONE
                deleteButton.setOnClickListener {
                    deleteButton.visibility = View.GONE
                    progress.visibility = View.VISIBLE
                    onRemoveCarCallback(list[position])
                }
            }
        }

        override fun getItemCount() = list.size

        class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        ) {
            val carId = itemView.carId
            val deleteButton = itemView.deleteButton
            val progress = itemView.progress
        }

    }

}