package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.setup
import farmani.masoud.app.traffic_regu_iab.ui.fragment.preference.AzeraSettingFragment
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setSupportActionBar(settingToolbar)
        supportActionBar?.setup()
        supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_NONE)
                .replace(R.id.settingFragmentContainer, AzeraSettingFragment())
                .commit()
        settingToolbar.title = "تنظیمات"
        settingTvAppVersion.text = "نسخه ${getVersionName()}"
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getVersionName(): String {
        return packageManager.getPackageInfo(packageName, 0).versionName
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?, pref: Preference?): Boolean {

        if (caller != null && pref != null) {
            val args = pref.extras
            val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment)
            fragment.arguments = args
            fragment.setTargetFragment(caller, 0)


            supportFragmentManager.beginTransaction()
                    .replace(R.id.settingFragmentContainer, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit()

            settingToolbar.title = pref.title
            return true
        } else return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        settingToolbar.title = "تنظیمات"
    }
}