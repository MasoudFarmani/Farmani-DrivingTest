package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import farmani.masoud.app.traffic_regu_iab.database.entity.Question

/**
 * Created by Masoud Farmani on 7/30/2018.
 */
@Dao
interface QuestionDao : BaseDao<Question> {

    @Query("SELECT * FROM question_table WHERE examId = :examId")
    fun selectQuestionList(examId: Int): LiveData<List<Question>>

    @Query("SELECT * FROM question_table where isBookmarked = 1 ")
    fun selectBookmarkedQuestionList(): List<Question>?
}
