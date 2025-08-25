package farmani.masoud.app.traffic_regu_iab.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import farmani.masoud.app.traffic_regu_iab.database.converter.*
import farmani.masoud.app.traffic_regu_iab.database.dao.*
import farmani.masoud.app.traffic_regu_iab.database.entity.*
import farmani.masoud.app.traffic_regu_iab.init.DatabaseInitializer
import farmani.masoud.app.traffic_regu_iab.util.LogCat

/**
 * Created by Masoud Farmani on 7/26/2018.
 */
private const val DATABASE_NAME = "azera_driving_test_database"

@Database(entities = [
    Exam::class,
    Question::class,
    ShortExam::class,
    ShortQuestion::class,
    RoadSignGroup::class,
    RoadSign::class], version = 1, exportSchema = false)
@TypeConverters(
        DateConverter::class,
        ExamStateConverter::class,
        ExamCategoryConverter::class,
        ShortExamCategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun examDao(): ExamDao

    abstract fun questionsDao(): QuestionDao

    abstract fun roadSignGroupDao(): RoadSignGroupDao

    abstract fun roadSignDao(): RoadSignDao

    abstract fun shortExamDao(): ShortExamDao

    abstract fun shortQuestionDao(): ShortQuestionDao

    companion object {

        @Volatile
        private lateinit var INSTANCE: AppDatabase

        fun getInstance(context: Context): AppDatabase {
            synchronized(AppDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = buildDatabase(context)
                }
            }
            return INSTANCE
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME)
                    //Migration Strategy
                    //.fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            LogCat.log(msg = "call from onCreate")
                            val db1 = getInstance(context)
                            DatabaseInitializer.RoadSignTableInitializer(
                                    db1.roadSignGroupDao(),
                                    db1.roadSignDao()).init()
                            DatabaseInitializer.QuestionsTableInitializer(
                                    db1.examDao(),
                                    db1.questionsDao()).init()
                            DatabaseInitializer.ShortQuestionTableInitializer(
                                    db1.shortExamDao(),
                                    db1.shortQuestionDao()).init()

                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            LogCat.log(msg = "call from onOpen")
                        }
                    })
                    .build()
        }
    }

}
