package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.*
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.application.App.Companion.iranLocale
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam
import farmani.masoud.app.traffic_regu_iab.database.entity.Question
import farmani.masoud.app.traffic_regu_iab.extensions.getAllRadioButtons
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor
import farmani.masoud.app.traffic_regu_iab.extensions.setup
import farmani.masoud.app.traffic_regu_iab.ui.AzeraBaseViewPagerAdapter
import farmani.masoud.app.traffic_regu_iab.ui.fragment.ExamResultFragment
import farmani.masoud.app.traffic_regu_iab.ui.fragment.preference.*
import farmani.masoud.app.traffic_regu_iab.ui.util.ScreenShot
import farmani.masoud.app.traffic_regu_iab.ui.widget.ReverseChronometer
import farmani.masoud.app.traffic_regu_iab.ui.widget.RoundedBackgroundSpan
import farmani.masoud.app.traffic_regu_iab.util.HelperFunctions
import farmani.masoud.app.traffic_regu_iab.viewmodel.ExamActivityViewModel
import kotlinx.android.synthetic.main.activity_exam.*
import java.util.*

class ExamActivity : BaseActivity() {
    companion object {
        lateinit var selectedExam: Exam
        var rewardModeFlag = false
        fun getIntent(starter: Context, currentExam: Exam, rewardMode: Boolean = false): Intent {
            selectedExam = currentExam
            rewardModeFlag = rewardMode
            return Intent(starter, ExamActivity::class.java)
        }
    }

    private lateinit var dataHolder: ExamActivityViewModel

    private fun prepareShowCaseView() {
        val bubble1 = BubbleShowCaseBuilder(this)
                .targetView(examTimer)
                .title("زمان سنج")
                .description("با لمس این قسمت میتونید زمان رو نگه دارید یا مجددا فعالش کنید")
                .backgroundColor(getThemeColor(R.attr.colorPrimary))
                .showOnce("examTimerShowCase")

        val bubble2 = BubbleShowCaseBuilder(this)
                .targetView(examBtnFinish)
                .description("با لمس این قسمت میتونید هرموقع که خواستید آزمون رو پایان بدید و نتیجه رو ببینید")
                .backgroundColor(getThemeColor(R.attr.colorPrimary))
                .showOnce("examFinishShowCase")

        val bubble3 = BubbleShowCaseBuilder(this)
                .title("ورق زدن سوالات")
                .description("با پاسخ به سوال، به طور خودکار سوال بعدی آماده میشه ( میتونید در تنظیمات عوضش کنید اگه نخواستید). همچنین میتونید با حرکت دستتون روی هر قسمت از صفحه به چپ و راست، ورق بزنید")
                .backgroundColor(getThemeColor(R.attr.colorPrimary))
                .showOnce("examPagingShowCase")

        val bubble4 = BubbleShowCaseBuilder(this)
                .description("زیر هرسوال ۳ آیکون هست که به ترتیب از راست به چپ: با مداد میتونید برای سوال یادداشت بنویسید. با قلب، سوالو نشاندار کنید برای مرور. علامت بعدی برای ارسال سوال از طریق واتس اپ یا تلگرام واسه دوستاتون هست")
                .backgroundColor(getThemeColor(R.attr.colorPrimary))
                .showOnce("examToolsShowCase")

        val bubble5 = BubbleShowCaseBuilder(this)
                .targetView(examRv)
                .title("جابحایی بین سوالات")
                .description("این قسمت کاربردای زیادی داره، هم میتونید با لمس هر شماره به سوال مورد نظر هدایت بشید، هم از روی تغییر رنگ هرشماره میفهمید به کدام سوالات جواب دادیدو...")
                .backgroundColor(getThemeColor(R.attr.colorPrimary))
                .showOnce("examBottomBarShowCase")

        BubbleShowCaseSequence()
                .addShowCase(bubble1)
                .addShowCase(bubble2)
                .addShowCase(bubble3)
                .addShowCase(bubble4)
                .addShowCase(bubble5)
                .show()
    }

    override fun onStart() {
        super.onStart()
        prepareShowCaseView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)
        dataHolder = ViewModelProviders.of(this).get(ExamActivityViewModel::class.java)
        setSupportActionBar(examToolbar)
        supportActionBar?.setup()
        val vpAdapter = createVpAdapter()
        val rvAdapter = setupRecyclerView()
        /*کلاس ویوپیجر آداپتر، اینترفیس OnStateChangeListener رو implement میکنه*/
        setupTimer(vpAdapter)
        examBtnFinish?.run {
            setOnClickListener {
                checkAllQuestionsAnsweredAndPrompt {
                    /*اگه تعداد سوالات پاسخ نداده برابر 0 بود یا کاربر
                    * از دیالوگ، گزینه پایان آزمون رو انتخاب کرد این بلوک اجرا میشه*/
                    examTimer.interrupt()
                    showResultFragment()
                }
            }
        }
        this.dataHolder.questionListLive.observe(
                this,
                Observer<List<Question>> onChanged@{ returnedQuestionList ->
                    if (returnedQuestionList != null) {
                        /*فقط یکبار لیست سوالات رو دریافت کن و اون رو کش کن*/
                        if (this.dataHolder.cachedQuestionList == null) {
                            this.dataHolder.cachedQuestionList = returnedQuestionList
                        }
                        dataHolder.cachedQuestionList?.run {
                            dataHolder.emptyListFlag.value = isEmpty()
                            val numberFormatter = "${selectedExam.category} %d"
                            val toolbarTitle = String.format(App.iranLocale, numberFormatter, selectedExam.number)
                            examToolbar.title = toolbarTitle
                            vpAdapter.setQuestionList(this)
                            rvAdapter.questionList = this
                        }
                    }
                }
        )
        this.dataHolder.examEvaluated.observe(this, Observer<Boolean> { examEvaluated ->
            if (examEvaluated) examBtnFinish.text = "مشاهده نتیجه"
        })

        dataHolder.emptyListFlag.observe(this, Observer { emptyList ->
            examEmptyListTvLabel.visibility = if (emptyList) View.VISIBLE
            else View.GONE
        })
    }

    private fun countNotAnsweredQuestions(): Int {
        var counter = 0
        dataHolder.cachedQuestionList?.run {
            forEach { question ->
                if (question.state == Question.State.DEFAULT) counter++
            }
        }
        return counter
    }

    override fun onPause() {
        super.onPause()
        if (examTimer.disabled()) return
        if (!(dataHolder.examEvaluated.value)!!) examTimer.pause()
    }

    override fun onResume() {
        super.onResume()
        if (examTimer.isPaused()) examTimer.resume()
    }

    override fun onStop() {
        super.onStop()
        this.dataHolder.persistQuestionList()
        this.dataHolder.persistExam()
    }

    override fun onDestroy() {
        super.onDestroy()
        rewardModeFlag = false
    }

    private fun showResultFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.examRootContainer, ExamResultFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("result")
                .commit()
    }

    private fun setupTimer(timerStateChangeListener: ReverseChronometer.OnStateChangeListener) {
        examTimer?.run {
            addOnStateChangeListener(timerStateChangeListener)
            addOnStateChangeListener(object : ReverseChronometer.OnStateChangeListener {
                override fun onStateChanged(state: ReverseChronometer.State) {
                    when (state) {
                        ReverseChronometer.State.PAUSED -> promptUser(getString(R.string.exam_snackbar_pause_msg))
                        ReverseChronometer.State.RUNNING -> promptUser(getString(R.string.exam_snackbar_resume_msg))
                        ReverseChronometer.State.FINISHED -> promptUserWithAction(
                                getString(R.string.exam_snackbar_finish_action),
                                getString(R.string.exam_snackbar_finish_msg),
                                View.OnClickListener { showResultFragment() }
                        )
                    }
                }
            })
            setOverallDuration((20 * 60).toLong())//ثانیه
                    .setWarningDuration((5 * 60).toLong())
                    .run()
        }//شروع به کار تایمر
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.azera_exam_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_exam_exit, android.R.id.home -> checkExamFinishedAndPrompt()
            R.id.examSetting -> supportFragmentManager
                    .beginTransaction().replace(R.id.examRootContainer, ExamSettingFragment(), "settingFragment")
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (dataHolder.examIsFinished)
            super.onBackPressed()
        else {
            /*درصورتیکه کاربر درحال مشاهده صفحه تنظیمات آزمون بود، به جای نمایش دیالوگ
            * صفحه تنظیمات رو ببند*/
            if (supportFragmentManager.findFragmentByTag("settingFragment") == null)
                checkExamFinishedAndPrompt()
            else
                super.onBackPressed()
        }
    }

    /*درصوتیکه کاربر قصد اتمام آزمون قبل از پاسخگویی به همه سوالات رو داشت بهش اخطار بده*/
    private fun checkAllQuestionsAnsweredAndPrompt(allAnsweredCallBack: () -> Unit) {
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val notShowFlag = preference.getBoolean("exam_unanswered_all_dialog_not_show_again", false)
        /*تعداد سوالات پاسخ نداده*/
        val notAnsweredQuestionCount = countNotAnsweredQuestions()
        /*سوالات پاسخ نداده داریم*/
        if (notAnsweredQuestionCount > 0 && !notShowFlag) {
            MaterialDialog(this).show {
                lifecycleOwner(this@ExamActivity)
                icon(R.drawable.azera_ic_warning)
                title(text = "سوالاتِ بدون پاسخ!")
                message(text = String.format(
                        iranLocale,
                        "هنوز به %d سوال پاسخ ندادی! خاطرجمعی که میخوای آزمون رو پایان بدی و نتیجه رو ببینی؟",
                        notAnsweredQuestionCount
                ))
                positiveButton(text = "آره، پایان آزمون") {
                    if (isCheckPromptChecked()) {
                        preference.edit {
                            putBoolean("exam_unanswered_all_dialog_not_show_again", true)
                        }
                    }
                    allAnsweredCallBack.invoke()
                }
                negativeButton(text = "نه، سوالات پاسخ نداده رو پاسخ میدم") { dismiss() }
                checkBoxPrompt(text = "دیگه این پیام رو نشون نده") { }
            }
        } else {//به همه سوالات پاسخ داده
            allAnsweredCallBack.invoke()
        }
    }

    /*در صورتیکه کاربر قصد خروج قبل از اتمام آزمون داشت با دیالوگ بهش اخطار بده*/
    private fun checkExamFinishedAndPrompt() {
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val notShowFlag = preference.getBoolean("exam_not_finished_dialog_not_show_again", false)
        if (!dataHolder.examIsFinished && !notShowFlag) {
            MaterialDialog(this).show {
                lifecycleOwner(this@ExamActivity)
                icon(R.drawable.azera_ic_warning)
                title(text = "آزمون ناتمام!")
                message(text = "هنوز آزمون رو تموم نکردی، خاطرجمعی که میخوای خارج شی؟")
                checkBoxPrompt(text = "دیگه این پیام رو نشون نده") {}
                positiveButton(text = "آره خارج میشم") { dialog ->
                    if (dialog.isCheckPromptChecked()) {
                        preference.edit { putBoolean("exam_not_finished_dialog_not_show_again", true) }
                    }
                    finish()
                }//خروج از اکتیویتی آزمون
                negativeButton(text = "نه، ادامه میدم") { dismiss() }
            }
        } else finish()
    }

    private fun promptUser(promptMsg: String) {
        HelperFunctions.promptUser(promptMsg, examVp, showLength = Snackbar.LENGTH_LONG)
    }

    private fun promptUserWithAction(actionText: String,
                                     promptMsg: String,
                                     clickListener: View.OnClickListener) {
        HelperFunctions.promptUser(promptMsg, examVp, Snackbar.LENGTH_INDEFINITE, actionText, clickListener = clickListener)
    }

    private fun createVpAdapter(): ViewPagerAdapter {
        return ViewPagerAdapter(this, examVp, examRv, dataHolder)
    }

    private fun setupRecyclerView(): RecyclerViewAdapter {
        val rvAdapter = RecyclerViewAdapter(this, examVp)
        this.examRv.adapter = rvAdapter
        this.examRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        return rvAdapter
    }


    private class ViewPagerAdapter(ctx: AppCompatActivity,
                                   containerVp: ViewPager,
                                   connectedRv: RecyclerView,
                                   private val examActivityViewModel: ExamActivityViewModel)
        : AzeraBaseViewPagerAdapter<Question>(ctx, containerVp, connectedRv),
            ReverseChronometer.OnStateChangeListener {

        private var currentTimerState: ReverseChronometer.State? = null
        private val colorAccent: Int = ctx.getThemeColor(R.attr.colorAccent)
        private val defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(ctx)
        private val context = ctx

        private fun buildArrayOfRadioButtons(rootView: View): Array<MaterialRadioButton> {
            return with(rootView) {
                arrayOf(
                        findViewById(R.id.examRbAnswer1),
                        findViewById(R.id.examRbAnswer2),
                        findViewById(R.id.examRbAnswer3),
                        findViewById(R.id.examRbAnswer4))
            }

        }

        private fun cacheRadioButtonDefaultBg(radioButton: RadioButton) = radioButton.background

        private fun buildBackgroundForRadioButtonState(@DrawableRes drawable: Int) =
                ContextCompat.getDrawable(layoutInflater.context, drawable)

        private fun displayQuestionImage(imageView: ImageView, currentQuestion: Question) {
            imageView.setImageResource(currentQuestion.imageId)
        }

        override fun instantiateItem(container: ViewGroup, ltrPosition: Int): Any {
            entityList?.run {
                // فقط درصورتیکه لیست سوالات لود شده بود ویو رو بساز
                val rootView = layoutInflater.inflate(R.layout.item_vp_exam, container, false)
                //آخرین سوال رو در اولین مکان قرار بده چون میخوام از راست به چپ ورق بزنم
                val currentQuestion = this[getMirroredPosition(ltrPosition)]
                val examTvQuestion = rootView.findViewById(R.id.examTvQuestion) as AppCompatTextView// صورت سوال
                val questionTvStatus = rootView.findViewById(R.id.questionTvStatus) as TextView// وضعیت سوال پس از پایان آزمون
                val examRbAnswerList = buildArrayOfRadioButtons(rootView)
                val defaultRadioButtonBg = cacheRadioButtonDefaultBg(examRbAnswerList[0])
                val answeredRadioButtonBg = buildBackgroundForRadioButtonState(R.drawable.azera_rb_bg_shape_answered)
                val examRgAnswers = rootView.findViewById(R.id.examRgAnswers) as RadioGroup
                /*بک گراند رادیوباتن ها ور براساس وضعیت چکشون تغییر بده*/
                examRgAnswers.setOnCheckedChangeListener onCheckChanged@{ group, _ ->
                    /*اگه آزمون تموم شده بود، برگرد! چون این پیاده سازی، بک گراند رادیوباتن هایی که چک نخوردن رو
                    * به حالت پیشفرض درمیاره و درصورتیکه پاسخ کاربر غلط باشه، ما باید 2 رادیو باتن که
                    * یکی پاسخ کاربر و دیگری پاسخ صحیح است رو بک گراند بدیم*/
                    if (examActivityViewModel.examEvaluated.value!!) return@onCheckChanged
                    group.getAllRadioButtons().groupBy {
                        it.isChecked
                    }.forEach {
                        if (it.key)
                            it.value[0].background = answeredRadioButtonBg
                        else
                            for (radioButton in it.value) {
                                radioButton.background = defaultRadioButtonBg
                            }
                    }
                }
                val examIvQuestion = rootView.findViewById(R.id.examIvQuestion) as AppCompatImageView// نمایش تصویر سوال
                val examIbShare = rootView.findViewById(R.id.examIbShare) as AppCompatImageButton
                val examIbNote = rootView.findViewById(R.id.examIbNote) as AppCompatImageButton
                val examChbBookmark = rootView.findViewById(R.id.examChbBookmark) as MaterialCheckBox
                examIbShare.setOnClickListener { ScreenShot.shareView(container.context, rootView.findViewById(R.id.examCardWrapper)) }
                /*نمایش دیالوگ برای دریافت یادداشت کاربر*/
                examIbNote.setOnClickListener {
                    val materialDialog = MaterialDialog(layoutInflater.context).show {
                        input(
                                prefill = currentQuestion.userNote,
                                inputType = InputType.TYPE_CLASS_TEXT,
                                maxLength = 100,
                                allowEmpty = true
                        ) { _: MaterialDialog, charSequence: CharSequence ->
                            currentQuestion.userNote = charSequence.toString()
                            Toast.makeText(context, "یادداشت ذخیره شد", Toast.LENGTH_SHORT).show()
                        }
                        icon(R.drawable.azera_ic_edit_outline)
                        title(text = "یادداشت بنویس")
                        positiveButton(text = "ذخیره")
                        negativeButton(text = "بیخیال")
                    }
                    val inputField = materialDialog.getInputField()
                    /*درصورتیکه متن کاربر بیشتر از یک خط بود، برو به خط بعدی*/
                    inputField.setSingleLine(false)
                    /*خواستم بیشتر شبیه تکست ویو باشه*/
                    inputField.setBackgroundColor(ContextCompat.getColor(layoutInflater.context, android.R.color.transparent))
                }
                /*اگر سوال حاضر قبلا بوکمارک شده بود، چک باکس رو تیک بزن*/
                examChbBookmark.isChecked = currentQuestion.profile.isBookmarked
                examChbBookmark.setOnClickListener here@{ checkBox: View ->
                    if (rewardModeFlag) {// اگر با تبلیغات اومده بود اجازه بوکمارک سوال رو نده
                        (checkBox as CheckBox).isChecked = false
                        Toast.makeText(context, "فقط در نسخه کامل این امکان وجود دارد!", Toast.LENGTH_SHORT).show()
                        return@here
                    }
                    checkBox as MaterialCheckBox;currentQuestion.profile.isBookmarked = checkBox.isChecked
                    promptUser(if (checkBox.isChecked) "سوال به لیست مرور اضافه شد" else "سوال از لیست مرور حذف شد")
                }
                /*ترکیب شماره سوال رنگی شده با colorAccent و صورت سوال با استفاده از Span*/
                val spannableQuestionWithNumber = SpannableStringBuilder(String.format(
                        iranLocale,
                        " سوال %d از %d ",
                        getMirroredPosition(ltrPosition) + 1,
                        size
                ))
                spannableQuestionWithNumber.setSpan(
                        RoundedBackgroundSpan(context),
                        0,
                        spannableQuestionWithNumber.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableQuestionWithNumber.setSpan(
                        StyleSpan(Typeface.BOLD_ITALIC),
                        0,
                        spannableQuestionWithNumber.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                /*اضافه کردن صورت سوال به شماره سوال رنگی شده*/
                spannableQuestionWithNumber.append(" ")
                spannableQuestionWithNumber.append(currentQuestion.question)
                examTvQuestion.text = spannableQuestionWithNumber
                /*------------------end concat current question number and statement*/
                displayQuestionImage(examIvQuestion, currentQuestion)
                /*دریافت لیست درهم شده یِ گزینه های سوال*/
                val answerList = currentQuestion.shuffledOptions
                val examFinished = currentTimerState?.let {
                    (it == ReverseChronometer.State.FINISHED)
                            ||
                            (it == ReverseChronometer.State.INTERRUPTED)
                } ?: false
                for (index in examRbAnswerList.indices) {
                    with(examRbAnswerList[index]) {
                        text = String.format(iranLocale, "%s", answerList[index])
                        /*اگر قبلا به این سوال پاسخ داده بود، پاسخش رو تیک بزن*/
                        isChecked = (answerList[index] == currentQuestion.userSelectedOption)
                    }

                    if (examFinished) {
                        with(examRbAnswerList[index]) {
                            if (answerList[index] == currentQuestion.answerCorrect)
                                background = buildBackgroundForRadioButtonState(R.drawable.azera_rb_bg_shape_correct)
                            else if (answerList[index] == currentQuestion.userSelectedOption)
                                background = buildBackgroundForRadioButtonState(R.drawable.azera_rb_bg_shape_wrong)

                            /*رادیوباتن ها بعد از پایان آزمون باید غیرقابل کلیک شدن باشند*/
                            isClickable = false
                        }

                        val currentState = currentQuestion.state
                        var statusMsg = ""
                        var statusBgColor = 0
                        var statusTextColor = Color.WHITE

                        /*
                         * در صورتیکه سوال پاسخ داده نشده بود، متن مناسب را از
                         * منابع متن دریافت کن و رنگ پس زمینه تکست ویو رو زرد کن
                         * */

                        when (currentState) {
                            Question.State.WHITE -> {
                                statusMsg = containerVp.resources.getString(R.string.exam_status_white)
                                statusBgColor = ContextCompat
                                        .getColor(
                                                containerVp.context,
                                                R.color.azeraColorYellow
                                        )
                                statusTextColor = Color.BLACK
                            }
                            Question.State.WRONG -> {
                                statusMsg = containerVp.resources.getString(R.string.exam_status_wrong)
                                statusBgColor = ContextCompat
                                        .getColor(containerVp.context,
                                                R.color.azeraColorRed
                                        )
                            }
                            Question.State.CORRECT -> {
                                statusMsg = containerVp.resources.getString(R.string.exam_status_correct)
                                statusBgColor = ContextCompat
                                        .getColor(containerVp.context,
                                                R.color.azeraColorGreen
                                        )
                            }
                        }
                        with(questionTvStatus) {
                            text = statusMsg
                            setBackgroundColor(statusBgColor)
                            setTextColor(statusTextColor)
                            visibility = View.VISIBLE
                        }
                        //به ابتدای حلقه  for برگرد
                        continue
                    }// End of if(examFinished)

                    /*در صورتیکه زمان آزمون تمام نشده باشد اجرای برنامه به این خط می رسد*/
                    examRbAnswerList[index].setOnClickListener onclick@{ radioButton: View ->
                        radioButton as MaterialRadioButton
                        if (currentTimerState == ReverseChronometer.State.PAUSED) {
                            //radioButton.isChecked = false
                            promptUser(containerVp.resources.getString(R.string.exam_snackbar_pause_msg))
                            return@onclick
                        }
                        currentQuestion.userSelectedOption = radioButton.text.toString()
                        // برای شمارش سوالات پاسخ نداده، هچنین تغییر رنگ آیتم ریسایکلرویو پایین صفحه برای راحتی کاربر
                        if (currentQuestion.state == Question.State.DEFAULT) {
                            currentQuestion.state = Question.State.ANSWERED
                            connectedRv.adapter?.notifyItemChanged(getMirroredPosition(ltrPosition))
                        }
                        pageTheViewPager(ltrPosition)
                    }
                }
                container.addView(rootView)
                return rootView
            } ?: return super.instantiateItem(container, ltrPosition)
        }

        fun pageTheViewPager(currentPosition: Int) {
            val autoPageAllowed = defaultSharedPref.getBoolean(examAutoPageEnableKey, true)
            /*درصورت غیرفعال بودن ورق زدن، برگرد*/
            if (!autoPageAllowed) return
            /*ثانیه 1,2,3*/
            var autoPageDelayTime = defaultSharedPref.getInt(examAutoPageTimeKey, examAutoPageTimeDefValue)
            /*میلی ثانیه*/
            autoPageDelayTime *= 800
            App.uiHandler.postDelayed({
                this.containerVp.setCurrentItem(if (currentPosition == 0) this.entityList?.lastIndex
                        ?: 0 else currentPosition - 1, true)
            }, autoPageDelayTime.toLong())
        }

        fun setQuestionList(questionList: List<Question>) {
            super.entityList = questionList

            if (questionList.isNotEmpty()) {
                this.containerVp.currentItem = examActivityViewModel.examVpStartingPosition
            }
        }

        /*
         * با استفاده از مقدار جاری currentTimerState تصمیم میگیرم که
         * پاسخ کاربر رو ثبت کنیم یا نه ( در رخداد کلیک)*/

        override fun onStateChanged(state: ReverseChronometer.State) {
            this.currentTimerState = state
            if (state == ReverseChronometer.State.FINISHED || state == ReverseChronometer.State.INTERRUPTED) {
                /*فلگ اتمام آزمون برای نشان دادن دیالوگ خروج قبل از اتمام*/
                examActivityViewModel.examIsFinished = true
                /*اگه نتیجه آزمون قبلا محاسبه شده، برگرد!*/
                if (examActivityViewModel.examEvaluated.value == true) return
                val examResult = calculateExamResult()
                //this.notifyDataSetChanged(); این جواب نمیده
                this.containerVp.adapter = this// این جواب میده
                //ویوپیجر رو ببر به موقعیت سمت راست ترین صفحه (به دلیل اینکه از راست به چپ چیده شدن
                if (this.entityList != null)
                    this.containerVp.setCurrentItem(this.entityList!!.lastIndex, true)
                // نتیجه محاسبه شده رو در آبجکت ویو مدل ذخیره کن
                this.examActivityViewModel.setExamResultLive(examResult)
                this.connectedRv.adapter?.notifyDataSetChanged()
                updateExamData(examResult[0])
            }
        }

        override fun onPageSelected(rtlPosition: Int) {
            super.onPageSelected(rtlPosition)
            examActivityViewModel.examVpStartingPosition = rtlPosition
        }

        private fun updateExamData(correctAnswerCount: Int) {
            with(selectedExam) {
                lastParticipateState =
                        if (correctAnswerCount >=
                                containerVp.resources.getInteger(R.integer.require_correct_to_pass)) {
                            Exam.State.PASSED
                        } else {
                            Exam.State.FAILED
                        }
                /*تعداد سوالات بوکماک شده در ویومدل محاسبه میشه*/
            }
        }

        /*
         * محاسبه نتیجه آزمون
         * تغییر وضعیت هرسوال باتوجه به پاسخ کاربر به اون سوال
         * */
        private fun calculateExamResult(): IntArray {
            var correct = 0
            var wrong = 0
            var white = 0
            var autoBookmarkAllowed = defaultSharedPref.getBoolean(examAutoBookmarkEnableKey, true)
            if (rewardModeFlag) autoBookmarkAllowed = false//اگر با تبلیغات اومده بود سوالای غلط جواب داده رو بوکمارک نکن

            if (this.entityList != null) {
                for (question in this.entityList!!) {

                    /*
                     * آزمون تموم شده پس هرسوالی که هنوز در وضعیت پیشفرض هست
                     * رو به وضعیت پاسخ نداده تبدیل کن
                     * */
                    if (question.state == Question.State.DEFAULT) {
                        white++
                        question.state = Question.State.WHITE

                        /*
                         * در صورتیکه وضعیت سوال در حالت پاسخ داده قرار داشت
                         * بررسی کن که آیا پاسخ کاربر درست است یا غلط
                         * */

                    } else if (question.state == Question.State.ANSWERED) {

                        /*
                         * اگه پاسخ انتخابی کاربر با پاسخ صحیح سوال یکی بود پس پاسخ درسته
                         * در غیر اینصورت پاسخ غلطه
                         * */
                        val correctlyAnswered = question.userSelectedOption == question.answerCorrect
                        if (correctlyAnswered) {
                            question.state = Question.State.CORRECT
                            correct++
                        } else {
                            question.state = Question.State.WRONG
                            wrong++
                            /*سوالات غیربوکمارک رو براساس تنظیمات نشان کردن، نشان کن*/
                            if (!question.profile.isBookmarked) {
                                question.profile.isBookmarked = autoBookmarkAllowed
                            }

                        }
                    }
                }
            }
            return intArrayOf(correct, wrong, white)
        }
    }

    private class RecyclerViewAdapter(ctx: Context, private val examVp: ViewPager)
        : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        private val iranLocale: Locale = Locale("fa")
        var questionList: List<Question>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        /*رنگ لبه های باتن برای سوال در وضعیت ANSWERED*/
        private val answeredStateList = createColorStateList(layoutInflater.context.getThemeColor(R.attr.colorSecondary))
        /*رنگ لبه های باتن برای سوال در وضعیت CORRECT*/
        private val correctStateList = createColorStateList(ContextCompat.getColor(ctx, R.color.azeraColorGreen))
        /*رنگ لبه های باتن برای سوال در وضعیت WRONG*/
        private val wrongStateList = createColorStateList(ContextCompat.getColor(ctx, R.color.azeraColorRed))
        /*رنگ لبه های باتن برای سوال در وضعیت WHITE*/
        private val whiteStateList = createColorStateList(ContextCompat.getColor(ctx, R.color.azeraColorYellow))
        /*رنگ لبه های باتن برای سوال در وضعیت پیشفرض*/
        private val defaultStateList = createColorStateList(ContextCompat.getColor(ctx, R.color.azeraColorGray))

        private fun createColorStateList(@ColorInt color: Int) =
                ColorStateList(
                        arrayOf(intArrayOf(-android.R.attr.state_pressed)),
                        intArrayOf(color)
                )

        override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): ViewHolder {
            val itemView = layoutInflater.inflate(R.layout.item_rv_exam, viewGroup, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.examBtnNumber.text = String.format(iranLocale, "%d", position + 1)
            val currentQuestion = questionList?.get(position)
            val currentQuestionState = currentQuestion?.state
            when (currentQuestionState) {
                Question.State.ANSWERED -> viewHolder.examBtnNumber.strokeColor = answeredStateList
                Question.State.CORRECT -> viewHolder.examBtnNumber.strokeColor = correctStateList
                Question.State.WRONG -> viewHolder.examBtnNumber.strokeColor = wrongStateList
                Question.State.WHITE -> viewHolder.examBtnNumber.strokeColor = whiteStateList
                else -> viewHolder.examBtnNumber.strokeColor = defaultStateList
            }
            viewHolder.examBtnNumber.setOnClickListener {
                //چون ایندکس های ویوپیجر و ریسایکلر ویو برعکس هستند
                //ابتدا ایندکس رو برعکس کن بعد ارسال کن به ویوپیجر
                examVp.setCurrentItem(getMirroredPosition(position), true)
            }
        }

        override fun getItemCount(): Int {
            return questionList?.size ?: 0
        }

        private fun getMirroredPosition(position: Int): Int {
            return this.questionList?.lastIndex?.minus(position) ?: 0
        }

        internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val examBtnNumber: MaterialButton = itemView.findViewById(R.id.examBtnNumber)
        }
    }
}