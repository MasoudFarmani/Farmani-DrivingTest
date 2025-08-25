package farmani.masoud.app.traffic_regu_iab.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

import farmani.masoud.app.traffic_regu_iab.database.entity.ShortQuestion

/**
 * Created by Masoud Farmani on 9/4/2018.
 */
@Dao
interface ShortQuestionDao : BaseDao<ShortQuestion> {

    @Query("SELECT * FROM short_question_table WHERE shortExamId = :shortExamId")
    fun selectShortQuestionList(shortExamId: Int): LiveData<List<ShortQuestion>>

    @Query("SELECT * FROM short_question_table WHERE shortExamId = :shortExamId AND isBookmarked = :isBookmarked")
    fun selectBookmarkedShortQuestionListByGroupId(shortExamId: Int, isBookmarked: Boolean): LiveData<List<ShortQuestion>>
}
