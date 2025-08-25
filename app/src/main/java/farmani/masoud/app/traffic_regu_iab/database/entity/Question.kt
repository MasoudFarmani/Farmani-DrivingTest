package farmani.masoud.app.traffic_regu_iab.database.entity

import android.os.Build
import androidx.room.*
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.profile.BaseProfile
import java.util.*
import java.util.concurrent.ThreadLocalRandom

/**
 * Created by Masoud Farmani on 7/26/2018.
 */
//this class represents questions generated from "exams" resources
@Entity(tableName = "question_table",
        foreignKeys = [
            ForeignKey(
                    entity = Exam::class,
                    parentColumns = ["id"],
                    childColumns = ["examId"],
                    onDelete = ForeignKey.CASCADE
            )])
data class Question
/*---------------------*/
//constructor
(val examId: Int// آی دی آزمونی که این سوال به آن تعلق دارد. کلید خارجی
 ,
 val number: Int//* شماره این سوال در بین "بقیه سوالات آزمون". مثلا سوال 2 از آزمون 5
 ,
 @field:Embedded
 val profile: BaseProfile,
 val question: String,
 val answerCorrect: String,
 val answerWrong1: String,
 val answerWrong2: String,
 val answerWrong3: String,
 val imageName: String?
) {
    @Ignore
    var userSelectedOption: String? = null
    @Ignore
    var state: State// وضعیت هر سوال در زمان اجرای آزمون :
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var userNote: String? = null// یادداشت کاربر درباره سوال حاضر

    val imageId: Int
        get() = imageName?.let {
            App.appResources
                    .getIdentifier(it, "drawable", App.azeraPackageName)
        } ?: 0

    // درهم کردن گزینه ها
    // در صورتیکه یکی از مقادیر OptionsToPutLast در گزینه ها موجود باشد، همیشه گزینه آخر قرارش می دهیم
    val shuffledOptions: Array<String>
        get() {
            val allOptions = arrayOf(this.answerCorrect, this.answerWrong1, this.answerWrong2, this.answerWrong3)
            shuffleOptions(allOptions)
            OptionsToPutLast.forEach { putItLast(allOptions, it) }
            return allOptions
        }

    init {
        this.state = State.DEFAULT
    }

    enum class State {
        /*
           تا زمانیکه کاربر در صفحه آزمون به سوالی پاسخ نداده باشد
           یا گزینه پایان آزمون را انتخاب نکرده باشد،
           همه سوالات آزمون دارای state = DEFAULT هستند
        */
        DEFAULT,
        ANSWERED,
        CORRECT,
        WRONG,
        WHITE// سوالی که کاربر به اون پاسخ نداده باشه بعد از اتمام آزمون در این وضعیت قرار میگیره
    }
}

private val OptionsToPutLast = arrayOf("همه موارد", "هیچ کدام")
/*  stringList = لیست تمام گزینه ها
        value = گزینه ای که قرار است آخر قرار بگیرد
     */
private fun putItLast(stringList: Array<String>, value: String) {

    val temp: String
    /*
 * نیازی به بررسی خودِ خانه یِ lastIndex در آرایه نیست
 * در صورتیکه مقدار مورد نظر در این خانه قرار داشته باشد مقصود ما برآورده شده است
 */
    for (i in 0 until stringList.lastIndex) {
        // در صورتیکه مقداری برابر با یکی از مقادیر OptionsToPutLast در لیست گزینه ها پیدا شود
        if (stringList[i] == value) {
            temp = stringList[stringList.lastIndex]
            stringList[stringList.lastIndex] = value
            stringList[i] = temp
            break
        }
    }
}

// Implementing Fisher–Yates shuffle
private fun shuffleOptions(ar: Array<String>) {
    val rnd: Random
    // If running on Java 6 or older, use `new Random()` on RHS here
    if (Build.VERSION.SDK_INT >= 21)
        rnd = ThreadLocalRandom.current()
    else
        rnd = Random()
    for (i in ar.size - 1 downTo 1) {
        val index = rnd.nextInt(i + 1)
        // Simple swap
        val s = ar[index]
        ar[index] = ar[i]
        ar[i] = s
    }
}
