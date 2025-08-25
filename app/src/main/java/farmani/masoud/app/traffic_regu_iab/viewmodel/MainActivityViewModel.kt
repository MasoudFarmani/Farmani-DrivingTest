package farmani.masoud.app.traffic_regu_iab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam

import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSignGroup
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository: AppRepository = AppRepository.getInstance(application)

    /*
     * دسته بندی های انتخاب شده توسط کاربر رو در این
     * آبجکت ها نگه میداریم تا بتونیم تغییراتش (انتخاب دسته بندی جدید توسط کاربر)
     * رو به صورت زنده مشاهده کرده و در فرگمنت مربوطه منعکس کنیم
     * */
    val selectedExamListMutableLive = MutableLiveData<List<Exam>>()
    val selectedShortExamMutableLive = MutableLiveData<ShortExam>()
    val selectedRoadSignGroupMutableLive = MutableLiveData<RoadSignGroup>()

    private val selectedWheelPickerPosition = 0

    val selectedRoadSignGroupLive: LiveData<RoadSignGroup>
        get() = selectedRoadSignGroupMutableLive

    val selectedShortExamLive: LiveData<ShortExam>
        get() = selectedShortExamMutableLive

    fun updateSelectedSignGroupMutableLive(groupId: Char) {
        appRepository.selectRoadSignGroup(selectedRoadSignGroupMutableLive, groupId)
    }

    fun updateSelectedShortExamMutableLive(category: ShortExam.Category, _number: Int) {
        this.appRepository.selectShortExam(
                this.selectedShortExamMutableLive,
                category,
                _number)
    }

    fun selectAllExamsByCategory(category: Exam.Category) {
        this.appRepository.selectAllExamsByCategory(
                selectedExamListMutableLive,
                category
        )
    }
}
