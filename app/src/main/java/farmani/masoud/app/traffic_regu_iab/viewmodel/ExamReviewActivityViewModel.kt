package farmani.masoud.app.traffic_regu_iab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam
import farmani.masoud.app.traffic_regu_iab.database.entity.Question
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository
import farmani.masoud.app.traffic_regu_iab.ui.activity.ExamReviewActivity

class ExamReviewActivityViewModel(application: Application) : AndroidViewModel(application) {

    val bookmarkedQuestionListLive = MutableLiveData<List<Question>>()
    private val appRepository = AppRepository.getInstance(application)

    init {
        loadBookmarkedQuestionList(ExamReviewActivity.examCategory)
    }

    private fun loadBookmarkedQuestionList(exams: List<Exam>) {
        appRepository.selectBookmarkedQuestionList(bookmarkedQuestionListLive, exams)
    }
}