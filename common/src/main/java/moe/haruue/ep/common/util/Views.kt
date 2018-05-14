package moe.haruue.ep.common.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import rx.android.schedulers.AndroidSchedulers

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

fun View.hideInputMethod() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showInputMethod() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

private val mainThreadWorker by lazy {
    AndroidSchedulers.mainThread().createWorker()
}

fun runOnUiThread(runnable: () -> Unit) {
    mainThreadWorker.schedule {
        runnable.invoke()
    }
}
