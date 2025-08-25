package farmani.masoud.app.traffic_regu_iab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import farmani.masoud.app.traffic_regu_iab.database.entity.Question
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository
import farmani.masoud.app.traffic_regu_iab.ui.activity.ExamActivity.Companion.selectedExam

class ExamActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository: AppRepository = AppRepository.getInstance(application)
    val questionListLive: LiveData<List<Question>>
    val examResultLive = MutableLiveData<IntArray>()
    val examEvaluated = MutableLiveData<Boolean>()
    val emptyListFlag = MutableLiveData<Boolean>()
    var examIsFinished = false

    init {
        examEvaluated.value = false
        emptyListFlag.value = false
    }

    /*لیست بازگشتی از دیتابیس کش میشه، تا با هربار آپدیت دیتابیس
    * در onStop اکتیویتی، لیست جدیدی به آداپترهای اکتیویتی تزریق نشه
    */
    var cachedQuestionList: List<Question>? = null
        set(value) {
            field = value
            /*ویوپیجر رو از آخرین صفحه شروع کن چون میخوام از راست به چپ ورق بزنم
            * این فیلد با هربار ورق زدن ویوپیجر آپدیت میشه، وقتی اکتیویتی stop و
            * دوباره resume میشه، ویوپیجر از آخرین مقدار ذخیره شده در این فیلد
            * شروع میشه*/
            this.examVpStartingPosition = value?.lastIndex ?: 0
        }
    var examVpStartingPosition = 0

    init {
        questionListLive = loadQuestionList()
    }

    private fun loadQuestionList(): LiveData<List<Question>> {
        return appRepository.selectQuestionList(selectedExam.id)
    }

    fun setExamResultLive(result: IntArray) {
        this.examResultLive.value = result
        this.examEvaluated.value = true
    }

    fun persistQuestionList() {
        cachedQuestionList?.let { appRepository.updateQuestionList(it) }
    }

    private fun countBookmarkQuestion(): Int {
        val localCopy = this.cachedQuestionList ?: return selectedExam.bookmarkedQuestionCount
        return localCopy.groupingBy { it.profile.isBookmarked }.eachCount()[true] ?: 0
    }

    fun persistExam() {
        with(selectedExam) {
            bookmarkedQuestionCount = countBookmarkQuestion()
            appRepository.updateExam(this)
        }
    }
}
