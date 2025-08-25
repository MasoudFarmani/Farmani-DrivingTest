package farmani.masoud.app.traffic_regu_iab.ui.fragment.preference

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.getThemeColor

class AzeraSettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val roadSignSetting = Preference(context)
        roadSignSetting.title = "تنظیمات آموزش تابلو"
        roadSignSetting.key = "go_road_sign_setting"
        roadSignSetting.fragment = LearnRoadSignSettingFragment::class.java.name
        roadSignSetting.setIcon(R.drawable.azera_ic_road_sign_setting)
        val roadSignSettingIcon = DrawableCompat.wrap(roadSignSetting.icon)

        val color = context.getThemeColor(R.attr.colorPrimary)

        val examSetting = Preference(context)
        examSetting.title = "تنظیمات آزمون"
        examSetting.key = "got_exam_setting"
        examSetting.fragment = ExamSettingFragment::class.java.name
        examSetting.setIcon(R.drawable.azera_ic_exam_setting)
        val examSettingIcon = DrawableCompat.wrap(examSetting.icon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(roadSignSettingIcon, color)
            DrawableCompat.setTint(examSettingIcon, color)
        } else {
            roadSignSettingIcon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
            examSettingIcon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }

        screen.addPreference(roadSignSetting)
        screen.addPreference(examSetting)

        preferenceScreen = screen

    }

}