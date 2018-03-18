package moe.haruue.ep.common.databinding

import android.animation.ObjectAnimator
import android.databinding.BindingAdapter
import android.support.design.widget.TextInputLayout
import android.widget.ProgressBar

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
