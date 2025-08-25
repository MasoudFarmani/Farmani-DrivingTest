package farmani.masoud.app.traffic_regu_iab.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Created by Masoud Farmani on 8/25/2018.
 */
@Entity(tableName = "short_exam_table")
data class ShortExam(@field:PrimaryKey val id: Int// مقدار این فیلد منحصر به فرد است - هنگام اولین استفاده از برنامه مقدار دهی می شود
                     ,
                     val number: Int// شماره این گروه در دسته بندی خودش - مثلا شماره 1 در سوالات فشرده فنی
                     ,
                     val name: String// نام این دسته بندی
                     ,
                     val category: Category, val resName: String//نام آرایه سوال و جواب های  این دسته بندی در فایل shortquestions.xml منابع
) {
    var questionCount: Int = 0// تعداد سوالات فشرده متعلق به این دسته بندی
    var lastSeenIndex: Int = 0// موقعیت آخرین سوالِ این دسته بندی که کاربر دیده است
    var learnedCount: Int = 0// تعداد سوالاتی که کاربر یاد گرفته
    var bookmarkedCount: Int = 0//تعداد سوالات این دسته بندی که کاربر بوکمارک کرده

    fun toStringArrayList(): List<String> {
        val iranLocale = Locale("fa")
        val infoList = ArrayList<String>()
        infoList.add(String.format(iranLocale, "%d سوال", questionCount))
        infoList.add("تعداد سوالات :")
        infoList.add(String.format(iranLocale, "%d سوال", bookmarkedCount))
        infoList.add("تعداد سوالات مروری :")
        infoList.add(String.format(iranLocale, "%d سوال", learnedCount))
        infoList.add("تعداد یادگرفته ها:")
        infoList.add(String.format(iranLocale, "%d درصد", learnedCount * 100 / questionCount))
        infoList.add("پیشرفت یادگیری :")
        return infoList
    }

    enum class Category constructor(val code: Int) {
        COMPREHENSIVE(1), TECHNICAL(2);

        override fun toString(): String {
            return names[code - 1]
        }

        companion object {
            internal var names = arrayOf("سوالات فشرده جامع", "سوالات فشرده فنی")
        }
    }
}
