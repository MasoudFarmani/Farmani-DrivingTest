package farmani.masoud.app.traffic_regu_iab.init;

import android.content.res.Resources;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import farmani.masoud.app.traffic_regu_iab.application.App;
import farmani.masoud.app.traffic_regu_iab.database.dao.ExamDao;
import farmani.masoud.app.traffic_regu_iab.database.dao.QuestionDao;
import farmani.masoud.app.traffic_regu_iab.database.dao.RoadSignDao;
import farmani.masoud.app.traffic_regu_iab.database.dao.RoadSignGroupDao;
import farmani.masoud.app.traffic_regu_iab.database.dao.ShortExamDao;
import farmani.masoud.app.traffic_regu_iab.database.dao.ShortQuestionDao;
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam;
import farmani.masoud.app.traffic_regu_iab.database.entity.Question;
import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSign;
import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSignGroup;
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam;
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortQuestion;
import farmani.masoud.app.traffic_regu_iab.database.profile.BaseProfile;
import farmani.masoud.app.traffic_regu_iab.database.profile.QuestionProfile;
import farmani.masoud.app.traffic_regu_iab.database.profile.RoadSignProfile;

/**
 * Created by Masoud Farmani on 9/2/2018.
 */

public final class DatabaseInitializer {

    public static final class RoadSignTableInitializer {

        @NonNull
        private final RoadSignGroupDao roadSignGroupDao;
        @NonNull
        private final RoadSignDao roadSignDao;

        public RoadSignTableInitializer(@NonNull RoadSignGroupDao roadSignGroupDao,
                                        @NonNull RoadSignDao roadSignDao) {
            this.roadSignGroupDao = roadSignGroupDao;
            this.roadSignDao = roadSignDao;
        }

        @NonNull
        private static List<RoadSign> resolveRoadSignEntityList(RoadSignGroup group) {
            final List<RoadSign> roadSignEntities = new ArrayList<>();
            try {
                /*
                 * signListName = XML اسم لیست تابلوها در فایل
                 * signNameList = لیست اسم های تابلو ها که برگشت داده می شود
                 * اسم لیست تابلوها ( دسته بندی ) در فایل XML با مقدار فیلد groupId در کلاس RoadSignGroup
                 * یکسان است
                 */
                final char groupId = group.getGroupId();
                final String signListName = Character.toString(groupId).toUpperCase();
                Resources r = App.Companion.getAppResources();
                String packageName = App.Companion.getAzeraPackageName();
                String[] roadSignNames = resolveRoadSignNameList(r, packageName, signListName);

                /*قبلا بصورت آرایه عددی ذخیره میشد ولی به دلیل بهم ریختگی
                 * تصاویر به دلیل تغییر آی-دی تصاویر بعد از چندبار بیلد پروژه،
                 * به صورت آرایه متنی نام تصویر تابلو - مثلا e24 - ذخیره میشه.
                 * هربار که نیازی به آی-دی تصویر تابلو داشته باشیم
                 * باصدا زدن RoadSign.getImageId، با استفاده از این متن و
                 * تکنیک Reflection  مقدار آی-دی در Runtime بدست آورده میشه
                 * وتوسط متد getter برگردونده میشه*/
                String[] roadSignImageIds = resolveRoadSignImages(groupId, roadSignNames.length);

                for (int i = 0; i < roadSignNames.length; i++) {
                    roadSignEntities.add(
                            new RoadSign(
                                    roadSignNames[i],
                                    groupId,
                                    roadSignImageIds[i],
                                    new RoadSignProfile(
                                            false,
                                            false,
                                            false)
                            ));
                }
            } catch (ArrayIndexOutOfBoundsException e) {// استثنا به درستی هندل نشده
                e.printStackTrace();
            }
            return roadSignEntities;
        }

        @NonNull
        private static String[] resolveRoadSignImages(char roadSignNameCode,
                                                      int size) {
            String[] ids = new String[size];
            for (int i = 0; i < size; i++) {
                final String signName = String.format(Locale.US, "%c%d", roadSignNameCode, i + 10);
                ids[i] = signName;
            }
            return ids;
        }

        @NonNull
        private static String[] resolveRoadSignNameList(@NonNull Resources res,
                                                        @NonNull String packageName,
                                                        @NonNull String signListName) {
            int roadSignNameListResId = res.getIdentifier(signListName, "array", packageName);
            return res.getStringArray(roadSignNameListResId);
        }

        public void init() {
            List<RoadSignGroup> roadSignGroupList = generateRoadSignGroupRecords();
            List<RoadSign> roadSignList = generateRoadSignRecords(roadSignGroupList);
            new InsertAsyncTask(
                    this.roadSignGroupDao,
                    this.roadSignDao,
                    roadSignGroupList,
                    roadSignList
            ).execute(null, null, null);
        }

        @NonNull
        private List<RoadSignGroup> generateRoadSignGroupRecords() {
            final List<RoadSignGroup> roadSignGroupEntities = new ArrayList<>();
            roadSignGroupEntities.add(new RoadSignGroup('e', "تابلوهای انتظامی"));
            roadSignGroupEntities.add(new RoadSignGroup('h', "تابلوهای هشداردهنده"));
            roadSignGroupEntities.add(new RoadSignGroup('i', "تابلوهای اَخباری"));
            roadSignGroupEntities.add(new RoadSignGroup('r', "تابلوهای راهنمای مسیر"));
            roadSignGroupEntities.add(new RoadSignGroup('l', "تابلوهای محلی"));
            roadSignGroupEntities.add(new RoadSignGroup('m', "تابلوهای مکمل"));
            return roadSignGroupEntities;
        }

        @NonNull
        private List<RoadSign> generateRoadSignRecords(List<RoadSignGroup> roadSignGroupEntities) {
            List<RoadSign> allRoadSignEntities = new ArrayList<>();
            for (RoadSignGroup group :
                    roadSignGroupEntities) {
                final List<RoadSign> roadSignEntities =
                        resolveRoadSignEntityList(group);
                group.setRoadSignCount(roadSignEntities.size());
                allRoadSignEntities.addAll(roadSignEntities);
            }
            return allRoadSignEntities;
        }

        private static class InsertAsyncTask extends AsyncTask<Void, Void, Void> {
            @NonNull
            private final RoadSignGroupDao roadSignGroupDao;
            @NonNull
            private final RoadSignDao roadSignDao;
            @NonNull
            private final List<RoadSignGroup> roadSignGroupList;
            @NonNull
            private final List<RoadSign> roadSignList;

            private InsertAsyncTask(@NonNull RoadSignGroupDao roadSignGroupDao,
                                    @NonNull RoadSignDao roadSignDao,
                                    @NonNull List<RoadSignGroup> roadSignGroupList,
                                    @NonNull List<RoadSign> roadSignList) {
                this.roadSignGroupDao = roadSignGroupDao;
                this.roadSignDao = roadSignDao;
                this.roadSignGroupList = roadSignGroupList;
                this.roadSignList = roadSignList;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                roadSignGroupDao.insertAll(roadSignGroupList);
                roadSignDao.insertAll(roadSignList);
                return null;
            }

        }
    }

    public static final class ShortQuestionTableInitializer {
        @NonNull
        private final ShortExamDao shortExamDao;
        @NonNull
        private final ShortQuestionDao shortQuestionDao;

        public ShortQuestionTableInitializer(@NonNull ShortExamDao shortExamDao,
                                             @NonNull ShortQuestionDao shortQuestionDao) {
            this.shortExamDao = shortExamDao;
            this.shortQuestionDao = shortQuestionDao;
        }

        public void init() {
            final List<ShortExam> shortExamList =
                    generateShortExamEntityRecords();
            final List<ShortQuestion> shortQuestionList =
                    generateShortQuestionEntityRecords(shortExamList);
            new insertAsyncTask(shortExamDao,
                    shortQuestionDao,
                    shortExamList,
                    shortQuestionList)
                    .execute(null, null, null);
        }

        private List<ShortExam> generateShortExamEntityRecords() {
            final List<ShortExam> shortExamList = new ArrayList<>();
            shortExamList.add(new ShortExam(1,
                    1,
                    "آشنایی با سیستم های فنی",
                    ShortExam.Category.TECHNICAL,//فنی
                    "fanni_ashnayi"));
            shortExamList.add(new ShortExam(2,
                    2,
                    "سرویس و نگهداری",
                    ShortExam.Category.TECHNICAL,
                    "fanni_service"));

            Locale iranLocale = new Locale("fa");
            ShortExam.Category category
                    = ShortExam.Category.COMPREHENSIVE;//جامع
            for (int i = 1; i <= 4; i++) {//چون 4 تا آیتم مجموعه سوالات فشرده جامع داریم
                String resourceName = String.format(Locale.US, "comprehensive_%d", i);
                String groupName = String.format(iranLocale, "فشرده جامع بخش %d", i);
                int _id = i + 2;// از شماره 3 به بعد شروع بشه چون 2 تا آیتم فنی داریم
                shortExamList.add(
                        new ShortExam(_id,
                                i,// shortExamNumber
                                groupName,
                                category,
                                resourceName));
            }
            return shortExamList;
        }

        private List<ShortQuestion> generateShortQuestionEntityRecords(
                final List<ShortExam> shortExamList) {
            final List<ShortQuestion> allShortQuestionRecords = new ArrayList<>();
            for (ShortExam shortExam :
                    shortExamList) {
                final int shortExamId = shortExam.getId();
                final String shortExamResourceName =
                        shortExam.getResName();
                final String pkgName = App.Companion.getAzeraPackageName();
                final Resources r = App.Companion.getAppResources();
                final int shortQuestionStringArrayResourceId =
                        r.getIdentifier(shortExamResourceName, "array", pkgName);
                String[] shortQuestionStringList = r.getStringArray(shortQuestionStringArrayResourceId);
                int shortQuestionCount = shortQuestionStringList.length;
                shortExam.setQuestionCount(shortQuestionCount);//تعداد سوالات دسته بندی
                for (int shortQuestionNumber = 1;
                     shortQuestionNumber <= shortQuestionCount;
                     shortQuestionNumber++) {
                    final String currentShortQuestionString = shortQuestionStringList[shortQuestionNumber - 1];
                    String[] shortQuestionParts = currentShortQuestionString.split("%");
                    String shortQuestion_question = shortQuestionParts[0];
                    String shortQuestion_correctAnswer = shortQuestionParts[1];
                    // shortQuestion Object!
                    allShortQuestionRecords.add(new ShortQuestion(
                            shortQuestionNumber,
                            shortExamId,
                            shortQuestion_question,
                            shortQuestion_correctAnswer,
                            new QuestionProfile(false, false)));
                }
            }
            return allShortQuestionRecords;
        }

        private static final class insertAsyncTask extends AsyncTask<Void, Void, Void> {
            @NonNull
            private final ShortExamDao shortExamDao;
            @NonNull
            private final ShortQuestionDao shortQuestionDao;
            @NonNull
            private final List<ShortExam> shortExamList;
            @NonNull
            private final List<ShortQuestion> shortQuestionList;

            insertAsyncTask(@NonNull ShortExamDao shortExamDao,
                            @NonNull ShortQuestionDao shortQuestionDao,
                            @NonNull List<ShortExam> shortExamList,
                            @NonNull List<ShortQuestion> shortQuestionList) {
                this.shortExamDao = shortExamDao;
                this.shortQuestionDao = shortQuestionDao;
                this.shortExamList = shortExamList;
                this.shortQuestionList = shortQuestionList;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                shortExamDao.insertAll(shortExamList);
                shortQuestionDao.insertAll(shortQuestionList);
                return null;
            }
        }
    }

    public static final class QuestionsTableInitializer {

        @NonNull
        private final ExamDao examDao;
        @NonNull
        private final QuestionDao questionDao;

        public QuestionsTableInitializer(@NonNull ExamDao examDao,
                                         @NonNull QuestionDao questionDao) {
            this.examDao = examDao;
            this.questionDao = questionDao;
        }

        public void init() {
            List<Exam> examList = generateExamEntityRecords();
            List<Question> questionList = generateQuestionEntityRecords(examList);
            new QuestionsTableInitializer.InsertAsyncTask(
                    examList,
                    questionList,
                    examDao,
                    questionDao
            ).execute(null, null, null);
        }

        private List<Exam> generateExamEntityRecords() {
            final List<Exam> examList = new ArrayList<>();
            int examId = 1;
            Locale iranLocale = new Locale("fa");
            for (int _number = 1; _number <= 19; _number++) {// قسمت آزمون مقدماتی
                boolean free = false;
                String examResourceName = String.format(Locale.US, "quiz_elementary_%d", _number);
                if (_number <= 5) free = true;
                examList.add(
                        new Exam(
                                Exam.Category.ELEMENTARY,
                                examId++, _number, examResourceName, free));
            }
            for (int _number = 1; _number <= 10; _number++) {// قسمت آزمون اصلی
                boolean free = false;
                String examResourceName = String.format(Locale.US, "quiz_main_%d", _number);
                if (_number <= 2) free = true;
                examList.add(
                        new Exam(
                                Exam.Category.MAIN,
                                examId++, _number, examResourceName, free));
            }
            for (int _number = 1; _number <= 3; _number++) {// قسمت آزمون فنی
                String examResourceName = String.format(Locale.US, "quiz_technical_%d", _number);
                examList.add(
                        new Exam(
                                Exam.Category.TECHNICAL,
                                examId++, _number, examResourceName, false));
            }

            return examList;
        }

        private List<Question> generateQuestionEntityRecords(List<Exam> examList) {
            List<Question> allQuestionRecords = new ArrayList<>();
            final String pkgName = App.Companion.getAzeraPackageName();
            final Resources r = App.Companion.getAppResources();
            for (Exam exam :
                    examList) {
                final int examId = exam.getId();
                final String examResourceName = exam.getResName();
                final int questionStringArrayResourceId =
                        r.getIdentifier(examResourceName, "array", pkgName);
                final String[] questionStringList = r.getStringArray(questionStringArrayResourceId);
                int questionCount = questionStringList.length;
                exam.setQuestionCount(questionCount);//تعداد سوالات این آزمون
                String[] imageNameList = new String[questionCount];//آرایه ای برای نگهداری آی دی
                //تصاویر سوالات - (با روش بهتری جایگزین شود)

                switch (exam.getCategory()) {
                    case ELEMENTARY:
                        switch (exam.getNumber()) {
                            case 1:
                                imageNameList[22] = "e57";
                                imageNameList[23] = "m18";
                                imageNameList[24] = "e15";
                                imageNameList[25] = "h13";
                                imageNameList[26] = "h32";
                                imageNameList[27] = "question_drw1";
                                imageNameList[28] = "question_drw2";
                                imageNameList[29] = "question_drw";
                                break;
                            case 2:
                                imageNameList[21] = "e54";
                                imageNameList[22] = "question_drw6";
                                imageNameList[23] = "e85";
                                imageNameList[24] = "e50";
                                imageNameList[25] = "e26";
                                imageNameList[26] = "e18";
                                imageNameList[27] = "question_drw3";
                                imageNameList[28] = "question_drw4";
                                imageNameList[29] = "question_drw5";
                                break;
                            case 3:
                                imageNameList[20] = "h48";
                                imageNameList[21] = "e16";
                                imageNameList[22] = "e31";
                                imageNameList[23] = "l10";
                                imageNameList[24] = "i62";
                                imageNameList[25] = "e69";
                                imageNameList[26] = "e22";
                                imageNameList[27] = "question_drw7";
                                imageNameList[28] = "question_drw";
                                imageNameList[29] = "question_drw8";
                                break;
                            case 4:
                                imageNameList[20] = "i10";
                                imageNameList[21] = "e34";
                                imageNameList[22] = "e53";
                                imageNameList[23] = "l15";
                                imageNameList[24] = "l16";
                                imageNameList[25] = "e21";
                                imageNameList[26] = "question_drw10";
                                imageNameList[27] = "question_drw11";
                                imageNameList[28] = "question_drw4";
                                imageNameList[29] = "question_drw9";
                                break;
                            case 5:
                                imageNameList[20] = "h56";
                                imageNameList[21] = "h49";
                                imageNameList[22] = "e30";
                                imageNameList[23] = "l13";
                                imageNameList[24] = "l16";
                                imageNameList[25] = "h33";
                                imageNameList[26] = "question_drw12";
                                imageNameList[27] = "question_drw13";
                                imageNameList[28] = "question_drw4";
                                imageNameList[29] = "question_drw14";
                                break;
                            case 6:
                                imageNameList[20] = "e52";
                                imageNameList[21] = "e10";
                                imageNameList[22] = "e32";
                                imageNameList[23] = "e15";
                                imageNameList[24] = "h42";
                                imageNameList[25] = "h26";
                                imageNameList[26] = "question_drw18";
                                imageNameList[27] = "question_drw17";
                                imageNameList[28] = "question_drw16";
                                imageNameList[29] = "question_drw15";
                                break;
                            case 7:
                                imageNameList[20] = "h52";
                                imageNameList[21] = "h53";
                                imageNameList[22] = "h31";
                                imageNameList[23] = "e13";
                                imageNameList[24] = "e72";
                                imageNameList[25] = "e59";
                                imageNameList[26] = "question_drw19";
                                imageNameList[27] = "question_drw20";
                                imageNameList[28] = "question_drw21";
                                imageNameList[29] = "question_drw22";
                                break;
                            case 8:
                                imageNameList[13] = "question_drw27";
                                imageNameList[20] = "e11";
                                imageNameList[21] = "i66";
                                imageNameList[22] = "e57";
                                imageNameList[23] = "h58";
                                imageNameList[24] = "i65";
                                imageNameList[25] = "e70";
                                imageNameList[26] = "question_drw26";
                                imageNameList[27] = "question_drw25";
                                imageNameList[28] = "question_drw24";
                                imageNameList[29] = "question_drw23";
                                break;
                            case 9:
                                imageNameList[6] = "question_drw28";
                                imageNameList[8] = "i58";
                                imageNameList[9] = "e56";
                                imageNameList[10] = "e48";
                                imageNameList[11] = "h14";
                                imageNameList[12] = "h20";
                                imageNameList[14] = "question_drw29";
                                imageNameList[17] = "question_drw32";
                                imageNameList[20] = "i55";
                                imageNameList[21] = "b10";
                                imageNameList[22] = "e82";
                                imageNameList[23] = "h57";
                                imageNameList[24] = "e29";
                                imageNameList[25] = "e74";
                                imageNameList[26] = "question_drw33";
                                imageNameList[27] = "question_drw30";
                                imageNameList[28] = "e38";
                                break;
                            case 10:
                                imageNameList[20] = "e44";
                                imageNameList[21] = "i22";
                                imageNameList[22] = "b11";
                                imageNameList[23] = "question_drw35";
                                imageNameList[24] = "e72";
                                imageNameList[25] = "h35";
                                imageNameList[26] = "e36";
                                imageNameList[27] = "i28";
                                imageNameList[28] = "i61";
                                imageNameList[29] = "question_drw34";
                                break;
                            case 11:
                                imageNameList[3] = "i11";
                                imageNameList[11] = "i57";
                                imageNameList[12] = "h54";
                                imageNameList[22] = "i56";
                                imageNameList[23] = "i60";
                                imageNameList[24] = "i10";
                                imageNameList[25] = "i48";
                                imageNameList[26] = "e63";
                                imageNameList[27] = "i34";
                                imageNameList[28] = "i26";
                                imageNameList[29] = "question_drw36";
                                break;
                            case 12:
                                imageNameList[8] = "e66";
                                imageNameList[9] = "h23";
                                imageNameList[14] = "question_drw37";
                                imageNameList[21] = "e67";
                                imageNameList[22] = "e68";
                                imageNameList[23] = "e24";
                                imageNameList[24] = "h64";
                                imageNameList[25] = "h12";
                                imageNameList[26] = "h61";
                                imageNameList[27] = "e43";
                                imageNameList[28] = "e52";
                                imageNameList[29] = "question_drw38";
                                break;
                            case 13:
                                imageNameList[10] = "h10";
                                imageNameList[11] = "e55";
                                imageNameList[12] = "h27";
                                imageNameList[20] = "e58";
                                imageNameList[21] = "i27";
                                imageNameList[22] = "e53";
                                imageNameList[23] = "question_drw39";
                                imageNameList[24] = "e51";
                                imageNameList[25] = "h31";
                                imageNameList[26] = "h33";
                                imageNameList[27] = "h52";
                                imageNameList[28] = "e70";
                                imageNameList[29] = "question_drw40";
                                break;
                            case 14:
                                imageNameList[3] = "question_drw41";
                                imageNameList[7] = "question_drw42";
                                imageNameList[9] = "question_drw43";
                                imageNameList[18] = "question_drw43";
                                imageNameList[19] = "e87";
                                imageNameList[21] = "h32";
                                imageNameList[22] = "e38";
                                imageNameList[23] = "e71";
                                imageNameList[24] = "i24";
                                imageNameList[25] = "h51";
                                imageNameList[26] = "e69";
                                imageNameList[27] = "h67";
                                imageNameList[29] = "question_drw44";
                                break;
                            case 15:
                                imageNameList[2] = "h38";
                                imageNameList[10] = "i52";
                                imageNameList[12] = "h50";
                                imageNameList[18] = "i12";
                                imageNameList[23] = "h36";
                                break;
                            case 16:
                                imageNameList[1] = "r19";
                                imageNameList[4] = "question_drw45";
                                imageNameList[6] = "question_drw46";
                                imageNameList[9] = "e23";
                                imageNameList[11] = "question_drw47";
                                imageNameList[18] = "h59";
                                imageNameList[19] = "h58";
                                imageNameList[20] = "e19";
                                imageNameList[21] = "r12";
                                imageNameList[22] = "i55";
                                imageNameList[26] = "question_drw48";
                                imageNameList[28] = "i39";
                                break;
                            case 17:
                                imageNameList[4] = "i44";
                                imageNameList[11] = "i47";
                                imageNameList[12] = "e84";
                                imageNameList[14] = "question_drw49";
                                imageNameList[16] = "h37";
                                imageNameList[20] = "question_drw50";
                                imageNameList[23] = "question_drw52";
                                imageNameList[25] = "question_drw53";
                                imageNameList[28] = "e86";
                                break;
                            case 18:
                                imageNameList[1] = "question_drw54";
                                imageNameList[7] = "question_drw55";
                                imageNameList[8] = "h49";
                                imageNameList[13] = "e64";
                                imageNameList[14] = "e58";
                                imageNameList[15] = "e72";
                                imageNameList[20] = "e59";
                                imageNameList[21] = "h26";
                                imageNameList[22] = "e11";
                                imageNameList[23] = "e16";
                                break;
                            case 19:
                                imageNameList[2] = "i29";
                                imageNameList[4] = "question_drw56";
                                imageNameList[12] = "i32";
                                imageNameList[13] = "l14";
                                imageNameList[14] = "question_drw57";
                                imageNameList[20] = "question_drw58";
                                imageNameList[21] = "question_drw59";
                                imageNameList[27] = "i33";
                                imageNameList[28] = "m19";
                                imageNameList[29] = "question_drw60";
                                break;
                        }
                        break;
                    case MAIN:
                        switch (exam.getNumber()) {
                            case 1:
                                imageNameList[20] = "h49";
                                imageNameList[21] = "h33";
                                imageNameList[22] = "i11";
                                imageNameList[23] = "i61";
                                imageNameList[24] = "i32";
                                imageNameList[25] = "h25";
                                imageNameList[26] = "h27";
                                imageNameList[27] = "e41";
                                imageNameList[28] = "e13";
                                imageNameList[29] = "h28";
                                break;
                            case 2:
                                imageNameList[20] = "h50";
                                imageNameList[21] = "h19";
                                imageNameList[22] = "i67";
                                imageNameList[23] = "i27";
                                imageNameList[24] = "a20s25";
                                imageNameList[25] = "e11";
                                imageNameList[26] = "e40";
                                imageNameList[27] = "e42";
                                imageNameList[28] = "h41";
                                imageNameList[29] = "h29";
                                break;
                            case 3:
                                imageNameList[20] = "h65";
                                imageNameList[21] = "h17";
                                imageNameList[22] = "h21";
                                imageNameList[23] = "a22s24";
                                imageNameList[24] = "a22s25";
                                imageNameList[25] = "i59";
                                imageNameList[26] = "e12";
                                imageNameList[27] = "e33";
                                imageNameList[28] = "e38";
                                imageNameList[29] = "e16";
                                break;
                            case 4:
                                imageNameList[10] = "e18";
                                imageNameList[11] = "h32";
                                imageNameList[12] = "i41";
                                imageNameList[13] = "i12";
                                imageNameList[14] = "i28";
                                imageNameList[15] = "e27";
                                imageNameList[16] = "e15";
                                imageNameList[17] = "e48";
                                imageNameList[18] = "h10";
                                imageNameList[20] = "e17";
                                break;
                            case 5:
                                imageNameList[0] = "h51";
                                imageNameList[1] = "h31";
                                imageNameList[2] = "e19";
                                imageNameList[3] = "e67";
                                imageNameList[4] = "h26";
                                imageNameList[5] = "h35";
                                imageNameList[6] = "h38";
                                imageNameList[7] = "a24s8";
                                imageNameList[8] = "e70";
                                imageNameList[9] = "m11";
                                break;
                            case 6:
                                imageNameList[11] = "e44";
                                imageNameList[12] = "h42";
                                imageNameList[14] = "a25s15";
                                imageNameList[15] = "i61";
                                imageNameList[22] = "a25s23";
                                imageNameList[23] = "a25s24";
                                imageNameList[24] = "i10";
                                imageNameList[25] = "e42";
                                imageNameList[26] = "h20";
                                imageNameList[29] = "l14";
                                break;
                            case 7:
                                imageNameList[0] = "i12";
                                imageNameList[9] = "e15";
                                imageNameList[10] = "h38";
                                imageNameList[12] = "e82";
                                imageNameList[14] = "e50";
                                imageNameList[17] = "a26s18";
                                imageNameList[29] = "a25s24";
                                break;
                            case 8:
                                imageNameList[1] = "a27s2";
                                imageNameList[2] = "a27s3";
                                imageNameList[6] = "a27s7";
                                imageNameList[14] = "a27s15";
                                imageNameList[16] = "a27s17";
                                imageNameList[21] = "l13";
                                imageNameList[24] = "h16";
                                imageNameList[25] = "e74";
                                break;
                            case 9:
                                imageNameList[0] = "a28s1";
                                imageNameList[6] = "h58";
                                imageNameList[9] = "a28s10";
                                imageNameList[11] = "h33";
                                imageNameList[13] = "l24";
                                imageNameList[15] = "a28s16";
                                imageNameList[16] = "e86";
                                imageNameList[20] = "a28s21";
                                imageNameList[22] = "a28s23";
                                imageNameList[26] = "e50";
                                imageNameList[29] = "h48";
                                break;
                            case 10:
                                imageNameList[0] = "a29s1";
                                imageNameList[3] = "a29s4";
                                imageNameList[5] = "a29s6";
                                imageNameList[7] = "a29s8";
                                imageNameList[9] = "i55";
                                imageNameList[12] = "a29s14";
                                imageNameList[15] = "a29s17";
                                imageNameList[17] = "e29";
                                imageNameList[18] = "a29s20";
                                break;
                        }
                        break;
                    case TECHNICAL://سوالات دسته بندی فنی فعلا تصویری ندارند
                        break;

                }

                for (int questionNumber = 1; questionNumber <= questionCount; questionNumber++) {
                    try {
                        String currentQuestionString = questionStringList[questionNumber - 1];
                        final String[] questionParts = currentQuestionString.split("\\.");
                        final String question_question = questionParts[0];
                        final String question_correctAnswer = questionParts[1];
                        final String question_wrongAnswerOne = questionParts[2];
                        final String question_wrongAnswerTwo = questionParts[3];
                        final String question_wrongAnswerThree = questionParts[4];
                        allQuestionRecords.add(new Question(
                                examId,
                                questionNumber,
                                new BaseProfile(false),
                                question_question,
                                question_correctAnswer,
                                question_wrongAnswerOne,
                                question_wrongAnswerTwo,
                                question_wrongAnswerThree,
                                imageNameList[questionNumber - 1]
                        ));
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(
                                "خطا در نگارش سوالات آزمون. شماره سوال : " + (questionNumber - 1));
                    }
                }
            }
            return allQuestionRecords;
        }

        private static class InsertAsyncTask extends AsyncTask<Void, Void, Void> {

            @NonNull
            private final List<Exam> examList;
            @NonNull
            private final List<Question> questionList;
            @NonNull
            private final ExamDao examDao;
            @NonNull
            private final QuestionDao questionDao;

            private InsertAsyncTask(@NonNull List<Exam> examList,
                                    @NonNull List<Question> questionList,
                                    @NonNull ExamDao examDao,
                                    @NonNull QuestionDao questionDao) {
                this.examList = examList;
                this.questionList = questionList;
                this.examDao = examDao;
                this.questionDao = questionDao;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                this.examDao.insertAll(examList);
                this.questionDao.insertAll(questionList);
                return null;
            }
        }
    }
}