package moe.haruue.ep.common.util

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import java.lang.ref.WeakReference

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class AdapterArrayListListener<T : ObservableList<*>>(
        adapter: RecyclerView.Adapter<*>
) : ObservableList.OnListChangedCallback<T>() {

    val adapterReference: WeakReference<RecyclerView.Adapter<*>> = WeakReference(adapter)
    inline val adapter: RecyclerView.Adapter<*>?
        get() = adapterReference.get()

    override fun onChanged(sender: T?) {
        adapter?.notifyDataSetChanged()
    }

    override fun onItemRangeRemoved(sender: T?, positionStart: Int, itemCount: Int) {
        adapter?.notifyItemRangeRemoved(positionStart, itemCount)
    }

    override fun onItemRangeMoved(sender: T?, fromPosition: Int, toPosition: Int, itemCount: Int) {
        adapter?.apply {
            for (i in 0 until itemCount) {
                notifyItemMoved(fromPosition + i, toPosition + i)
            }
        }
    }

    override fun onItemRangeInserted(sender: T?, positionStart: Int, itemCount: Int) {
        adapter?.notifyItemRangeInserted(positionStart, itemCount)
    }

    override fun onItemRangeChanged(sender: T?, positionStart: Int, itemCount: Int) {
        adapter?.notifyItemRangeChanged(positionStart, itemCount)
    }

}

fun <T> mutableObservableList(vararg items: T) : ObservableList<T> {
    val list = ObservableArrayList<T>()
    list.addAll(items)
    return list
}