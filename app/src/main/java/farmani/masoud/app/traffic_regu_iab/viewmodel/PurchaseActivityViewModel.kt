package farmani.masoud.app.traffic_regu_iab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository

class PurchaseActivityViewModel(application: Application)
    : AndroidViewModel(application) {

    private val appRepository = AppRepository.getInstance(application)
    val skuChangeTrigger = MutableLiveData<Boolean>().apply {
        value = false
    }
    /*پیامی که کاربر رو از نتیجه فعالسازی آزمون آگاه میکنه*/
    val activationResultMessage = MutableLiveData<String>()

    fun activateExamGroup(category: Exam.Category) {
        appRepository.activateExamAsync(category)
    }

    fun reversTriggerValue() {
        skuChangeTrigger.value = !skuChangeTrigger.value!!
    }
}