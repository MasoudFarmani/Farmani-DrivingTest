package farmani.masoud.app.traffic_regu_iab.database.converter

import androidx.room.TypeConverter

import farmani.masoud.app.traffic_regu_iab.database.entity.Exam

/**
 * Created by Masoud Farmani on 7/30/2018.
 */
class ExamStateConverter {

    @TypeConverter
    fun codeToState(code: Int?): Exam.State? {
        return when (code) {
            Exam.State.PASSED.code -> Exam.State.PASSED
            Exam.State.FAILED.code -> Exam.State.FAILED
            Exam.State.NULL.code -> Exam.State.NULL
            else -> throw IllegalArgumentException("Could not recognize state")
        }
    }

    @TypeConverter
    fun stateToCode(state: Exam.State?): Int? {
        return state?.code
    }
}
