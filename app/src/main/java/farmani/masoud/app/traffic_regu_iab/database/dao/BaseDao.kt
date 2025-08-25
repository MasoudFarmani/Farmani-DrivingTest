package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.room.Insert
import androidx.room.Update

/**
 * Created by Masoud Farmani on 8/26/2018.
 */
interface BaseDao<T> {

    @Insert
    fun insert(t: T): Long

    @Insert
    fun insertAll(tList: List<T>)

    @Update
    suspend fun update(t: T): Int

    @Update
    suspend fun updateAll(tList: List<T>): Int
}
