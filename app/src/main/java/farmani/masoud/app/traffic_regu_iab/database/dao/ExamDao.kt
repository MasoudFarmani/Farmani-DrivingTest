package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import farmani.masoud.app.traffic_regu_iab.database.entity.Exam

/**
 * Created by Masoud Farmani on 7/30/2018.
 */
@Dao
interface ExamDao : BaseDao<Exam> {

    @Query("SELECT * FROM exam_table WHERE category= :category AND number= :examNumber")
    suspend fun getExamByCategoryAndNumber(category: Int, examNumber: Int): Exam?

    @Query("SELECT * FROM exam_table WHERE category = :category")
    fun getAllExamsByCategory(category: Int): List<Exam>?

    @Query("UPDATE exam_table SET isPurchased=1 WHERE category= :category")
    suspend fun activateExamGroup(category: Int): Int
}