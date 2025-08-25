package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSign

/**
 * Created by Masoud Farmani on 8/25/2018.
 */
@Dao
interface RoadSignDao : BaseDao<RoadSign> {

    @Query("SELECT * FROM road_sign_table WHERE groupId = :groupId")
    fun selectRoadSignListByGroupId(groupId: Char): LiveData<List<RoadSign>>


    @Query("SELECT * FROM road_sign_table WHERE name LIKE :roadSignName")
    suspend fun selectRoadSignListByName(roadSignName: String): List<RoadSign> //جستجو براساس نام تابلو


    /*دریافت لیست همه تابلوهایی که بوکمارک شدن*/
    @Query("SELECT * FROM road_sign_table WHERE isBookmarked = :isBookmarked")
    fun selectBookmarkedRoadSigns(isBookmarked: Boolean): LiveData<List<RoadSign>>


    /*دریافت لیست تابلوهایی که متعلق به گروه خاصی هستند
     * و بوکمارک شدن*/
    @Query("SELECT * FROM road_sign_table WHERE groupId = :groupId AND isBookmarked = :isBookmarked")
    fun selectBookmarkedRoadSignListByGroupId(groupId: Int, isBookmarked: Boolean): LiveData<List<RoadSign>>

}
