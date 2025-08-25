package farmani.masoud.app.traffic_regu_iab.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import farmani.masoud.app.traffic_regu_iab.application.App.Companion.iranLocale
import java.util.*

/**
 * Created by Masoud Farmani on 8/24/2018.
 */
@Entity(tableName = "road_sign_group_table")
data class RoadSignGroup(@field:PrimaryKey val groupId: Char// کد دسته بندی - ابتدای نام تصاویر تابلوهای دسته - مقدار آن منحصر به فرد است
                         , val groupName: String// نام دسته بندی - مثلا تابلوهای انتظامی
) {
    var lastSeenIndex: Int = 0// موقعیت آخرین تابلویی که از این دسته بندی مشاهده شده
    var roadSignCount: Int = 0
    var learnedCount: Int = 0// تعداد تابلوهای یادگرفته شده
    var bookmarkedCount: Int = 0

    fun toStringArrayList(): List<String> {
        val infoList = ArrayList<String>()
        infoList.add(String.format(iranLocale, "%d تابلو", roadSignCount))
        infoList.add("تعداد تابلوها :")
        infoList.add(String.format(iranLocale, "%d تابلو", bookmarkedCount))
        infoList.add("تابلوهای نشان شده :")
        infoList.add(String.format(iranLocale, "%d تابلو", learnedCount))
        infoList.add("یادگرفته ها :")
        infoList.add(String.format(iranLocale, "%d درصد", learnedCount * 100 / roadSignCount))
        infoList.add("میزان پیشرفت یادگیری :")
        return infoList
    }
}
