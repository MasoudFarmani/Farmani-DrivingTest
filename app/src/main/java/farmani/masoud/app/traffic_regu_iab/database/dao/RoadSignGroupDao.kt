package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSignGroup

/**
 * Created by Masoud Farmani on 8/26/2018.
 */
@Dao
interface RoadSignGroupDao : BaseDao<RoadSignGroup> {

    @Query("SELECT * FROM road_sign_group_table")
    fun selectAll(): LiveData<List<RoadSignGroup>> //دریافت همه رکوردها


    @Query("SELECT * FROM road_sign_group_table where groupId= :id")
    suspend fun selectById(id: Char): RoadSignGroup?
}
