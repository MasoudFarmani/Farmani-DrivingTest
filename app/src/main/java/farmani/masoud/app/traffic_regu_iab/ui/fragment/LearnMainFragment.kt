package farmani.masoud.app.traffic_regu_iab.ui.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aigestudio.wheelpicker.WheelPicker
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import com.google.android.material.button.MaterialButton
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.database.entity.RoadSignGroup
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor
import farmani.masoud.app.traffic_regu_iab.extensions.gone
import farmani.masoud.app.traffic_regu_iab.ui.activity.LearnRoadSignActivity
import farmani.masoud.app.traffic_regu_iab.ui.activity.MainActivity
import farmani.masoud.app.traffic_regu_iab.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_learn_main_menu.*

val signGroupNames = listOf("تابلوهای انتظامی", "تابلوهای اخطاری", "تابلوهای اَخباری", "تابلوهای راهنمای مسیر", "تابلوهای محلی", "تابلوهای مکمل")

class LearnMainMenuFragment : Fragment() {

    companion object {

        // order is based on List<String> defined in setupWheelPicker()
        private val signGroupIds = charArrayOf('e', //انتظامی
                'h', // اخطاری
                'i', // اخباری
                'r', // راهنمای مسیر
                'l', // محلی
                'm'// مکمل
        )
    }

    private lateinit var mainActivityViewModel: MainActivityViewModel
    /*رفرنس به گروه انتخاب شده رو نگه داشتم تا در رخداد کلیک btnStart اونو
     * به LearnRoadSignActivity ارسال کنم*/
    private var selectedRoadSignGroup: RoadSignGroup? = null
    private var centerWheelSelectedPosition = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        savedInstanceState?.run { centerWheelSelectedPosition = getInt("centerWheelSelectedPosition") }
        mainActivityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        return inflater.inflate(R.layout.fragment_learn_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvAdapter = setupRecyclerView(rvInfo)
        mainActivityViewModel.selectedRoadSignGroupLive.observe(activity!!, androidx.lifecycle.Observer { selectedItem ->
            selectedItem?.let {
                tvPrompt.gone()
                selectedRoadSignGroup = it
                tvGroupName.text = it.groupName
                rvAdapter.selectedRoadSignGroup = it
            }
        })
        setupWheelPicker(centerWheelPicker)
        btnStart.setOnClickListener {
            selectedRoadSignGroup?.let {
                startActivity(LearnRoadSignActivity.getIntent(activity!!, selectedRoadSignGroup!!))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        prepareShowcaseView(activity!!)
        mainActivityViewModel.updateSelectedSignGroupMutableLive(signGroupIds[centerWheelPicker.currentItemPosition])
    }

    private fun prepareShowcaseView(context: Activity) {
        val wheelPickerShowcase = BubbleShowCaseBuilder(context)
                .title("انتخاب دسته بندی")
                .description("با چرخوندن این چرخونک میتونین دسته بندی مورد نظرتون رو انتخاب کنید")
                .targetView(centerWheelPicker)
                .backgroundColor(context.getThemeColor(R.attr.colorPrimary))
                .showOnce("mainWheelPickerShowCase")

        val tabLayoutShowCase = BubbleShowCaseBuilder(context)
                .title("جابجایی بین سربرگ ها")
                .description("با لمس این بالا یا با کشیدن انگشتتون به چپ و راست صفحه میتونین بین قسمت های مختلف برنامه جابجا بشید")
                .targetView((context as MainActivity).tabLayout)
                .backgroundColor(context.getThemeColor(R.attr.colorPrimary))
                .showOnce("mainTabLayoutShowCase")

        BubbleShowCaseSequence()
                .addShowCase(wheelPickerShowcase)
                .addShowCase(tabLayoutShowCase)
                .show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("centerWheelSelectedPosition", this.centerWheelSelectedPosition)
    }


    private fun setupWheelPicker(wheelPicker: WheelPicker) {
        wheelPicker.typeface = ResourcesCompat.getFont(activity!!, R.font.iran_sans)
        wheelPicker.data = signGroupNames
        wheelPicker.selectedItemPosition = centerWheelSelectedPosition

        wheelPicker.setOnItemSelectedListener { _, _, position ->
            mainActivityViewModel.updateSelectedSignGroupMutableLive(signGroupIds[position])
            /*
                     * نگهداری شماره position انتخاب شده برای ذخیره در زمان
                     * صدازده شدن onSaveInstanceState و بازیابی در زمان
                     * صدا زده شده onCreateView*/
            this.centerWheelSelectedPosition = position
        }
    }

    private fun setupRecyclerView(rv: RecyclerView): RecyclerViewAdapter {
        rv.layoutManager = GridLayoutManager(activity, 2)
        val rvAdapter = RecyclerViewAdapter(activity!!)
        rv.adapter = rvAdapter
        return rvAdapter
    }


    private class RecyclerViewAdapter internal constructor(ctx: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        /*رفرنس به گروه تابلوی انتخاب شده رو نگه داشتم تا در
         * رخداد کلیک btnInfoReview اون رو به LearnRoadSignActivity
         * ارسال کنم*/
        var selectedRoadSignGroup: RoadSignGroup? = null
            set(value) {
                field = value;currentGroupInfoList = value?.toStringArrayList();notifyDataSetChanged()
            }
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
            if (holder is ViewHolder1) {
                holder.textView.text = item
                holder.btnInfoReview.setOnClickListener {
                    if (this.selectedRoadSignGroup != null)
                        inflater.context
                                .startActivity(
                                        LearnRoadSignActivity.getIntent(
                                                inflater.context,
                                                this.selectedRoadSignGroup!!,
                                                true
                                        )
                                )
                }

            } else {
                (holder as ViewHolder).textView.text = item
            }
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun getItemCount(): Int {
            return currentGroupInfoList?.size ?: 0
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
