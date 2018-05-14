package moe.haruue.ep.view.main

import android.arch.persistence.room.*
import moe.haruue.ep.common.util.ApplicationContextHandler
import moe.haruue.ep.common.util.runOnUiThread
import kotlin.concurrent.thread

/**
 *
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */
object SearchHistoryRepository {

    private val db by lazy {
        Room.databaseBuilder(ApplicationContextHandler.context!!,
                SearchHistoryDatabase::class.java, "search_history")
                .build()
    }

    fun findSync(keyword: String = "") = db.searchHistoryDao().find(keyword)

    fun find(keyword: String = "", callback: (histories: List<SearchHistory>) -> Unit) {
        thread(start = true) {
            val histories = findSync(keyword)
            runOnUiThread { callback(histories) }
        }
    }

    fun insertSync(keyword: String, time: Long = System.currentTimeMillis()) = db.searchHistoryDao().insert(SearchHistory(time, keyword))

    fun insert(keyword: String, time: Long = System.currentTimeMillis(), callback: () -> Unit = {}) {
        thread(start = true) {
            insertSync(keyword, time)
            runOnUiThread(callback)
        }
    }

    @Entity(tableName = "search_history", primaryKeys = ["time"], indices = [(Index("keyword", unique = true))])
    data class SearchHistory(
            @ColumnInfo(name = "time") val time: Long,
            @ColumnInfo(name = "keyword") val keyword: String
    )

    @Dao
    interface SearchHistoryDao {
        @Query("""
            SELECT * FROM search_history
            WHERE keyword LIKE '%' || :keyword || '%'
            ORDER BY time DESC
            LIMIT 4
        """)
        fun find(keyword: String = ""): List<SearchHistory>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insert(history: SearchHistory)
    }

    @Database(entities = [SearchHistory::class], version = 1)
    abstract class SearchHistoryDatabase : RoomDatabase() {
        abstract fun searchHistoryDao(): SearchHistoryDao
    }

}