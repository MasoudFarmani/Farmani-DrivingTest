package farmani.masoud.app.traffic_regu_iab.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.button.MaterialButton
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App.Companion.iranLocale
import farmani.masoud.app.traffic_regu_iab.viewmodel.ExamActivityViewModel
import java.util.*


/*
 * این کلاس داده های نتیجه آزمون که یک ارایه 3 عضوی به ترتیب،
 * حاوی : تعداد پاسخ درست، تعداد پاسخ نادرست، تعداد بدون پاسخ
 * هست رو در قالب یک نمودار دایره ای نمایش میده و پیامی رو
 * در رابطه به اینکه کاربر آزمون رو قبول شده یا نه مینویسه
 * */

class ExamResultFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_exam_result, container, false)
        val examResultChart = rootView.findViewById<PieChart>(R.id.examResultChart)// نمودار نتایج
        val examResultTvMsg = rootView.findViewById<TextView>(R.id.examResultTvMsg)//پیغام به کاربر
        val examResultBtnReturn = rootView.findViewById<MaterialButton>(R.id.examResultBtnReturn)// باتن برای برگشت به صفحه آزمون
        examResultBtnReturn.setOnClickListener {
            val supportFragmentManager = activity!!.supportFragmentManager

            supportFragmentManager
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .remove(this)
                    .commit()

            supportFragmentManager.popBackStack("result", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        /*
         * برای جلوگیری از کلیک شدن آیتم های اکتیویتی زمانیکه این فرگمنت
         * در حال نمایش است، هرگونه رخداد لمس کاربر رو توسط rootView این فرگمنت مصرف میکنیم
         * */

        rootView.findViewById<View>(R.id.examResultRoot).setOnTouchListener { _, _ -> true }

        /*
         * دریافت رفرنس به examActivityViewModel برای دریافت
         * آرایه نتایج آزمون به صورت LiveData و استخراج داده ها
         * زمانیکه آداپتر ویوپیجر در ExamActivity کار محاسبه
         * نتایج و قرار دادنشان در ویو مدل رو انجام داد
         * */

        val examActivityViewModel = ViewModelProviders.of(activity!!).get(ExamActivityViewModel::class.java)
        examActivityViewModel.examResultLive.observe(
                activity as FragmentActivity,
                Observer<IntArray> { examResultArray ->
                    val correct = examResultArray[0]
                    val wrong = examResultArray[1]
                    val white = examResultArray[2]

                    /*
                     * اگر کاربر به بیش از 27 سوال پاسخ درست داده بود، پیام تبریک
                     * در غیراینصورت و درصورتیکه تعداد غلط ها بیش از 20 عدد نبود
                     * به کاربر تعداد سوالاتی که باید درست جواب میداد تا قبول میشد رو اطلاع بده
                     * */

                    val examResultMsg: String
                    val examResultStatus: String//وضعیت نتیجه آزمون - قبول یا مردود که در وسط نمودار نمایش داده میشه
                    val requireCorrectToPass = resources.getInteger(R.integer.require_correct_to_pass)
                    when {
                        correct >= requireCorrectToPass -> {
                            examResultMsg = getString(R.string.exam_result_message_passed)
                            examResultStatus = getString(R.string.exam_result_status_passed)
                        }

                        correct in 20..requireCorrectToPass -> {
                            examResultMsg = String.format(
                                    iranLocale,
                                    getString(R.string.exam_result_message_failed),
                                    requireCorrectToPass - correct)
                            examResultStatus = getString(R.string.exam_result_status_failed)
                        }
                        correct in 10..20 -> {
                            examResultMsg = String.format(
                                    iranLocale,
                                    "داری خوب پیشرفت میکنی، به %d سوال پاسخ درست دادی ولی برای قبولی لازمه حداقل %d سوال رو درست پاسخ بدی. ادامه بده حتما میتونی",
                                    correct, requireCorrectToPass
                            )
                            examResultStatus = getString(R.string.exam_result_status_failed)
                        }
                        else -> {
                            examResultMsg = "تعداد ${if (white > wrong) "سوالات پاسخ نداده" else "پاسخ اشتباه"} خیلی زیاده. بازم آزمون بده ولی با دقت بیشتر. حتما میتونی با تمرینِ کافی قبول بشی"
                            examResultStatus = getString(R.string.exam_result_status_failed)
                        }
                    }
                    examResultTvMsg.text = examResultMsg
                    setupResultChart(context!!, examResultChart, examResultStatus, correct, wrong, white)
                    drawResultChart(examResultChart)
                })

        return rootView
    }

    private fun drawResultChart(pieChart: PieChart) {
        pieChart.animateY(3000, Easing.EaseOutBack)
        pieChart.invalidate()
    }

    private fun setupResultChart(ctx: Context,
                                 pieChart: PieChart,
                                 examStatus: String,
                                 correct: Int, wrong: Int, white: Int) {
        val chartEntryList = createChartEntryList(correct, wrong, white)
        val chartDataSet = createChartDataSet(ctx, chartEntryList)
        val chartData = PieData(chartDataSet)
        chartData.setValueFormatter { value, _, _, _ ->
            if (value == 0f) "" else String.format(iranLocale, "%.0f سوال", value)
        }
        chartData.setValueTextSize(16f)
        pieChart.centerText = examStatus
        pieChart.setCenterTextColor(
                if (examStatus == getString(R.string.exam_result_status_passed))
                    ContextCompat.getColor(ctx, R.color.azeraColorGreen)
                else
                    ContextCompat.getColor(ctx, R.color.azeraColorRed)
        )
        styleTheChart(ctx, pieChart)
        pieChart.data = chartData
    }

    private fun createChartEntryList(correct: Int, wrong: Int, white: Int): List<PieEntry> {
        val chartEntryList = ArrayList<PieEntry>()
        chartEntryList.add(PieEntry(correct.toFloat(), getString(R.string.exam_result_chart_label_correct)))
        chartEntryList.add(PieEntry(wrong.toFloat(), getString(R.string.exam_result_chart_label_wrong)))
        chartEntryList.add(PieEntry(white.toFloat(), getString(R.string.exam_result_chart_label_white)))
        return chartEntryList
    }

    private fun createChartDataSet(ctx: Context, entryList: List<PieEntry>): PieDataSet {
        val set = PieDataSet(entryList, "")
        val iranSansFont = ResourcesCompat.getFont(ctx, R.font.iran_sans)
        set.valueTypeface = iranSansFont
        set.setColors(
                ContextCompat.getColor(ctx, R.color.azeraColorGreen),
                ContextCompat.getColor(ctx, R.color.azeraColorRed),
                ContextCompat.getColor(ctx, R.color.azeraColorYellow)
        )
        return set
    }

    private fun styleTheChart(ctx: Context, pieChart: PieChart) {
        pieChart.setDrawEntryLabels(false)
        pieChart.setCenterTextTypeface(ResourcesCompat.getFont(context!!, R.font.iran_sans))
        pieChart.setCenterTextSize(16f)
        val description = Description()
        description.text = "نتیجه آزمون شما"
        description.typeface = ResourcesCompat.getFont(context!!, R.font.iran_sans)
        description.textSize = 14f
        description.yOffset = 12f
        pieChart.description = description
        val iranSansFont = ResourcesCompat.getFont(ctx, R.font.iran_sans)
        val chartLegend = pieChart.legend// دریافت آبجکت شرح نمودار
        chartLegend.typeface = iranSansFont// تغییر فونت متن شرح نمودار
        chartLegend.form = Legend.LegendForm.CIRCLE
        chartLegend.orientation = Legend.LegendOrientation.VERTICAL
        chartLegend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        chartLegend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        chartLegend.direction = Legend.LegendDirection.RIGHT_TO_LEFT
    }
}
