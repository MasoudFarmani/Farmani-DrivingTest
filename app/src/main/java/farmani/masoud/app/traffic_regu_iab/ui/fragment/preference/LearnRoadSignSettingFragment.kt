package farmani.masoud.app.traffic_regu_iab.ui.fragment.preference

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SeekBarPreference
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor
import java.util.*

class LearnRoadSignSettingFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        val summaryTemplate = "زمان رفتن خودکار به تابلوی بعدی : %d ثانیه"
        val autoPageTimePreference = HiddenValueSeekBarPreference(context)

        /*سازوکار قراردادن آیکون و رنگی کردنش با colorPrimary*/
        autoPageTimePreference.setIcon(R.drawable.azera_ic_timer_black_24dp)
        val icon = DrawableCompat.wrap(autoPageTimePreference.icon)
        val color = context.getThemeColor(R.attr.colorPrimary)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(icon, color)
        } else {
            icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
        /*-------------------------------------------------------------*/
        autoPageTimePreference.key = "auto_page_time"
        autoPageTimePreference.title = "ورق زدن خودکار"
        autoPageTimePreference.min = 2//ثانیه
        autoPageTimePreference.max = 10//ثانیه
        autoPageTimePreference.setDefaultValue(3)
        val iranLocale = Locale("fa")
        autoPageTimePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    preference?.summary = String.format(iranLocale, summaryTemplate, newValue)
                    true
                }
        screen.addPreference(autoPageTimePreference)
        /*نمایش در ابتدای شروع فرگمنت. حتما باید بعد از اضافه شدن
        * به screen صدا زده شود در غیر اینصورت value برابر 0 خواهد بود*/
        autoPageTimePreference.summary = String.format(iranLocale, summaryTemplate, autoPageTimePreference.value)
        preferenceScreen = screen
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        rootView?.setBackgroundColor(Color.WHITE)
        return rootView
    }

}

class HiddenValueSeekBarPreference(context: Context) : SeekBarPreference(context) {

    override fun onBindViewHolder(view: PreferenceViewHolder?) {
        super.onBindViewHolder(view)
        val seekBarValueTextView = view?.findViewById(androidx.preference.R.id.seekbar_value)
        seekBarValueTextView?.visibility = GONE
    }
}