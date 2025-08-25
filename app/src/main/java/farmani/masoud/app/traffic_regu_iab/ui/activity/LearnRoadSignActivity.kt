package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSign
import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSignGroup
import farmani.masoud.app.traffic_regu_iab.ui.AzeraBaseViewPagerAdapter
import farmani.masoud.app.traffic_regu_iab.ui.fragment.preference.LearnRoadSignSettingFragment
import farmani.masoud.app.traffic_regu_iab.ui.util.ScreenShot
import farmani.masoud.app.traffic_regu_iab.util.HelperFunctions
import farmani.masoud.app.traffic_regu_iab.viewmodel.LearnRoadSignActivityViewModel
import kotlinx.android.synthetic.main.activity_learn_roadsign.*
import java.util.*

class LearnRoadSignActivity : BaseActivity() {

    companion object {

        private lateinit var selectedRoadSignGroup: RoadSignGroup
        private var sReviewModeFlag = false

        fun getIntent(ctx: Context, roadSignGroup: RoadSignGroup): Intent {
            selectedRoadSignGroup = roadSignGroup
            return Intent(ctx, LearnRoadSignActivity::class.java)
        }

        fun getIntent(ctx: Context, roadSignGroup: RoadSignGroup, reviewMode: Boolean): Intent {
            sReviewModeFlag = reviewMode
            selectedRoadSignGroup = roadSignGroup
            return Intent(ctx, LearnRoadSignActivity::class.java)
        }
    }

    private lateinit var dataHolder: LearnRoadSignActivityViewModel
    private var signCount: Int = 0
    /*از اونجاییکه ویوپیجر رو از راست به چپ داریم ورق میزنیم و
     * ویوپیجر خودش از حالت راست به چپ پشتیبانی نمیکنه ، بنابراین
     * در صورت فعال بودن حالت ورق زدن خودکار، شماره صفحات رو یکی یکی
     * کم میکنیم تا اینکه به شماره 0 برسیم و سپس به شماره آخر میرویم*/
    private val autoPageRun = Runnable {
        signVp.currentItem = if (signVp.currentItem > 0) signVp.currentItem - 1 else signCount - 1
        autoPage(true)
    }
    private var cachedRoadSignList: List<RoadSign>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_roadsign)
        dataHolder = ViewModelProviders.of(this).get(LearnRoadSignActivityViewModel::class.java)
        val vpAdapter = createVpAdapter()
        val rvAdapter = setupRecyclerView()
        /*درصورتیکه این خط وجود نداشته باشه، تغییرات شماره
         * صفحه ویوپیجر در ویومدل ذخیره نمیشه و طبیعتا در
         * دیتابیس هم بروز نخواهد شد*/
        vpAdapter.dataHolder = this.dataHolder
        setSupportActionBar(signToolbar)
        setupAppBar(supportActionBar)
        // هر موقع لیست تابلوها رو دیتابیس گرفتی لیست رو به آداپترها ارسال کن
        dataHolder.selectRoadSignList(
                selectedRoadSignGroup,
                sReviewModeFlag).observe(this, Observer<List<RoadSign>> { returnedRoadSignList ->

            if (returnedRoadSignList != null) {
                dataHolder.emptyListFlag.value = returnedRoadSignList.isEmpty()
                /*اگه حالت مرور فعال بود، همین اول کار، تعداد
                 * تابلوهای یاد گرفته شده رو میشمرم تا با
                 * بدست آوردن اختلاف این مقدار با مقدار شمارش
                 * شده در آخر کار، تعداد تابلوهای یادگرفته شده
                 * این دسته بندی رو به درستی ذخیره کنم. دلیلشم
                 * اینه که ممکنه بعضی از تابلوهای یادگرفته شده
                 * توی لیست تابلوهای بوکمارک شده نباشندو  همینطور
                 * ممکنه کاربر وضعیت یادگرفته شده بودن تابلوی
                 * لیست بوکمارک رو تغییر بده*/
                if (sReviewModeFlag) {
                    var alreadyLearnedCount = 0
                    for (rs in returnedRoadSignList) if (rs.profile.isLearned) alreadyLearnedCount++
                    dataHolder.alreadyLearnedCount = alreadyLearnedCount
                }
                this.cachedRoadSignList = returnedRoadSignList
                this.signCount = returnedRoadSignList.size
                /*نمایش نام دسته بندی انتخاب شده در عنوان تولبار*/
                val toolbarTitle = if (sReviewModeFlag)
                    "مرور " + selectedRoadSignGroup.groupName
                else
                    selectedRoadSignGroup.groupName
                signToolbar.title = toolbarTitle
                vpAdapter.setRoadSignList(returnedRoadSignList)
                rvAdapter.setRoadSignList(returnedRoadSignList)
            }
        })

        signChipFilterNotLearned.setOnCheckedChangeListener { _, isChecked ->
            cachedRoadSignList?.let {
                if (isChecked) {
                    val notLearnedRoadSignList = ArrayList<RoadSign>()
                    for (rs in it) {
                        if (!rs.profile.isLearned) {
                            notLearnedRoadSignList.add(rs)
                        }
                    }
                    dataHolder.emptyListFlag.value = notLearnedRoadSignList.isEmpty()
                    vpAdapter.setRoadSignList(notLearnedRoadSignList)
                    rvAdapter.setRoadSignList(notLearnedRoadSignList)
                } else {
                    vpAdapter.setRoadSignList(it)
                    rvAdapter.setRoadSignList(it)
                }
            }
        }
        signSwAutoPage.setOnCheckedChangeListener { _, isChecked ->
            /*در صورت روشن/خاموش شدن کلید، تغییرات رو در ویومدل ذخیره کن*/
            this.dataHolder.setValueAutoPageFlagLive(isChecked)
            HelperFunctions.promptUser(
                    if (isChecked) "ورق زدن خودکار، فعال شد" else "ورق زدن خودکار، غیرفعال شد",
                    signVp)
        }

        /*مقدار بول ذخیره شده در ویومدل رو رصد کن ، در صورت تغییر
         * عملیات مورد نظر رو انجام بده*/
        dataHolder.autoPageFlagLive.observe(this, Observer<Boolean> { autoPageFlag ->
            autoPage(autoPageFlag ?: false)
            //signSwAutoPage.isChecked = autoPageFlag ?: false
        })

        dataHolder.emptyListFlag.observe(this, Observer { emptyList ->
            with(signEmptyListTvLabel) {
                visibility = if (emptyList) View.VISIBLE
                else View.GONE
            }
        })
    }

    override fun onPause() {
        super.onPause()
        autoPage(false)
    }

    override fun onResume() {
        super.onResume()
        autoPage(dataHolder.autoPageFlagLive.value ?: false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.azera_learn_road_sign_activtiy_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item != null) {
            when (item.itemId) {
                /*کلیک روی [up\back button]*/
                android.R.id.home,
                R.id.menu_learn_road_sign_exit -> this.finish()
                R.id.setting -> supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.learnRoadSignRootContainer, LearnRoadSignSettingFragment())
                        .addToBackStack(null)
                        .commit()
            }
            true
        } else {
            false
        }
    }

    private fun autoPage(flag: Boolean) {
        val shprefs = PreferenceManager.getDefaultSharedPreferences(this)
        val autoPageDelay = shprefs.getInt("auto_page_time", 3)
        if (flag) {
            App.uiHandler.removeCallbacks(autoPageRun)
            App.uiHandler.postDelayed(autoPageRun, (autoPageDelay * 1000).toLong())
        } else {
            App.uiHandler.removeCallbacks(autoPageRun)
        }
    }

    override fun onStop() {
        super.onStop()
        this.dataHolder.persistRoadSignList()
        this.dataHolder.persistRoadSignGroup()
        /*بازنشانی مقدار flag، چون از نوع static است پس از هربار خروج
         * از صفحه، لازم است*/
        sReviewModeFlag = false
    }

    private fun setupAppBar(toolbar: ActionBar?) {
        toolbar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setHomeButtonEnabled(true)
        toolbar?.setDisplayShowTitleEnabled(true)
    }

    //آداپتر ویوپیجر تحت شرایطی باید بتونه ویوپیجر(ورق زدن خودکار)
    // و ریسایکلرویو(در رخداد انتخاب صفحه) رو ورق
    //بزنه پس به رفرنس هاشون نیاز داره
    /*نیازی به صدا زدن setAdapter در اینجا نداریم چون
     * به صورت خودکار، اینکار در setEntityList کلاس آداپتری
     * که نوشتم انجام میشود*/
    //signVp.setAdapter(vpAdapter);
    private fun createVpAdapter(): SignViewPagerAdapter {
        return SignViewPagerAdapter(this, signVp, signRv)
    }

    private fun setupRecyclerView(): SignRecyclerViewAdapter {
        val rvAdapter = SignRecyclerViewAdapter(this, signVp)
        signRv.adapter = rvAdapter
        signRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        return rvAdapter
    }

    private class SignViewPagerAdapter(ctx: AppCompatActivity,
                                       containerVp: ViewPager,
                                       connectedRv: RecyclerView)
        : AzeraBaseViewPagerAdapter<RoadSign>(ctx, containerVp, connectedRv) {

        /*رفرنس به آبجکت ویومدل. چون آبجکت ویوپیجر آداپتر
         * یک شنونده برای رخداد تغییر صفحه ویوپیجر هم هست
         * این رفرنس رو بهش پاس میدم تابتونه با هربار
         * تغییر صفحه ویوپیجر، شماره اون صفحه رو در ویومدل ذخیره کنه*/
        var dataHolder: LearnRoadSignActivityViewModel? = null

        fun setRoadSignList(roadSignList: List<RoadSign>) {
            super.entityList = roadSignList
            displayCorrectPage(roadSignList)
        }

        private fun displayCorrectPage(roadSignList: List<RoadSign>) {
            if (roadSignList.isNotEmpty()) {
                val lastPositionDisplayed = dataHolder?.lastPositionDisplayed ?: 0
                this.containerVp.currentItem =
                        if (lastPositionDisplayed == 0) roadSignList.lastIndex else lastPositionDisplayed
            }
        }

        override fun instantiateItem(container: ViewGroup, ltrPosition: Int): Any {
            //فقط در صورتیکه لیست تابلوها لود شده بود ویو رو بساز
            if (this.entityList != null) {
                val currentRoadSign = this.entityList!![getMirroredPosition(ltrPosition)]
                val rootView = layoutInflater.inflate(R.layout.item_vp_roadsign, container, false)
                val signTvNumber = rootView.findViewById(R.id.signTvNumber) as TextView//نمایش شماره تابلو
                val signIv = rootView.findViewById(R.id.signIv) as ImageView//نمایش تصویر تابلو
                val signTvName = rootView.findViewById(R.id.signTvName) as TextView//نمایش نام تابلو
                val signIbShare = rootView.findViewById(R.id.signIbShare) as ImageButton
                val signChbBookmark = rootView.findViewById(R.id.signChbBookmark) as MaterialCheckBox
                val signChipLearn = rootView.findViewById(R.id.signChipLearn) as Chip

                signIbShare.setOnClickListener { ScreenShot.shareView(this.containerVp.context, rootView) }

                signChbBookmark.setOnClickListener { view ->
                    val checkBox = view as CheckBox
                    currentRoadSign.profile.isBookmarked = checkBox.isChecked
                    promptUser(if (checkBox.isChecked) "تابلو به لیست مرور اضافه شد" else "تابلو از لیست مرور حذف شد")
                }

                signChipLearn.setOnClickListener { view ->
                    val chip = view as Chip
                    currentRoadSign.profile.isLearned = chip.isChecked
                    promptUser(if (chip.isChecked) "آفرین، این تابلو رو یادگرفتی" else "با مرور بیشتر این تابلو رو هم یاد می گیری")
                }

                signTvNumber.text = String.format(
                        iranLocale,
                        "تابلوی شماره %d از %d",
                        getMirroredPosition(ltrPosition) + 1, this.entityList?.size)

                signTvName.text = String.format(iranLocale, "%s", currentRoadSign.name)
                Glide.with(rootView).load(currentRoadSign.imageId).into(signIv)
                //اگر تابلو قبلا بوکمارک شده بود چک باکس رو تیک بزن
                signChbBookmark.isChecked = currentRoadSign.profile.isBookmarked
                //اگر تابلو قبلا یادگرفته شده بود چک باکس رو تیک بزن
                signChipLearn.isChecked = currentRoadSign.profile.isLearned
                container.addView(rootView)
                return rootView
            } else {
                return super.instantiateItem(container, ltrPosition)
            }
        }

        override fun onPageSelected(rtlPosition: Int) {
            super.onPageSelected(rtlPosition)
            dataHolder?.lastPositionDisplayed = rtlPosition
        }
    }

    private class SignRecyclerViewAdapter(ctx: Context, private val signVp: ViewPager)
        : RecyclerView.Adapter<SignRecyclerViewAdapter.SignViewHolder>() {

        private var roadSignList: List<RoadSign>? = null
        private val layoutInflater: LayoutInflater = LayoutInflater.from(ctx)

        private fun getMirroredPosition(position: Int): Int {
            return roadSignList?.size?.minus(1 + position) ?: 0
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): SignViewHolder {
            val rootView = layoutInflater.inflate(R.layout.item_rv_roadsign, viewGroup, false)
            return SignViewHolder(rootView)
        }

        override fun onBindViewHolder(signViewHolder: SignViewHolder, position: Int) {
            if (roadSignList != null) {
                signViewHolder.signMiniIv.setImageResource(roadSignList!![position].imageId)
                signViewHolder.signMiniIv.setOnClickListener { signVp.currentItem = getMirroredPosition(position) }
            }
        }

        fun setRoadSignList(roadSignList: List<RoadSign>?) {
            this.roadSignList = roadSignList
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return if (roadSignList != null) roadSignList!!.size else 0
        }

        internal class SignViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val signMiniIv: ImageView = itemView.findViewById(R.id.signMiniIv)
        }
    }
}
