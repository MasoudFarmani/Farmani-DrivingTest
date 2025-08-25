package farmani.masoud.app.traffic_regu_iab.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import farmani.masoud.app.traffic_regu_iab.database.profile.QuestionProfile

/**
 * Created by Masoud Farmani on 7/27/2018.
 */
@Entity(tableName = "short_question_table",
        foreignKeys = [
            ForeignKey(
                    entity = ShortExam::class,
                    parentColumns = ["id"],
                    childColumns = ["shortExamId"],
                    onDelete = ForeignKey.CASCADE
            )])
data class ShortQuestion(val number: Int// شماره سوال در دسته بندی - مثلا سوال 34 فنی آشنایی
                         ,
                         val shortExamId: Int//شماره امتحانی که این سوال متعلق به آن است - محدودیت کلید خارجی
                         ,
                         val question: String// متن سوال
                         ,
                         val answer: String// متن جواب
                         ,
                         @field:Embedded
                         val profile: QuestionProfile// پروفایل این سوال
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0// مقدار این فیلد منحصر به فرد است - فاینال تعریف نشده تا دیتابیس به صورت خودکار بهش مقدار بده
    var userNote: String? = null// یادداشت کاربر درباره این سوال
}
