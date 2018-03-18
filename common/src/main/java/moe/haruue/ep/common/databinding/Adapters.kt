package moe.haruue.ep.common.databinding

import android.databinding.BindingAdapter
import android.support.design.widget.TextInputLayout

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
@BindingAdapter("app:error")
fun setError(view: TextInputLayout, error: String?) {
    view.isErrorEnabled = !error.isNullOrBlank()
    view.error = error
}

