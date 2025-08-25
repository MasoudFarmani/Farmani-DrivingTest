package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.os.Bundle
import android.view.MenuItem
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.setup
import kotlinx.android.synthetic.main.activtiy_about_me.*

class AboutMeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activtiy_about_me)
        setSupportActionBar(aboutMeToolbar)
        supportActionBar?.setup()
        aboutMeToolbar.title = " درباره من"
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }
}