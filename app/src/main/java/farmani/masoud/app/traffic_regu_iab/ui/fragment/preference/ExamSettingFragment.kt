package farmani.masoud.app.traffic_regu_iab.ui.fragment.preference

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import java.util.*

const val examAutoPageEnableKey = "exam_auto_page"
const val examAutoPageTimeKey = "exam_auto_page_time"
const val examAutoPageTimeDefValue = 1
const val examAutoBookmarkEnableKey = "exam_auto_bookmark"

class ExamSettingFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        /*تنظیمات فعال/غیرفعال کردن ورق زدن خودکار سوال*/
        val autoPageCategory = PreferenceCategory(context)
        autoPageCategory.key = "exam_auto_page_category"
        autoPageCategory.title = "ورق زدن"
        screen.addPreference(autoPageCategory)

        val autoPageEnablePreference = SwitchPreference(context)
        autoPageEnablePreference.key = examAutoPageEnableKey
        autoPageEnablePreference.title = "ورق زدن خودکارِ سوال"
        autoPageEnablePreference.summary = "درصورت فعال بودن، با پاسخ دادن به سوال، به صورت خودکار سوال بعدی آماده می شود"
        autoPageEnablePreference.switchTextOn = "روشن"
        autoPageEnablePreference.switchTextOff = "خاموش"
        autoPageEnablePreference.setDefaultValue(true)
        autoPageCategory.addPreference(autoPageEnablePreference)

        /*تنظیمات زمانِ رفتن خودکار به سوال بعدی در صورت فعال بودن ورق زدن خودکار*/
        val autoPageTimePreference = HiddenValueSeekBarPreference(context)
        autoPageTimePreference.key = examAutoPageTimeKey
        autoPageTimePreference.title = "زمان رفتن خودکار به سوال بعدی"
        autoPageTimePreference.min = 1//ثانیه
        autoPageTimePreference.max = 3//ثانیه
        autoPageTimePreference.setDefaultValue(examAutoPageTimeDefValue)
        val iranLocale = Locale("fa")
        autoPageTimePreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    preference?.summary = String.format(iranLocale, "%d ثانیه", newValue)
                    true
                }
        autoPageCategory.addPreference(autoPageTimePreference)
        autoPageTimePreference.summary = String.format(iranLocale, "%d ثانیه", autoPageTimePreference.value)


        val autoBookmarkCategory = PreferenceCategory(context)
        autoBookmarkCategory.title = "نشانه گذاری"
        autoBookmarkCategory.key = "exam_auto_bookmark_category"
        screen.addPreference(autoBookmarkCategory)

        val autoBookmarkPreference = SwitchPreference(context)
        autoBookmarkPreference.setDefaultValue(true)
        autoBookmarkPreference.key = examAutoBookmarkEnableKey
        autoBookmarkPreference.title = "نشان کردن خودکار سوال"
        autoBookmarkPreference.summary = "درصورت فعال بودن، سوالاتی که غلط پاسخ می دهید، به صورت خودکار بوکمارک می شوند"
        autoBookmarkCategory.addPreference(autoBookmarkPreference)

        preferenceScreen = screen
        autoPageTimePreference.dependency = autoPageEnablePreference.key
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)
        rootView?.setBackgroundColor(Color.WHITE)
        return rootView
    }
}

