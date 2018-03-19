package moe.haruue.ep.manager

import android.app.Application
import moe.haruue.ep.common.util.ApplicationContextHandler
import moe.haruue.ep.common.util.isDebug

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ApplicationContextHandler.context = this
        isDebug = BuildConfig.DEBUG
    }

}