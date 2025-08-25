package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.room.Dao
import androidx.room.Query

import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam

/**
 * Created by Masoud Farmani on 9/4/2018.
 */
@Dao
interface ShortExamDao : BaseDao<ShortExam> {

    @Query("SELECT * FROM short_exam_table WHERE category = :category AND number = :shortExamNumber")
    suspend fun getShortExamByCategoryAndNumber(category: Int, shortExamNumber: Int): ShortExam?
}
