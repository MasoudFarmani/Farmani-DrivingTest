package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.extensions.setViewPager
import farmani.masoud.app.traffic_regu_iab.extensions.setup
import farmani.masoud.app.traffic_regu_iab.ui.fragment.ExamMainFragment
import farmani.masoud.app.traffic_regu_iab.ui.fragment.LearnMainMenuFragment
import farmani.masoud.app.traffic_regu_iab.ui.fragment.ShortExamMainMenuFragment
import farmani.masoud.app.traffic_regu_iab.util.AppRate
import farmani.masoud.app.traffic_regu_iab.util.HelperFunctions
import farmani.masoud.app.traffic_regu_iab.util.LogCat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : BaseActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private var starterIntent : Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        starterIntent = intent
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setup()
        toolbar.title = getString(R.string.azr_app_name)
        val activityPages = listOf(
                ExamMainFragment(),
                ShortExamMainMenuFragment(),
                LearnMainMenuFragment()
        )
        setupViewPager(activityPages)
        setupTabLayout(activityPages)
        tabLayout.setViewPager(viewpager)
        toggle = ActionBarDrawerToggle(this, drawer_layout, 0, 0)
        drawer_layout.addDrawerListener(toggle)
        navigationView.setNavigationItemSelectedListener { item ->
            navigationItemSelected(item.itemId)
            true
        }
        navigationView.getHeaderView(0).findViewById<ImageView>(R.id.navHeaderIvAppIcon)
                .background = applicationInfo.loadIcon(packageManager)
        AppRate(this).show {
            threshold(3)
            session(5)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            PurchaseActivity.RESULT_CODE_SUCCESS -> {
                recreate()
                LogCat.log(msg = "راه اندازی مجدد اکتیویتی")
            }
        }
    }

    private fun navigationItemSelected(itemId: Int) {
        when (itemId) {
            R.id.navItemSuggestToFriends -> HelperFunctions.shareWithFriends(this)
            R.id.navItemPurchaseGuide -> HelperFunctions.purchaseGuide(this)
            R.id.navItemRate -> AppRate(this).show { threshold(3) }
            R.id.navItemSupport -> HelperFunctions.telegramSupport(this, getString(R.string.telegram_support_id))
            R.id.navItemAboutMe -> startActivity(Intent(this, AboutMeActivity::class.java))
        }
    }


    private fun setupTabLayout(pageList: List<Fragment>) {
        tabLayout.selectTab(tabLayout.getTabAt(pageList.lastIndex))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.azr_main_activity_options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.END)
                true
            }
            R.id.action_setting -> {
                startActivity(Intent(this, SettingActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.END))
            drawer_layout.closeDrawer(GravityCompat.END)
        else super.onBackPressed()
    }

    private fun setupViewPager(pages: List<Fragment>) {
        viewpager.adapter = ViewPagerAdapter(supportFragmentManager, pages)
        viewpager.currentItem = pages.size - 1
    }

    private class ViewPagerAdapter(fm: FragmentManager,
                                   private val fragmentList: List<Fragment>) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {


        override fun getItem(index: Int): Fragment {
            return fragmentList[index]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }
    }
}