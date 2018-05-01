package moe.haruue.ep.common.databinding

import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.graphics.Bitmap
import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
@BindingAdapter("app:error")
fun setError(view: TextInputLayout, error: String?) {
    view.isErrorEnabled = !error.isNullOrBlank()
    view.error = error
}

@BindingAdapter("android:progress")
fun setProgress(view: ProgressBar, progress: Int) {
    val old = view.progress
    val new = progress
    if (old < new) {
        ObjectAnimator.ofInt(view, "progress", old, new)
                .apply {
                    duration = 500
                    setAutoCancel(true)
                }.start()
    } else {
        view.progress = new
    }
}

@BindingAdapter("app:bitmap")
fun setImageBipmap(view: ImageView, bitmap: Bitmap?) {
    if (bitmap == null) {
        view.setImageResource(android.R.color.transparent)
    } else {
        view.setImageBitmap(bitmap)
    }
}

@BindingAdapter("android:text")
fun setPrice(view: TextView, price: Double) {
    view.text = "%1$.2f".format(price)
    /*
    fun String?.isEmptyOrPrice(): Boolean {
        fun Char.isNum() = toInt() in 0x30..0x39
        this ?: return true
        if (isEmpty()) return true

        val a = split(".")
        if (a.size == 1) {
            return a[0].all { it.isNum() }
        }
        if (a.size in 1..2) {
            return a[0].all { it.isNum() } &&
                    a[1].all { it.isNum() } &&
                    a[1].length <= 2
        }
        return false
    }
    view.addTextChangedListener(object : TextWatcher {
        var str = ""
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.d("HIRD", "beforeTextChanged($s, $start, $count, $after)")
            str = s.toString()
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            Log.d("HIRD", "afterTextChanged($s)")
            if (!s.toString().isEmptyOrPrice()) {
                view.text = str
            }
        }
    })
    */
}

@InverseBindingAdapter(attribute = "android:text")
fun getPrice(view: TextView): Double {
    fun String.toPrice(): Double {
        val dot = lastIndexOf(".")
        val s = if (dot >= 0) substring(0, dot + 2) else { this }
        return try {
            s.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }
    return view.text.toString().toPrice()
}

@BindingAdapter("android:activated")
fun setActivated(view: View, activated: Boolean) {
    view.isActivated = activated
}

@InverseBindingAdapter(attribute = "android:activated")
fun isActivated(view: View) = view.isActivated
