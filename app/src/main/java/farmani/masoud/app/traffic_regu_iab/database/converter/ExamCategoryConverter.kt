package farmani.masoud.app.traffic_regu_iab.database.converter

import androidx.room.TypeConverter

import farmani.masoud.app.traffic_regu_iab.database.entity.Exam

/**
 * Created by Masoud Farmani on 9/6/2018.
 */
//این کلاس حاوی متدهایی برای تبدیل مقدار فیلد category کلاس Exam به یک عدد صحیح و بالعکس می باشد
class ExamCategoryConverter {

    @TypeConverter
    fun codeToCategory(code: Int?): Exam.Category? {
        return when (code) {
            Exam.Category.MAIN.code -> Exam.Category.MAIN
            Exam.Category.ELEMENTARY.code -> Exam.Category.ELEMENTARY
            Exam.Category.TECHNICAL.code -> Exam.Category.TECHNICAL
            else -> throw IllegalArgumentException("could not recognize category")
        }
    }

    @TypeConverter
    fun categoryToCode(category: Exam.Category?): Int? {
        return category?.code
    }
}
