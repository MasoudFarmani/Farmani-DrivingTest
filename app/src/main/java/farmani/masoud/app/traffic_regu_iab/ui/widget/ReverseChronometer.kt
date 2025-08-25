package farmani.masoud.app.traffic_regu_iab.ui.widget


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor
import java.util.*

/**
 * Created by MasoudFarmani on {* 7/9/2017  10:20 PM *}
 */

class ReverseChronometer : MaterialButton, Runnable {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val timerFormat = resources.getString(R.string.format_chronometer)
    private val iranLocale: Locale = Locale("fa")
    private var onStateChangeListeners = ArrayList<OnStateChangeListener>()
    private var currentState: State
    private var startTime = SystemClock.elapsedRealtime()
    private var overallDuration = 0L
    private var warningDuration = 0L
    private var remainingSeconds = 0L
    private val warningColor = R.color.azeraColorAccent

    init {
        typeface = ResourcesCompat.getFont(context, R.font.iran_sans_numeral_monospaced_medium)
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        changeIcon(R.drawable.azera_ic_timer_resume)
        //کلیک لیستنر داخلی که هربار متد toggleState رو فراخوانی میکنه
        this.setOnClickListener { toggleState() }
        //وضعیت جاری = ساخته شده
        this.currentState = State.CREATED
    }

    fun setOverallDuration(overallDuration: Long): ReverseChronometer {
        this.overallDuration = overallDuration
        return this
    }

    fun setWarningDuration(warningDuration: Long): ReverseChronometer {
        this.warningDuration = warningDuration
        return this
    }

    private fun changeIcon(@DrawableRes drawable: Int) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0)
    }

    fun addOnStateChangeListener(onStateChangeListener: OnStateChangeListener) {
        this.onStateChangeListeners.add(onStateChangeListener)
    }

    override fun run() {

        val elapsedSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000

        if (elapsedSeconds <= overallDuration) {
            this.currentState = State.RUNNING// این وضعیت ، فقط لازمه که در متد resume به لیستنرها اعلام بشه
            remainingSeconds = overallDuration - elapsedSeconds
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            updateTime(minutes, seconds)
            if (remainingSeconds == 0L) {
                setTextColor(ContextCompat.getColor(context, warningColor))
            } else if (warningDuration > 0 && remainingSeconds < warningDuration) {
                setTextColor(ContextCompat.getColor(context, warningColor))
            } else {
                setTextColor(context.getThemeColor(R.attr.colorPrimary))
            }

            postDelayed(this, 1000)
        } else {// پابان زمان تعیین شده
            overallDuration = 0L
            stop()
            this.currentState = State.FINISHED
            exposeCurrentState()
        }
    }

    /*
     * متن تکست ویو رو بر اساس دقیقه و ثانیه باقی مانده بروز میکنه
     * */
    private fun updateTime(minutes: Long, seconds: Long) {
        text = String.format(iranLocale, timerFormat, minutes, seconds)
    }

    fun pause() {
        changeIcon(R.drawable.azera_ic_timer_pause)
        overallDuration = remainingSeconds
        removeCallbacks(this)
        this.currentState = State.PAUSED
        exposeCurrentState()
    }

    fun isPaused() = this.currentState == State.PAUSED

    /*
     * به دلیل اینکه در این متد ما در واقع عمل run رو با مقدار جدید startTime صدا میزنیم
     * وضعیت جاری متاثر از صدا زدن این متد رو در همون متد run مقدار میدهیم*/
    fun resume() {
        changeIcon(R.drawable.azera_ic_timer_resume)
        startTime = SystemClock.elapsedRealtime()
        run()
        exposeCurrentState()
    }

    private fun stop() {
        changeIcon(R.drawable.azera_ic_timer_stop)
        removeCallbacks(this)
        this.currentState = State.STOPPED
        exposeCurrentState()
    }

    /*
    با هربار صدازدن این متد ، تمامی لیستنترها از وضعیت جاری کرنومتر با
     * خبر میشوند*/
    private fun exposeCurrentState() {
        for (onStateChangeListener in this.onStateChangeListeners) {
            onStateChangeListener.onStateChanged(this.currentState)
        }
    }

    /*
     * این متد در صورتیکه تایمر در حالت Resume باشه
     * اون رو به حالت Pause میبره و بالعکس
     * */
    private fun toggleState() {
        if (this.currentState == State.RUNNING) {
            pause()
        } else if (this.currentState == State.PAUSED) {
            resume()
        }
    }


    fun disable() {
        isEnabled = false
        stop()
        this.currentState = State.DISABLED
        exposeCurrentState()
    }

    fun disabled() = currentState == State.DISABLED

    fun interrupt() {
        stop()
        this.currentState = State.INTERRUPTED
        exposeCurrentState()
    }

    enum class State {
        CREATED, RUNNING, PAUSED, STOPPED, DISABLED, FINISHED, INTERRUPTED
    }

    interface OnStateChangeListener {
        fun onStateChanged(state: State)
    }
}
