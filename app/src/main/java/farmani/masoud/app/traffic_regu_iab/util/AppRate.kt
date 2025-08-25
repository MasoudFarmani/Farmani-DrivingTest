package farmani.masoud.app.traffic_regu_iab.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.analytics.FirebaseAnalytics
import com.willy.ratingbar.RotationRatingBar
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.gone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val SESSION_KEY = "rate_session"
const val PASSED_SESSION_KEY = "passed_session"
const val RATED_KEY = "rated"
const val NEVER_SHOW_KEY = "never_show"
private const val INTENT_SUBJECT = "نظر و پیشنهاد درباره برنامه"

class AppRate(private val activity: AppCompatActivity) {

    private var threshold = -1
    private var session: Int = -1
    private var passedSession = 0
    private val preference = PreferenceManager.getDefaultSharedPreferences(activity)

    init {
    }

    fun threshold(value: Int): AppRate {
        this.threshold = value
        return this
    }

    private fun passedSessionInc() {
        passedSession = preference.getInt(PASSED_SESSION_KEY, 0)
        preference.edit { putInt(PASSED_SESSION_KEY, ++passedSession) }
    }

    fun session(value: Int): AppRate {
        if (!preference.contains(SESSION_KEY)) {
            session = value
            preference.edit { putInt(SESSION_KEY, value) }
        } else {
            session = preference.getInt(SESSION_KEY, -1)
        }
        passedSessionInc()
        return this
    }

    private fun showRateDialog() {
        val rateDialog = createRateDialog()
        setupRateDialogListeners(rateDialog)
        rateDialog.show()
    }

    private fun showFeedbackDialog() {
        val feedbackDialog = createFeedbackDialog()
        setupFeedbackDialogListeners(feedbackDialog)
        feedbackDialog.show()
    }

    private fun show() {
        if (session == -1) {
            if (ratedBefore()) {
                showFeedbackDialog()
            } else {
                showRateDialog()
            }
        } else {
            if (sessionPassed() && !ratedBefore() && !neverShow()) {
                showRateDialog()
            }
        }
    }

    internal fun show(func: AppRate.() -> Unit): AppRate {
        this.func()
        this.show()
        return this
    }

    private fun sessionPassed() = passedSession >= session
    private fun ratedBefore() = preference.getBoolean(RATED_KEY, false)
    private fun neverShow() = preference.getBoolean(NEVER_SHOW_KEY, false)

    private fun reportRateEvent(rate: Int) {
        val params = bundleOf(
                "rate" to "$rate"
        )
        FirebaseAnalytics.getInstance(activity).logEvent("rate", params)
    }

    private fun setupRateDialogListeners(rateDialog: MaterialDialog) {
        val rateDialogLayout = rateDialog.getCustomView()
        val rateDialogIvAppIcon = rateDialogLayout.findViewById<ImageView>(R.id.rateDialogIvAppIcon)
        val appIcon = activity.applicationInfo.loadIcon(activity.packageManager)
        rateDialogIvAppIcon.setImageDrawable(appIcon)
        val ratingBar = rateDialogLayout.findViewById<RotationRatingBar>(R.id.rateDialogRatingBar)
        ratingBar.setOnRatingChangeListener { _, rating, _ ->
            if (rating < threshold) GlobalScope.launch(Dispatchers.Main) {
                delay(1000)
                rateDialog.dismiss()
                showFeedbackDialog()
            } else GlobalScope.launch(Dispatchers.Main) {
                delay(1000)
                rateDialog.dismiss()
                sendToCafeBazaar()
            }
            saveRated()
            reportRateEvent(rating.toInt())
        }
        val btnLater = rateDialogLayout.findViewById<Button>(R.id.rateDialogBtnLater)
        btnLater.setOnClickListener {
            resetSession()
            rateDialog.dismiss()
        }
        val btnNever = rateDialogLayout.findViewById<Button>(R.id.rateDialogBtnNever)
        if (session != -1) {
            btnNever.setOnClickListener {
                rateDialog.dismiss()
                saveNeverShow()
            }
        } else {
            btnNever.gone()
        }

    }

    private fun resetSession() {
        preference.edit { putInt(PASSED_SESSION_KEY, 0) }
    }

    private fun setupFeedbackDialogListeners(feedbackDialog: MaterialDialog) {
        val feedbackDialogLayout = feedbackDialog.getCustomView()
        val textInput = feedbackDialogLayout.findViewById<TextInputEditText>(R.id.feedbackDialogInputEdt)
        val btnEmail = feedbackDialogLayout.findViewById<Button>(R.id.feedbackDialogBtnEmail)
        btnEmail.setOnClickListener thisPoint@{
            textInput.text?.let { input ->
                if (TextUtils.isEmpty(input.toString())) {
                    textInput.error = "پیام نمیتونه خالی باشه!"
                    return@thisPoint
                }
                sendEmail(input.toString())
            }
        }
        val btnTelegram = feedbackDialogLayout.findViewById<Button>(R.id.feedbackDialogBtnTelegram)
        btnTelegram.setOnClickListener thisPoint@{
            textInput.text?.let { input ->
                if (TextUtils.isEmpty(input.toString())) {
                    textInput.error = "پیام نمیتونه خالی باشه!"
                    return@thisPoint
                }
                sendTelegram(input.toString())
            }
        }
    }

    private fun sendEmail(message: String) {
        activity.startActivity(
                Intent.createChooser(getEmailIntent(message), "ارسال ایمیل...")
        )
    }

    private fun getEmailIntent(message: String): Intent {
        return Intent(Intent.ACTION_SENDTO).setData(Uri.parse(
                "mailto:${Uri.encode(activity.getString(R.string.email_address))}" +
                        "?subject=${Uri.encode(INTENT_SUBJECT)}" +
                        "&body=${Uri.encode(message)}"
        ))
    }

    private fun sendTelegram(message: String) {
        try {
            activity.startActivity(Intent.createChooser(getTelegramIntent(message), "ارسال پیام..."))
        } catch (notFound: ActivityNotFoundException) {
            HelperFunctions.promptUser("هیچ برنامه ای برای انجام این عملیات پیدا نشد!", activity.window.decorView)
        }
    }

    private fun getTelegramIntent(message: String): Intent {
        return Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .setPackage("org.telegram.messenger")
                .setData(Uri.parse("https://telegram.me/${activity.getString(R.string.telegram_support_id)}"))
                .putExtra(Intent.EXTRA_TEXT, INTENT_SUBJECT)
                .putExtra(Intent.EXTRA_TEXT, message)

    }

    private fun sendToCafeBazaar() {
        HelperFunctions.rateApp(activity)
    }

    private fun saveRated() {
        preference.edit { putBoolean(RATED_KEY, true) }
    }

    private fun saveNeverShow() {
        preference.edit { putBoolean(NEVER_SHOW_KEY, true) }
    }

    private fun createRateDialog(): MaterialDialog {
        return MaterialDialog(activity)
                .customView(R.layout.dialog_app_rate, scrollable = true, noVerticalPadding = true)
                .lifecycleOwner(activity)
    }

    private fun createFeedbackDialog(): MaterialDialog {
        return MaterialDialog(activity)
                .customView(R.layout.dialog_send_feedback, scrollable = true, noVerticalPadding = true)
                .lifecycleOwner(activity)
    }

}