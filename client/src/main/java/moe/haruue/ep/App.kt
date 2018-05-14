package moe.haruue.ep

import android.app.Application
import com.oasisfeng.condom.CondomProcess
import moe.haruue.ep.common.util.ApplicationContextHandler
import moe.haruue.ep.common.util.isDebug

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        CondomProcess.installExceptDefaultProcess(this)
        ApplicationContextHandler.context = this
        isDebug = BuildConfig.DEBUG
    }

}