package farmani.masoud.app.traffic_regu_iab.database.entity

import androidx.lifecycle.MutableLiveData
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Created by Masoud Farmani on 7/27/2018.
 */
@Entity(tableName = "exam_table")
data class Exam(val category: Category// مجموعه آزمونی که این آزمون به آن تعلق دارد - فنی اصلی مقدماتی
                ,
                @field:PrimaryKey val id: Int// شماره آزمون: هیچ 2 آزمونی مقدار این فیلدشان یکسان نیست
                ,
                val number: Int// شماره آزمون در مجموعه آزمون ها - مثلا آزمون 2 فنی یا آزمون 2 اصلی
                ,
                val resName: String//نام آرایه سوالاتِ این آزمون در فایل questions.xml منابع
                ,
                val isFree: Boolean// آیا این آزمون رایگان هست؟
) {
    @Ignore
    val lastParticipateStateLive = MutableLiveData<State>()
    var lastParticipateState = State.NULL
        // وضعیت آخرین شرکت در آزمون - قبول مردود شرکت نکرده
        set(value) {
            field = value
            lastParticipateStateLive.postValue(field)
        }
    var isPurchased = false// آیا این آزمون خریداری شده؟
    var questionCount = 0//تعداد سوالات این آزمون
    var bookmarkedQuestionCount = 0//تعداد سوالات بوکمارک شده یِ این آزمون

    enum class State(val code: Int) {
        PASSED(1), FAILED(-1), NULL(0);

        override fun toString(): String {
            return stateNames[code + 1]
        }

        companion object {
            internal var stateNames = arrayOf("مردود", "شرکت نکرده", "قبول")
        }
    }

    //order of items is important
    enum class Category(val code: Int) {
        MAIN(0), ELEMENTARY(1), TECHNICAL(2);

        override fun toString(): String {
            return names[code]
        }

        companion object {
            internal var names = listOf("آزمون اصلی", "آزمون مقدماتی", "آزمون فنی")
        }
    }
}

