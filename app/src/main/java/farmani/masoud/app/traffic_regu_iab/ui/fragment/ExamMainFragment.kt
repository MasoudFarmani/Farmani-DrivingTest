package farmani.masoud.app.traffic_regu_iab.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.ad.requestRewardAd
import farmani.masoud.app.traffic_regu_iab.ad.setAdRewardListener
import farmani.masoud.app.traffic_regu_iab.ad.showRewardAd
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor
import farmani.masoud.app.traffic_regu_iab.extensions.hide
import farmani.masoud.app.traffic_regu_iab.extensions.visible
import farmani.masoud.app.traffic_regu_iab.ui.activity.ExamActivity
import farmani.masoud.app.traffic_regu_iab.ui.activity.ExamReviewActivity
import farmani.masoud.app.traffic_regu_iab.ui.activity.PurchaseActivity
import farmani.masoud.app.traffic_regu_iab.viewmodel.MainActivityViewModel
import ir.tapsell.sdk.TapsellAd
import kotlinx.android.synthetic.main.fragment_exam_main_layout.*
import org.angmarch.views.NiceSpinner

var availableAd: TapsellAd? = null

class ExamMainFragment : Fragment() {

    private lateinit var viewModel: MainActivityViewModel
    private var examListOfSelectedCategory: List<Exam>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_exam_main_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        val examCatPicker: NiceSpinner = view.findViewById(R.id.examCatPicker)
        examCatPicker.attachDataSource(Exam.Category.names)
        examCatPicker.setOnSpinnerItemSelectedListener { _, _, position, _ ->
            viewModel.selectAllExamsByCategory(Exam.Category.values()[position])
        }
        val rvAdapter = prepareRecyclerView()
        viewModel.selectedExamListMutableLive.observe(activity!!, Observer { queryResult ->
            rvAdapter.examList = queryResult
        })
        //برای اینکه لیست آزمون ها از همون اول مقدار داشته باشه
        viewModel.selectAllExamsByCategory(Exam.Category.values()[examCatPicker.selectedIndex])
        /*لانچ اکتیویتی مرور سوالات بوکمارک شده یِ هر دسته بندی آزمونی*/
        btnStartExamReview.setOnClickListener {
            val intent = ExamReviewActivity.getIntent(activity!!, rvAdapter.examList!!)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        prepareShowCaseView(activity!!)

    }
    private fun prepareShowCaseView(context: Activity) {
        val examBubble = BubbleShowCaseBuilder(context)
                .targetView(showCaseAnchor)
                .title("آزمون ها")
                .description("آزمون هایی که قفل دارند غیررایگان هستند، با لمس روشون میتونید انتخاب کنید که نسخه کامل رو بخرید یا با دیدن تبلیغات، رایگان آزمون بدید")
                .backgroundColor(context.getThemeColor(R.attr.colorPrimary))
                .showOnce("notFreeExamShowCase")

        val examCategoryBubble = BubbleShowCaseBuilder(context)
                .targetView(examCatPicker)
                .title("دسته بندی آزمون")
                .description("از این قسمت میتونید دسته بندی آزمون ( اصلی - فنی - مقدماتی ) رو انتخاب کنید")
                .backgroundColor(context.getThemeColor(R.attr.colorPrimary))
                .showOnce("examCategoryShowCase")

        val examReviewBubble = BubbleShowCaseBuilder(context)
                .targetView(btnStartExamReview)
                .title("مرور سوالات نشاندار")
                .description("سوالاتی که در آزمون نشاندار میکنید یا غلط جواب میدید رو میتونید بعدا با لمس این قسمت مرور کنید. با لمس اون، سوالات مربوط به دسته بندی انتخاب شده که نشاندار هستند برای مرور آماده میشن")
                .backgroundColor(context.getThemeColor(R.attr.colorPrimary))
                .showOnce("examReviewShowCase")
        BubbleShowCaseSequence()
                .addShowCase(examBubble)
                .addShowCase(examCategoryBubble)
                .addShowCase(examReviewBubble)
                .show()
    }
    private fun prepareRecyclerView(): ExamMainRecyclerViewAdapter {
        val rvAdapter = ExamMainRecyclerViewAdapter(activity!!, this)
        rvExamList.adapter = rvAdapter
        rvExamList.layoutManager = GridLayoutManager(activity!!, 2)
        return rvAdapter
    }

    private class ExamMainRecyclerViewAdapter internal constructor(ctx: Activity, fragment: ExamMainFragment)
        : RecyclerView.Adapter<ExamMainRecyclerViewAdapter.VH>() {

        private val context: Activity = ctx
        private val examMainFragmentInstance: ExamMainFragment = fragment
        var examList: List<Exam>? = null
            set(value) {
                field = value
                if (field != null) notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(context).inflate(R.layout.item_rv_main_exam, parent, false))
        }


        override fun getItemCount(): Int {
            return this.examList?.size ?: 0
        }


        override fun onBindViewHolder(holder: VH, position: Int) {
            val currentExam = examList?.get(position)
            currentExam?.let { curExam ->
                val numberFormatter = "${curExam.category} %d"
                holder.txtExamName.text = String.format(App.iranLocale, numberFormatter, position + 1)
                holder.cvStartExam.setOnClickListener {
                    if (shouldOpenExam(currentExam)) {
                        context.run {
                            startActivity(ExamActivity.getIntent(
                                    this,
                                    curExam,
                                    false
                            ))
                        }
                    } else {// آزمونی که کلیک شده شرایط نمایش را ندارد(نسخه کامل نیست، آزمون رایگان نیست)
                        MaterialDialog(context).show {
                            title(text = "آزمون غیررایگان!")
                            icon(R.drawable.bazar_logo)
                            customView(R.layout.dialog_ad_prompt)
                            val btnStartAd = getCustomView().findViewById<Button>(R.id.btnDialogStartAd)
                            btnStartAd.setOnClickListener {
                                dismiss()
                                /*شروع نمایش تبلیغ درخواست شده*/
                                showRewardAd(context, availableAd)
                            }
                            val btnStartPurchase = getCustomView().findViewById<Button>(R.id.btnDialogPurchase)
                            /*لانچ اکتیویتی پرداخت با رخداد کلیک*/
                            btnStartPurchase.setOnClickListener {
                                dismiss()
                                (windowContext as Activity).run {
                                    startActivityForResult(Intent(this, PurchaseActivity::class.java), 5)
                                }
                            }
                        }
                        /*شروع درخواست تبلیغ*/
                        requestRewardAd(context) {
                            availableAd = it
                        }
                        /*بلوکی که باید بعد از دیدن ویدیو به عنوان جایزه اجرا شود*/
                        setAdRewardListener {
                            context.run {
                                startActivity(ExamActivity.getIntent(
                                        this,
                                        curExam,
                                        rewardMode = true
                                ))
                            }
                        }
                    }
                }
                if (shouldOpenExam(currentExam)) {
                    holder.imgLockIcon.hide()
                    holder.txtExamState.visible()
                    currentExam.lastParticipateStateLive.observe(context as LifecycleOwner, Observer { state ->
                        holder.txtExamState.text = state.toString()
                        holder.txtExamState.setBackgroundColor(when (state) {
                            Exam.State.FAILED -> ContextCompat.getColor(context, R.color.azeraColorRed60)
                            Exam.State.PASSED -> ContextCompat.getColor(context, R.color.azeraColorGreen60)
                            else -> ContextCompat.getColor(context, R.color.azeraColorGray60)

                        })
                    })
                } else {
                    holder.imgLockIcon.visible()
                    holder.txtExamState.hide()
                }
            }
        }

        private fun shouldOpenExam(exam: Exam) =
                exam.isFree || exam.isPurchased || App.adReward

        internal class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val cvStartExam = itemView.findViewById<CardView>(R.id.cvStartExam)
            val txtExamName = itemView.findViewById<TextView>(R.id.txtExamName)
            val txtExamState = itemView.findViewById<TextView>(R.id.txtExamState)
            val imgLockIcon = itemView.findViewById<ImageView>(R.id.imgLockIcon)
        }
    }

}