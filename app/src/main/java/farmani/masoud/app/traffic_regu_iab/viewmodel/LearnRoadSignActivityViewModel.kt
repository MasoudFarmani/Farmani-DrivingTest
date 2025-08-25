package farmani.masoud.app.traffic_regu_iab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSign
import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSignGroup
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository

class LearnRoadSignActivityViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var liveSignList: LiveData<List<RoadSign>>

    /*اکتیویتی با مشاهده(observe) مقدار این flag ، سرویس ورق زدن خودکار
     * رو فعال یا غیرفعال میکنه
     * */
    val autoPageFlagLive: MutableLiveData<Boolean> = MutableLiveData()
    private val appRepository: AppRepository = AppRepository.getInstance(application)
    private lateinit var selectedGroup: RoadSignGroup
    var emptyListFlag = MutableLiveData<Boolean>()

    init {
        emptyListFlag.value = false
    }

    /*آیا کاربر، اکتیویتی رو با کلیک روی دکمه یِ "مرور کن"
     * استارت کرده؟. در صورتیکه در حالت مرور باشیم 1.نمیخوام
     * آخرین صفحه ای که کاربر مشاهده کرده به عنوان آخرین صفحه
     * مشاهده شده یِ این دسته بندی تابلو، ذخیره بشه و 2.سازوکار شمارش
     * تعداد تابلوهای یادگرفته متفاوت هست
     * چونکه در حالت مرور، ما همه تابلوهای دسته بندی رو لود نکردیم
     * بلکه فقط اوهاییکه بوکمارک شدن رو لود کردیم و ممکنه تعدادی
     * تابلوی یادگرفته شده در لیست تابلوهای بوکمارک شده باشن
     * و کاربر وضعیت یادگرفته شده اونها رو تغییر بده*/
    private var reviewMode = false
    var alreadyLearnedCount: Int = 0
    /*شماره آخرین صفحه ای که کاربر مشاهده کرده*/
    var lastPositionDisplayed: Int = 0

    fun setValueAutoPageFlagLive(autoPageFlag: Boolean) {
        this.autoPageFlagLive.value = autoPageFlag
    }

    fun selectRoadSignList(selectedGroup: RoadSignGroup, reviewMode: Boolean): LiveData<List<RoadSign>> {
        this.reviewMode = reviewMode
        if (!reviewMode) this.lastPositionDisplayed = selectedGroup.lastSeenIndex
        this.selectedGroup = selectedGroup
        val groupId = selectedGroup.groupId
        this.liveSignList = appRepository.selectRoadSignList(groupId, reviewMode)
        return this.liveSignList
    }

    fun persistRoadSignList() {
        val roadSignList: List<RoadSign>? = this.liveSignList.value
        if (roadSignList != null) this.appRepository.updateRoadSignList(roadSignList)
    }

    fun persistRoadSignGroup() {
        val roadSignList: List<RoadSign>? = this.liveSignList.value
        if (roadSignList != null) {
            var bookmarkCount = 0
            var learnCount = 0
            for (rs in roadSignList) {
                if (rs.profile.isBookmarked) bookmarkCount++
                if (rs.profile.isLearned) learnCount++
            }
            this.selectedGroup.bookmarkedCount = bookmarkCount
            val difference = learnCount - alreadyLearnedCount
            this.selectedGroup.learnedCount =
                    if (this.reviewMode) selectedGroup.learnedCount + difference
                    else learnCount
        }
        if (!this.reviewMode) this.selectedGroup.lastSeenIndex = this.lastPositionDisplayed
        this.appRepository.updateRoadSignGroup(this.selectedGroup)
    }
}