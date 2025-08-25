package farmani.masoud.app.traffic_regu_iab.database.converter

import androidx.room.TypeConverter

import saman.zamani.persiandate.PersianDate

/**
 * Created by Masoud Farmani on 7/30/2018.
 */
class DateConverter {
    @TypeConverter
    fun toDate(timestamp: Long?): PersianDate? {
        return timestamp?.let { PersianDate(it) }
    }

    @TypeConverter
    fun toTimestamp(date: PersianDate?): Long? {
        return date?.time
    }
}
