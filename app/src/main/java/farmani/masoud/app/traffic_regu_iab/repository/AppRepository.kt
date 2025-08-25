package farmani.masoud.app.traffic_regu_iab.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import farmani.masoud.app.traffic_regu_iab.database.AppDatabase
import farmani.masoud.app.traffic_regu_iab.database.dao.*
import farmani.masoud.app.traffic_regu_iab.database.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Masoud Farmani on 7/27/2018.
 */
class AppRepository private constructor(application: Application) {

    private val examDao: ExamDao
    private val questionDao: QuestionDao
    private val shortExamDao: ShortExamDao
    private val shortQuestionDao: ShortQuestionDao
    private val roadSignGroupDao: RoadSignGroupDao
    private val roadSignDao: RoadSignDao

    init {
        val database = AppDatabase.getInstance(application)
        examDao = database.examDao()
        questionDao = database.questionsDao()
        shortExamDao = database.shortExamDao()
        shortQuestionDao = database.shortQuestionDao()
        roadSignGroupDao = database.roadSignGroupDao()
        roadSignDao = database.roadSignDao()
    }


    fun selectShortExam(mutableLiveData: MutableLiveData<ShortExam>,
                        category: ShortExam.Category,
                        shortExamNumber: Int) {
        launchAsync {
            shortExamDao.getShortExamByCategoryAndNumber(category.code, shortExamNumber)?.let {
                mutableLiveData.postValue(it)
            }
        }
    }

    fun selectRoadSignGroup(mutableLiveData: MutableLiveData<RoadSignGroup>,
                            groupId: Char) {
        launchAsync {
            roadSignGroupDao.selectById(groupId)?.let {
                mutableLiveData.postValue(it)
            }
        }
    }

    fun activateExamAsync(examCategory: Exam.Category) {
        launchAsync {
            examDao.activateExamGroup(examCategory.code)
        }
    }

    fun selectAllExamsByCategory(mutableLiveData: MutableLiveData<List<Exam>>,
                                 category: Exam.Category) {
        launchAsync {
            examDao.getAllExamsByCategory(category.code)?.let {
                mutableLiveData.postValue(it)
            }
        }
    }

    fun selectQuestionList(examId: Int): LiveData<List<Question>> {
        return questionDao.selectQuestionList(examId)
    }

    /**
     * الگوریتم استفاده شده برای این متد زیبا و کارآمد نیست، یادم باشه اگه راه حل بهتری پیدا کردم الگوریتم رو اصلاح کنم
     * توضیح الگوریتم :
     * اول میام همه سوالات بوکمارک شده رو لود میکنم و لیست آزمون های مربوط به هر دسته بندی که انتخاب شده بوده رو
     * بهش ارسال میکنم بعد میام از روی لیست آزمون ها، لیست آی دی هاشونو استخراج میکنم، بعدش میام چک میکنم اگه لیست
     * آی دی های آزمون ها حاویِ مقدار examId مربوط به لیست سوالات بوکمارک شده بود اون سوال بوکمارک شده رو به
     * لیست سوالات بوکمارک شده قابل قبول اضافه میکنم و در آخر لیست سوالات گلچین شده رو به لایو دیتا ارسال میکنم*/
    fun selectBookmarkedQuestionList(dataLive: MutableLiveData<List<Question>>, exams: List<Exam>) {
        launchAsync {
            val examIdList: List<Int> = exams.map { it.id }
            val bookmarkedQuestionList = questionDao.selectBookmarkedQuestionList()
            val pickedBookmarkedQuestionList = mutableListOf<Question>()
            bookmarkedQuestionList?.let {
                for (q in it) {
                    if (examIdList.contains(q.examId))
                        pickedBookmarkedQuestionList.add(q)
                }
            }
            dataLive.postValue(pickedBookmarkedQuestionList)
        }
    }

    fun selectRoadSignList(groupId: Char, reviewMode: Boolean): LiveData<List<RoadSign>> {
        return if (!reviewMode)
            roadSignDao.selectRoadSignListByGroupId(groupId)
        else
            roadSignDao.selectBookmarkedRoadSignListByGroupId(groupId.toInt(), true)
    }

    fun selectShortQuestionList(shortExamId: Int, reviewMode: Boolean): LiveData<List<ShortQuestion>> {
        return if (!reviewMode)
            shortQuestionDao.selectShortQuestionList(shortExamId)
        else
            shortQuestionDao.selectBookmarkedShortQuestionListByGroupId(shortExamId, true)
    }

    fun updateQuestionList(questionList: List<Question>) {
        launchAsync {
            questionDao.updateAll(questionList)
        }
    }

    fun updateExam(exam: Exam) {
        launchAsync {
            examDao.update(exam)
        }
    }

    fun updateShortQuestionList(shortQuestionList: List<ShortQuestion>) {
        launchAsync {
            shortQuestionDao.updateAll(shortQuestionList)
        }
    }

    fun updateShortExam(shortExam: ShortExam) {
        launchAsync {
            shortExamDao.update(shortExam)
        }
    }
    fun updateQuestion(question:Question){
        launchAsync {
            questionDao.update(question)
        }
    }

    fun updateRoadSignList(roadSignList: List<RoadSign>) {
        launchAsync {
            roadSignDao.updateAll(roadSignList)
        }
    }

    fun updateRoadSignGroup(roadSignGroup: RoadSignGroup) {
        launchAsync {
            roadSignGroupDao.update(roadSignGroup)
        }
    }

    private fun launchAsync(block: suspend () -> Unit) =
            GlobalScope.launch(Dispatchers.IO) {
                block.invoke()
            }

    companion object {
        //Singleton
        @Volatile
        private lateinit var INSTANCE: AppRepository

        fun getInstance(application: Application): AppRepository {
            synchronized(AppRepository::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = AppRepository(application)
                }
            }
            return INSTANCE
        }
    }
}
