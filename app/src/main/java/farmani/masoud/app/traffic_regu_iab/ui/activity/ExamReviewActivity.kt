package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam
import farmani.masoud.app.traffic_regu_iab.database.entity.Question
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor
import farmani.masoud.app.traffic_regu_iab.extensions.hide
import farmani.masoud.app.traffic_regu_iab.extensions.setup
import farmani.masoud.app.traffic_regu_iab.extensions.visible
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository
import farmani.masoud.app.traffic_regu_iab.ui.util.ScreenShot
import farmani.masoud.app.traffic_regu_iab.ui.widget.RoundedBackgroundSpan
import farmani.masoud.app.traffic_regu_iab.viewmodel.ExamReviewActivityViewModel
import kotlinx.android.synthetic.main.activity_exam_review.*

class ExamReviewActivity : BaseActivity() {

    companion object {
        lateinit var examCategory: List<Exam>
        fun getIntent(starter: Context, examCat: List<Exam>): Intent {
            examCategory = examCat
            return Intent(starter, ExamReviewActivity::class.java)
        }
    }

    private lateinit var dataHolder: ExamReviewActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_review)
        setSupportActionBar(examReviewToolbar)
        supportActionBar?.setup()
        val rvAdapter = setupRecyclerView()
        dataHolder = ViewModelProviders.of(this).get(ExamReviewActivityViewModel::class.java)
        dataHolder.bookmarkedQuestionListLive.observe(this, Observer { bookmarkedQuestions ->
            val title = "مرور   " + examCategory[0].category.toString()
            examReviewToolbar.title = title
            rvAdapter.questionList = mutableListOf()
            rvAdapter.questionList!!.addAll(bookmarkedQuestions)
            if (bookmarkedQuestions.isEmpty()) {
                examReviewRv.hide()
                containerNotFound.visible()
            }
        })
    }

    private fun setupRecyclerView(): ExamReviewRecyclerviewAdapter {
        val adapter = ExamReviewRecyclerviewAdapter(this)
        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        examReviewRv.layoutManager = lm
        examReviewRv.adapter = adapter
        return adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private class ExamReviewRecyclerviewAdapter internal constructor(ctx: Context)
        : RecyclerView.Adapter<ExamReviewRecyclerviewAdapter.VH>() {

        private val context = ctx
        var questionList: MutableList<Question>? = null
            set(value) {
                field = value;notifyDataSetChanged()
            }

        private val mutableMultiColorText = SpannableStringBuilder()
        private val colorAccent: Int = ctx.getThemeColor(R.attr.colorAccent)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_rv_review_exam, parent, false)
            return VH(itemView)
        }

        override fun getItemCount(): Int {
            return questionList?.size ?: 0
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val currentQuestion = questionList?.get(position)
            currentQuestion?.let { question ->
                val questionNumber = String.format(App.iranLocale, "سوال %d", question.id)
                holder.txtQuestionDetail.text = questionNumber
                prepareMultiColorText(" سوال ", question.question)
                holder.txtQuestion.text = mutableMultiColorText
                holder.ivQuestionImage.setImageResource(question.imageId)
                prepareMultiColorText(" جواب ", question.answerCorrect)
                holder.txtAnswer.text = mutableMultiColorText
                if (question.userNote != null && question.userNote!!.trim() != "") {
                    prepareMultiColorText(" یادداشت شما ", question.userNote!!)
                    holder.txtNote.text = mutableMultiColorText
                } else {
                    holder.txtNote.text = ""
                }
            }
            holder.ibDelete.setOnClickListener {
                currentQuestion?.let {
                    currentQuestion.profile.isBookmarked = false
                    AppRepository.getInstance(App.instance).updateQuestion(currentQuestion)
                }
                questionList?.remove(currentQuestion)
                notifyDataSetChanged()
                Toast.makeText(context, "سوال از لیست مرور حذف شد", Toast.LENGTH_SHORT).show()
                questionList?.run {
                    if (isEmpty()) {
                        (context as ExamReviewActivity).containerNotFound.visible()
                    }
                }
            }
            holder.ibShare.setOnClickListener {
                ScreenShot.shareView(context,holder.itemView.findViewById(R.id.container))
            }
        }

        private fun prepareMultiColorText(prefix: String, append: String) {
            mutableMultiColorText.delete(0, mutableMultiColorText.length)
            mutableMultiColorText.append(prefix)
            mutableMultiColorText.setSpan(RoundedBackgroundSpan(context),
                    0,
                    prefix.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mutableMultiColorText.setSpan(StyleSpan(BOLD),
                    0,
                    prefix.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            mutableMultiColorText.append(" ")
            mutableMultiColorText.append(append)
        }

        internal class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtQuestionDetail: TextView = itemView.findViewById(R.id.examReviewTxtTitle)
            val txtQuestion: TextView = itemView.findViewById(R.id.examReviewTxtQuestion)
            val txtAnswer: TextView = itemView.findViewById(R.id.examReviewTxtAnswer)
            val txtNote: TextView = itemView.findViewById(R.id.examReviewTxtNote)
            val ivQuestionImage: ImageView = itemView.findViewById(R.id.examReviewIvQuestionImage)
            val ibDelete: ImageButton = itemView.findViewById(R.id.examReviewIbDelete)
            val ibShare: ImageButton = itemView.findViewById(R.id.examReviewIbShare)
        }

    }
}