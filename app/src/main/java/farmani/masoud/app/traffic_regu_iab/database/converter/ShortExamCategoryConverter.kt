package farmani.masoud.app.traffic_regu_iab.database.converter

import androidx.room.TypeConverter

import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam

/**
 * Created by Masoud Farmani on 9/6/2018.
 */
//این کلاس حاوی متدهایی برای تبدیل مقدار فیلد category کلاس ShortExam به یک عدد صحیح و بالعکس می باشد
class ShortExamCategoryConverter {

    @TypeConverter
    fun codeToCategory(code: Int?): ShortExam.Category? {
        return when (code) {
            ShortExam.Category.COMPREHENSIVE.code -> ShortExam.Category.COMPREHENSIVE
            ShortExam.Category.TECHNICAL.code -> ShortExam.Category.TECHNICAL
            else -> throw IllegalArgumentException("could not recognize category")
        }
    }

    @TypeConverter
    fun categoryToCode(category: ShortExam.Category?): Int? {
        return category?.code
    }
}
