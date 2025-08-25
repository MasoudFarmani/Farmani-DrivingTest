package farmani.masoud.app.traffic_regu_iab.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import farmani.masoud.app.traffic_regu_iab.util.HelperFunctions
import java.util.*

/*
 * این کلاس به عنوان کلاس عمومی پدر برای کلاس های آداپتر ویوپیجر
 * در ExamActivity و LearnRoadSignActivity وShortExamActivity نوشته شده با هدف استفاده
 * مجدد از متدها و فیلدهایی که عموما طرز نوشتن مشترکی در این کلاس ها دارند
 * پارامتر ENTITY یک type-parameter است که میتونه یکی از
 * انواع entity های ایجاد شده در پروژه باشه مثلا Question  یا ShortQuestion
 * */
abstract class AzeraBaseViewPagerAdapter<ENTITY>(ctx: AppCompatActivity,
                                                 protected var containerVp: ViewPager,
                                                 protected var connectedRv: RecyclerView)
    : PagerAdapter(), ViewPager.OnPageChangeListener {

    protected var entityList: List<ENTITY>? = null
        set(value) {
            field = null
            field = value
            //notifyDataSetChanged();
            /*به جای استفاده از notifyDataSetChanged از خط زیر استفاده میکنم
             * تا بروزرسانی در لیست داده، به سرعت در pagerAdapter منعکس شود*/
            this.containerVp.adapter = this
        }
    protected var layoutInflater: LayoutInflater = LayoutInflater.from(ctx)
    protected var iranLocale: Locale = Locale("fa")

    init {
        //آبجکت همین کلاس رخداد ورق زدن ویوپیجر رو هندل میکنه
        this.containerVp.addOnPageChangeListener(this)
    }

    override fun getCount(): Int {
        return this.entityList?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }


    protected fun getMirroredPosition(position: Int): Int {
        return this.entityList?.lastIndex?.minus(position) ?: 0
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    /*
     * پیامی رو با استفاده از Snackbar به کاربر نمایش میدهد
     * */

    protected fun promptUser(promptMsg: String) {
        HelperFunctions.promptUser(promptMsg, this.containerVp)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    /*
     * با حرکت ویوپیجر ریسایکلر ویو هم به صورت خودکار حرکت کند
     * */
    override fun onPageSelected(rtlPosition: Int) {
        //یادآوری: ایندکس های داده ها در ویوپیجر و در ریسایکلر ویو برعکس هم هستند
        this.connectedRv.smoothScrollToPosition(getMirroredPosition(rtlPosition))
    }

    override fun onPageScrollStateChanged(state: Int) {}
}
