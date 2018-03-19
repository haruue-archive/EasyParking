package moe.haruue.ep.manager.viewmodel.manager

import android.content.Context
import androidx.content.edit
import moe.haruue.ep.common.util.ApplicationContextHandler
import moe.haruue.ep.manager.model.Manager

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
object ManagerRepository {

    const val SP_NAME = "manager"
    const val SP_KEY_ID = "id"
    const val SP_KEY_PASSWORD = "password"

    val sp by lazy {
        val context = ApplicationContextHandler.context
        context!!.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    fun receive(callback: (manager: Manager?) -> Unit) {
        callback(load())
    }

    fun load(): Manager? {
        val id = sp.getString(SP_KEY_ID, null)
        val password = sp.getString(SP_KEY_PASSWORD, null)
        return if (id == null || password == null) {
            null
        } else {
            Manager(id, password)
        }
    }

    fun save(manager: Manager?) {
        if (manager != null) {
            sp.edit {
                putString(SP_KEY_ID, manager.id)
                putString(SP_KEY_PASSWORD, manager.password)
            }
        } else {
            sp.edit {
                remove(SP_KEY_ID)
                remove(SP_KEY_PASSWORD)
            }
        }
    }
}