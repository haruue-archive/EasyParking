package moe.haruue.ep.view.search

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_search.*
import moe.haruue.ep.R
import moe.haruue.ep.common.model.Lot
import moe.haruue.ep.common.util.AdapterArrayListListener
import moe.haruue.ep.common.util.hideInputMethod
import moe.haruue.ep.common.util.showInputMethod
import moe.haruue.ep.common.util.toast
import moe.haruue.ep.databinding.ActivitySearchBinding
import moe.haruue.ep.view.main.SearchHistoryRepository

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class SearchActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_KEYWORD = "keyword"
        const val EXTRA_MY_LOCATION = "my_location"
        const val RESULT_LOT = "lot"
    }

    lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        binding = DataBindingUtil.setContentView<ActivitySearchBinding>(this, R.layout.activity_search).apply {
            m = ViewModelProviders.of(this@SearchActivity)[SearchViewModel::class.java].apply {
                myLocation = intent.getParcelableExtra(EXTRA_MY_LOCATION)
                keyword.value = intent.getStringExtra(EXTRA_KEYWORD)
                keyword.observe(this@SearchActivity::getLifecycle) {
                    refreshSearchHistoryView()
                }
                error.observe(this@SearchActivity::getLifecycle) {
                    it?.let { toast(it) }
                }
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(false)
        }
        toolbar.apply {
            title = ""
            subtitle = ""
            setNavigationIcon(R.drawable.ic_arrow_back_gray_24dp)
            setNavigationOnClickListener {
                when {
                    search.hasFocus() -> list.requestFocus() /*search.removeFocus()*/
                    else -> finish()
                }
            }
        }

        search.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    binding.m!!.doSearch()
                    exitSearch()
                    true
                }
                else -> false
            }
        }

        list.adapter = Adapter(binding.m!!.result) {
            val result = Intent().apply {
                putExtra(RESULT_LOT, it)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        binding.m!!.doSearch()
    }

    override fun onResume() {
        super.onResume()
        search.post {
            exitSearch()
            search.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    enterSearch()
                } else {
                    exitSearch()
                }
            }
        }
    }

    private fun enterSearch() {
        history.visibility = View.VISIBLE
        search.showInputMethod()
        refreshSearchHistoryView()
    }

    private fun refreshSearchHistoryView() {
        SearchHistoryRepository.find(search.text?.toString() ?: "") {
            history.removeAllViews()
            it.map { createSearchHistoryItemView(it.keyword, history) }.forEach { history.addView(it) }
        }
    }

    private fun createSearchHistoryItemView(keyword: String, parent: ViewGroup): View {
        val layout = layoutInflater.inflate(R.layout.item_search_history, parent, false)
        val text = layout.findViewById<TextView>(R.id.keyword)
        text.text = keyword
        layout.setOnClickListener {
            binding.m!!.let {
                it.keyword.value = keyword
//                search.setText(keyword)
//                search.setSelection(keyword.length)
            }
        }
        return layout
    }

    private fun exitSearch() {
        history.visibility = View.GONE
        search.hideInputMethod()
//        search.removeFocus()
        list.requestFocus()
    }

    class Adapter(
            val results: ObservableList<Lot>,
            private val onItemSelectedCallback: (lot: Lot) -> Unit
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        init {
            results.addOnListChangedCallback(AdapterArrayListListener(this))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(parent)
        }

        override fun getItemCount(): Int {
            return results.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val lot = results[position]
            holder.name.text = lot.name
            holder.location.text = lot.location
            holder.itemView.setOnClickListener {
                onItemSelectedCallback(lot)
            }
        }

        class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        ) {
            val name = itemView.findViewById<TextView>(R.id.lotName)!!
            val location = itemView.findViewById<TextView>(R.id.lotLocation)!!
        }

    }

    override fun onBackPressed() {
        when {
            search.hasFocus() -> list.requestFocus() /*search.removeFocus()*/
            else -> super.onBackPressed()
        }
    }

}