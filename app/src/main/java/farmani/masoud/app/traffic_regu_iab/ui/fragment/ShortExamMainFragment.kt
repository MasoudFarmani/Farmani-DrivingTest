package farmani.masoud.app.traffic_regu_iab.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aigestudio.wheelpicker.WheelPicker
import com.google.android.material.button.MaterialButton
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App.Companion.iranLocale
import farmani.masoud.app.traffic_regu_iab.database.entity.ShortExam
import farmani.masoud.app.traffic_regu_iab.extensions.gone
import farmani.masoud.app.traffic_regu_iab.ui.activity.ShortExamActivity
import farmani.masoud.app.traffic_regu_iab.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_shortexam_main_menu.*

val shortExamCategoryNames = listOf(
        ShortExam.Category.COMPREHENSIVE.toString(),
        ShortExam.Category.TECHNICAL.toString()
)
val shortExamComprehensiveItems = List(4) {
    String.format(iranLocale, "بخش %d", it + 1)
}
val shortExamTechnicalItems = listOf(
        "آشنایی با سیستم های فنی",
        "سرویس و نگهداری"
)

class ShortExamMainMenuFragment : Fragment() {

    private lateinit var mainActivityViewModel: MainActivityViewModel

    /*رفرنس به گروه انتخاب شده رو نگه داشتم تا در رخداد
     * کلیک btnStart اون رو به اکتیویتی ShortExamActivity ارسال کنم*/
    private var selectedShortExam: ShortExam? = null

    private var leftWheelSelectedPosition = 0
    private var rightWheelSelectedPosition = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        if (savedInstanceState != null) {
            this.leftWheelSelectedPosition = savedInstanceState.getInt("leftWheelSelectedPosition")
            this.rightWheelSelectedPosition = savedInstanceState.getInt("rightWheelSelectedPosition")
        }
        return inflater.inflate(R.layout.fragment_shortexam_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvAdapter = setupRecyclerView(rvInfo)
        mainActivityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        mainActivityViewModel.selectedShortExamLive.observe(activity!!, Observer { shortExam ->
            shortExam?.let {
                tvPrompt.gone()
                selectedShortExam = it
                tvGroupName.text = it.name
                rvAdapter.setSelectedShortExam(it)
            }
        })
        setupWheelPickers(leftWheelPicker, rightWheelPicker)
        btnStart.setOnClickListener {
            selectedShortExam?.let { startActivity(ShortExamActivity.getIntent(activity!!, it)) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("leftWheelSelectedPosition", this.leftWheelSelectedPosition)
        outState.putInt("rightWheelSelectedPosition", this.rightWheelSelectedPosition)
    }

    private fun setupWheelPickers(left: WheelPicker, right: WheelPicker) {

        left.typeface = ResourcesCompat.getFont(activity!!, R.font.iran_sans)
        right.typeface = ResourcesCompat.getFont(activity!!, R.font.iran_sans)
        right.data = shortExamCategoryNames
        right.selectedItemPosition = this.rightWheelSelectedPosition
        when (right.currentItemPosition) {
            0//جامع
            -> left.data = shortExamComprehensiveItems
            1//فنی
            -> left.data = shortExamTechnicalItems
        }

        /*اگر صفحه rotate بشه یا کاربر با ورق زدن ویوپیجر،
         * این فرگمنت رو ترک کنه و دوباره برگرده، هرکدام
         * از WheelPicker ها مجددا به آخرین position خودشون
         * قبل از خروج از فرگمنت برمیگردند چون آخرین position
         * اونها در onSaveInstanceState ذخیره شده و
         * و در onCreateView بازیابی شده.اگه مقداری هم ذخیره
         * نشده باشه هم مقدار پیشفرضشون 0 هست*/

        left.selectedItemPosition = this.leftWheelSelectedPosition

        mainActivityViewModel
                .updateSelectedShortExamMutableLive(
                        ShortExam.Category.values()[right.currentItemPosition],
                        left.currentItemPosition + 1
                )
        val itemSelectedListener = WheelPicker.OnItemSelectedListener { picker, _, position ->
            if (picker.id == R.id.rightWheelPicker) {
                this.rightWheelSelectedPosition = position
                when (position) {
                    0 -> left.data = shortExamComprehensiveItems
                    1 -> left.data = shortExamTechnicalItems
                }
            } else
                this.leftWheelSelectedPosition = position
            mainActivityViewModel
                    .updateSelectedShortExamMutableLive(
                            ShortExam.Category.values()[right.currentItemPosition],
                            left.currentItemPosition + 1
                    )
        }

        right.setOnItemSelectedListener(itemSelectedListener)
        left.setOnItemSelectedListener(itemSelectedListener)
    }

    private fun setupRecyclerView(rv: RecyclerView): RecyclerViewAdapter {
        rv.layoutManager = GridLayoutManager(activity, 2)
        val rvAdapter = RecyclerViewAdapter(activity!!)
        rv.adapter = rvAdapter
        return rvAdapter
    }

    private class RecyclerViewAdapter internal constructor(ctx: Activity)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


        /*رفرنس به گروه  انتخاب شده رو نگه داشتم تا در
         * رخداد کلیک btnInfoReview اون رو به ShortExamActivity
         * ارسال کنم*/
        private var selectedShortExam: ShortExam? = null
        private var currentGroupInfoList: List<String>? = null
        private val inflater: LayoutInflater = LayoutInflater.from(ctx)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 2) {
                RecyclerViewAdapter.ViewHolder1(
                        inflater.inflate(R.layout.item_rv_info_cell_review_action,
                                parent,
                                false))
            } else {
                RecyclerViewAdapter.ViewHolder(
                        inflater.inflate(R.layout.item_rv_info_cell,
                                parent,
                                false))
            }
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = currentGroupInfoList!![position]
            if (holder.itemViewType == 2) {
                (holder as RecyclerViewAdapter.ViewHolder1).textView.text = item
                holder.btnInfoReview.setOnClickListener {
                    selectedShortExam?.let {
                        inflater.context
                                .startActivity(
                                        ShortExamActivity.getIntent(
                                                inflater.context,
                                                it,
                                                true
                                        )
                                )
                    }
                }

            } else {
                (holder as RecyclerViewAdapter.ViewHolder).textView.text = item
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return if (currentGroupInfoList != null) currentGroupInfoList!!.size else 0
        }

        fun setSelectedShortExam(shortExam: ShortExam?) {
            this.selectedShortExam = shortExam
            if (shortExam != null)
                this.currentGroupInfoList = shortExam.toStringArrayList()
            notifyDataSetChanged()
        }

        internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.tvInfo)

        }

        internal class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.tvInfo)
            val btnInfoReview: MaterialButton = itemView.findViewById(R.id.btnInfoReview)
        }
    }
}
