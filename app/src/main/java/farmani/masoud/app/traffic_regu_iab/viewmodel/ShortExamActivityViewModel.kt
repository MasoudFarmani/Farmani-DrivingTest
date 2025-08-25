package farmani.masoud.app.traffic_regu_iab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortQuestion
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository

class ShortExamActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository: AppRepository = AppRepository.getInstance(application)
    private lateinit var liveShortQuestionList: LiveData<List<ShortQuestion>>
    private lateinit var selectedShortExam: ShortExam

    /*شماره آخرین صفحه ای که کاربر مشاهده کرده*/
    var lastPositionDisplayed: Int = 0

    private var reviewMode = false
    var alreadyLearnedCount: Int = 0
    var emptyListFlag = MutableLiveData<Boolean>()

    init {
        emptyListFlag.value = false
    }

    fun selectShortQuestionListLive(selectedShortExam: ShortExam, reviewMode: Boolean): LiveData<List<ShortQuestion>> {
        this.reviewMode = reviewMode
        if (!reviewMode) this.lastPositionDisplayed = selectedShortExam.lastSeenIndex
        this.selectedShortExam = selectedShortExam
        this.liveShortQuestionList = this.appRepository.selectShortQuestionList(selectedShortExam.id, reviewMode)
        return this.liveShortQuestionList
    }

    fun persistShortQuestionList() {
        val shortQuestionList: List<ShortQuestion>? = this.liveShortQuestionList.value
        if (!shortQuestionList.isNullOrEmpty()) this.appRepository.updateShortQuestionList(shortQuestionList)
    }

    fun persistShortExam() {
        val shortQuestionList: List<ShortQuestion>? = this.liveShortQuestionList.value
        if (shortQuestionList.isNullOrEmpty()) return
        var bookmarkCount = 0
        var learnCount = 0
        for (shq in shortQuestionList) {
            if (shq.profile.isBookmarked) bookmarkCount++
            if (shq.profile.isLearned) learnCount++
        }
        this.selectedShortExam.bookmarkedCount = bookmarkCount
        val difference = learnCount - alreadyLearnedCount
        this.selectedShortExam.learnedCount =
                if (this.reviewMode) selectedShortExam.learnedCount + difference
                else learnCount
        if (!this.reviewMode) this.selectedShortExam.lastSeenIndex = lastPositionDisplayed
        this.appRepository.updateShortExam(this.selectedShortExam)

    }
}
