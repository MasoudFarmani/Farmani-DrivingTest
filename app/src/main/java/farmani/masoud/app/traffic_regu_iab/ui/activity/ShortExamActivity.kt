package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortQuestion
import farmani.masoud.app.traffic_regu_iab.ui.AzeraBaseViewPagerAdapter
import farmani.masoud.app.traffic_regu_iab.ui.util.ScreenShot
import farmani.masoud.app.traffic_regu_iab.viewmodel.ShortExamActivityViewModel
import kotlinx.android.synthetic.main.activity_shortexam.*
import java.util.*

class ShortExamActivity : BaseActivity() {

    companion object {

        private lateinit var selectedShortExam: ShortExam
        private var sReviewModeFlag = false

        fun getIntent(ctx: Context, selected: ShortExam): Intent {
            selectedShortExam = selected
            return Intent(ctx, ShortExamActivity::class.java)
        }

        fun getIntent(ctx: Context, selected: ShortExam, reviewMode: Boolean = false): Intent {
            sReviewModeFlag = reviewMode
            return Companion.getIntent(ctx, selected)
        }
    }

    private lateinit var dataHolder: ShortExamActivityViewModel
    /*لیست سوالات کوئری گرفته شده رو کش میکنم که وقتی notLearnedChip چک خورد
    * بعد دوباره uncheck شد مجددا این لیست رو به آداپترهای ویوپیجر وریسایکرویو بدم*/
    private var cachedShortQuestionList: List<ShortQuestion>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortexam)
        /*ساخت آبجکت مدل برای نگهداری داده های اکتیویتی*/
        this.dataHolder = ViewModelProviders.of(this).get(ShortExamActivityViewModel::class.java)
        setSupportActionBar(shortExamToolbar)
        setupAppBar(supportActionBar)
        /*کد راه اندازی ویوپیجر*/
        val vpAdapter = createVpAdapter()
        /*کد راه اندازی ریسایکلرویو*/
        val rvAdapter = setupRecyclerView()
        vpAdapter.dataHolder = this.dataHolder
        /*انتظار برای بازگشت لیست سوالات کوتاه که از دیتابیس استخراج میشود
         * و واریز مقدار برگشتی به آداپترهای ویوپیجر و ریسایکلر ویو*/
        this.dataHolder.selectShortQuestionListLive(selectedShortExam, sReviewModeFlag)
                .observe(this, Observer<List<ShortQuestion>> { returnedShortQuestionList ->
                    if (returnedShortQuestionList != null) {
                        dataHolder.emptyListFlag.value = returnedShortQuestionList.isEmpty()
                        if (sReviewModeFlag) {
                            var alreadyLearnedCount = 0
                            for (shq in returnedShortQuestionList) if (shq.profile.isLearned) alreadyLearnedCount++
                            this.dataHolder.alreadyLearnedCount = alreadyLearnedCount
                        }
                        val toolbarTitle =
                                if (sReviewModeFlag) "مرور " + selectedShortExam.name
                                else selectedShortExam.name
                        shortExamToolbar.title = toolbarTitle
                        this.cachedShortQuestionList = returnedShortQuestionList
                        vpAdapter.setShortQuestionList(returnedShortQuestionList)
                        /*ارسال تعداد سوالات به آداپتر ریسایکلرویو*/
                        rvAdapter.shortQuestionCount = returnedShortQuestionList.size
                    }
                })

        shortExamChipFilterNotLearned.setOnCheckedChangeListener { _, isChecked ->
            if (cachedShortQuestionList != null)
                if (isChecked) {
                    val notLearnedShortQuestions = ArrayList<ShortQuestion>()
                    for (shq in cachedShortQuestionList!!) if (!shq.profile.isLearned) notLearnedShortQuestions.add(shq)
                    dataHolder.emptyListFlag.value = notLearnedShortQuestions.isEmpty()
                    vpAdapter.setShortQuestionList(notLearnedShortQuestions)
                    rvAdapter.shortQuestionCount = notLearnedShortQuestions.size
                } else {
                    vpAdapter.setShortQuestionList(cachedShortQuestionList!!)
                    rvAdapter.shortQuestionCount = cachedShortQuestionList!!.size
                }

        }

        dataHolder.emptyListFlag.observe(this, Observer { emptyList ->
            shortExamEmptyListTvLabel.visibility = if (emptyList) View.VISIBLE
            else View.GONE
        })
    }

    override fun onStop() {
        super.onStop()
        dataHolder.persistShortQuestionList()
        dataHolder.persistShortExam()
        sReviewModeFlag = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.azera_short_exam_activity_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_short_exam_activity_exit, android.R.id.home -> this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /*راه اندازی ویوپیجر
     * نکته: رفرنس ریسایکلرویو برای اسکرول خودکارش به شماره
     * صفحه ای که کاربر داره اونو مشاهده میکنه لازمه.
     * نکته: اینجا فقط نمونه ای از آداپتر ویوپیجر ساخته میشه
     * و ازونجاییکه رفرنس ویوپیجر رو به نمونه آداپترش ارسال کردم
     * هر موقع لیست سوالات توسط کوئری از دیتابیس برگشت داده شد
     * همونجا shortExamVp.setAdapter رو صدا میزنم و نمونه
     * آداپتر رو بهش ارسال میکنم*/
    private fun createVpAdapter() = ViewPagerAdapter(this, shortExamVp, shortExamRv)

    /*راه اندازی ریسایکلرویو
     * نکته: رفرنیس ویوپیجر برای ورق زدن ویوپیجر به صفحه مورد نظر زمانیکه کاربر
     * برروی آیتم ریسایکلر ویو کلیک میکند لازم است*/
    private fun setupRecyclerView(): RecyclerViewAdapter {
        val rvAdapter = RecyclerViewAdapter(this, shortExamVp)
        shortExamRv.adapter = rvAdapter
        shortExamRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        return rvAdapter
    }

    private fun setupAppBar(toolbar: ActionBar?) {
        toolbar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setHomeButtonEnabled(true)
        toolbar?.setDisplayShowTitleEnabled(true)
    }

    private class ViewPagerAdapter(ctx: AppCompatActivity,
                                   containerVp: ViewPager,
                                   connectedRv: RecyclerView)
        : AzeraBaseViewPagerAdapter<ShortQuestion>(ctx, containerVp, connectedRv) {

        var dataHolder: ShortExamActivityViewModel? = null

        override fun instantiateItem(container: ViewGroup, ltrPosition: Int): Any {
            /*فقط در صورتیکه لیست داده ها null نباشد ویو ها رو بساز*/
            if (this.entityList != null) {
                /*دریافت سوال متناظر با شماره صفحه(ایندکس ها برعکس هستند)*/
                val currentShortQuestion = this.entityList!![getMirroredPosition(ltrPosition)]
                /*تبدیل فایل xml رابط کابری به آبجکت جاوا*/
                val rootView = layoutInflater.inflate(R.layout.item_vp_shortexam, container, false)
                /*نمایش شماره سوال*/
                val shortExamTVNumber = rootView.findViewById(R.id.shortExamTvNumber) as TextView
                shortExamTVNumber.text = String.format(
                        iranLocale,
                        "سوال شماره %d از %d",
                        getMirroredPosition(ltrPosition) + 1, this.entityList!!.size)

                /*نمایش متن صورت سوال*/
                val shortExamTvQuestion = rootView.findViewById(R.id.shortExamTvQuestion) as TextView
                shortExamTvQuestion.text = String.format(iranLocale, "%s", currentShortQuestion.question)
                /*نمایش متن پاسخ سوال*/
                val shortExamTvAnswer = rootView.findViewById(R.id.shortExamTvAnswer) as TextView
                shortExamTvAnswer.text = String.format(iranLocale, "%s", currentShortQuestion.answer)
                /*تعریف سازوکار نمایش/عدم نمایش پاسخ سوال، زمانیکه کاربر
                 * بر روی آیکون نمایش پاسخ سوال کلیک میکند، آیکون مخفی و متن پاسخ سوال به
                 * همراه دو باتن که یکیشون رو کاربر با توجه به اینکه پاسخ رو قبل از نمایش
                 * بلد بوده یا نه، میتونه انتخاب کنه، آشکار میشه*/
                val shortExamBtnShowAnswer = rootView.findViewById(R.id.shortExamBtnShowAnswer) as MaterialButton
                //برای پیدا/پنهان کردن ایتم های پاسخ سوال
                val shortExamAnswerGroup = rootView.findViewById(R.id.shortExamAnswerGroup) as Group
                shortExamBtnShowAnswer.setOnClickListener {
                    shortExamBtnShowAnswer.visibility = View.INVISIBLE
                    shortExamAnswerGroup.visibility = View.VISIBLE
                }

                /*نشاندار کردن سوال در حال نمایش*/
                val shortExamChbBookmark = rootView.findViewById(R.id.shortExamChbBookmark) as MaterialCheckBox
                /*اگه سوال مورد نظر از قبل بوکمارک بود، چک باکس رو چک بزن*/
                shortExamChbBookmark.isChecked = currentShortQuestion.profile.isBookmarked

                shortExamChbBookmark.setOnClickListener { clickedView ->
                    clickedView as CheckBox
                    promptUser(if (clickedView.isChecked) "سوال به لیست مرور اضافه شد" else "سوال از لیست مرور حذف شد")
                }

                shortExamChbBookmark.setOnCheckedChangeListener { _, isChecked ->
                    currentShortQuestion.profile.isBookmarked = isChecked
                }
                /*تعریف سازوکاری که با انتخاب هرکدام از گزینه های
                 * "بلد بودم" یا "بلدنبودم" توسط کاربر بعد از نمایش پاسخ
                 * باید اتفاق بیفتد*/
                val shortExamBtnKnow = rootView.findViewById(R.id.shortExamBtnKnow) as MaterialButton
                val shortExamBtnNotKnow = rootView.findViewById(R.id.shortExamBtnNotKnow) as MaterialButton
                val clickListener = View.OnClickListener { view: View ->
                    currentShortQuestion.profile.isLearned = view.id == R.id.shortExamBtnKnow
                    shortExamChbBookmark.isChecked = view.id == R.id.shortExamBtnNotKnow

                    promptUser(if (view.id == R.id.shortExamBtnKnow) "آفرین! این سوال رو یاد گرفتی" else "بازم مرور کن تا یاد بگیری")
                    /*کمی با تاخیر ویوپیجر رو ورق بزن تا پیغام رو ببینه*/
                    App.uiHandler.postDelayed({
                        containerVp.setCurrentItem(if (ltrPosition == 0) entityList!!.lastIndex else ltrPosition - 1, true)
                    }, 800)
                }
                shortExamBtnKnow.setOnClickListener(clickListener)
                shortExamBtnNotKnow.setOnClickListener(clickListener)
                /*--------------------------------------------------------------------------------*/
                //رفرنس به اکشن های ویوپیجر
                val shortExamIbNote = rootView.findViewById(R.id.shortExamIbNote) as ImageButton
                shortExamIbNote.setOnClickListener {
                    val materialDialog = MaterialDialog(layoutInflater.context).show {
                        input(
                                prefill = currentShortQuestion.userNote,
                                inputType = InputType.TYPE_CLASS_TEXT,
                                maxLength = 100,
                                allowEmpty = true
                        ) { _: MaterialDialog, charSequence: CharSequence ->
                            currentShortQuestion.userNote = charSequence.toString()
                        }
                        icon(R.drawable.azera_ic_edit_outline)
                        title(text = "یادداشت بنویس")
                        positiveButton(text = "ذخیره")
                        negativeButton(text = "بیخیال")
                    }
                    val inputField = materialDialog.getInputField()
                    inputField.setSingleLine(false)
                    inputField.setBackgroundColor(ContextCompat.getColor(layoutInflater.context, android.R.color.transparent))
                }
                /*به اشتراک گذاری کارت سوال*/
                val shortExamIbShare = rootView.findViewById(R.id.shortExamIbShare) as ImageButton
                shortExamIbShare.setOnClickListener { ScreenShot.shareView(containerVp.context, rootView) }



                container.addView(rootView)
                return rootView
            } else {
                return super.instantiateItem(container, ltrPosition)
            }
        }

        override fun onPageSelected(rtlPosition: Int) {
            super.onPageSelected(rtlPosition)
            this.dataHolder?.lastPositionDisplayed = rtlPosition
        }

        fun setShortQuestionList(shortQuestionList: List<ShortQuestion>) {
            /*با صدازده شدن این متد، بصورت خودکار این آداپتر روی ویوپیجر setAdapter میشه*/
            super.entityList = shortQuestionList
            displayCorrectPage(shortQuestionList)
        }

        private fun displayCorrectPage(shortQuestionList: List<ShortQuestion>) {
            if (shortQuestionList.isNotEmpty()) {
                val lastPositionDisplayed = dataHolder?.lastPositionDisplayed ?: 0
                this.containerVp.currentItem =
                        if (lastPositionDisplayed == 0) shortQuestionList.lastIndex else lastPositionDisplayed
            }
        }

    }

    private class RecyclerViewAdapter(ctx: Context, private val shortExamVp: ViewPager)
        : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
        private val iranLocale: Locale = Locale("fa")
        // به اندازه تعداد سوالات کوتاه، باتن بساز
        var shortQuestionCount: Int = 0
            set(value) {
                field = value; notifyDataSetChanged()
            }

        override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int)
                : RecyclerViewAdapter.ViewHolder {

            val itemView = layoutInflater.inflate(R.layout.item_rv_exam, viewGroup, false)
            return RecyclerViewAdapter.ViewHolder(itemView)
        }

        override fun onBindViewHolder(viewHolder: RecyclerViewAdapter.ViewHolder, ltrPosition: Int) {
            viewHolder.examBtnNumber.text = String.format(iranLocale, "%d", ltrPosition + 1)
            viewHolder.examBtnNumber.setOnClickListener {
                //چون ایندکس های ویوپیجر و ریسایکلر ویو برعکس هستند
                //ابتدا ایندکس رو برعکس کن بعد ارسال کن به ویوپیجر
                shortExamVp.setCurrentItem(getMirroredPosition(ltrPosition), true)
            }
        }

        override fun getItemCount(): Int {
            return shortQuestionCount
        }

        private fun getMirroredPosition(position: Int): Int {
            return shortQuestionCount - 1 - position
        }

        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val examBtnNumber: Button = itemView.findViewById(R.id.examBtnNumber)

        }
    }
}
