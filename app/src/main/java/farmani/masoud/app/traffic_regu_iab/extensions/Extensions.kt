package farmani.masoud.app.traffic_regu_iab.extensions

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBar
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

/*extension fun to get theme color attribute*/
@ColorInt
fun Context.getThemeColor(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun View.hide(){
    this.visibility = View.INVISIBLE
}
fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible(){
    this.visibility = View.VISIBLE
}

fun ActionBar.setup() {
    setDisplayHomeAsUpEnabled(true)
    setHomeButtonEnabled(true)
    setDisplayShowTitleEnabled(false)
}

fun RadioGroup.getAllRadioButtons(): List<RadioButton> {
    val radioButtonList = mutableListOf<RadioButton>()
    for (index in 0..childCount) {
        val child = getChildAt(index)
        if (child is RadioButton) radioButtonList.add(child)
    }
    return radioButtonList
}

fun Activity.getRSA(): String {
    val rsa = StringBuilder()
    for (i in 1..7) {
        val ii = when (i) {
            1 -> 5
            2 -> 3
            3 -> 2
            4 -> 6
            5 -> 1
            6 -> 7
            else -> 4
        }
        val resName = "RSA$ii"
        val rrsa = getString(resources.getIdentifier(resName, "string", packageName))
        if (i == 3) rrsa.replace('y', 'Y').replace('d', 'm')
        if (ii == 7) rrsa.replace('q', 'Q').replace('r', 'R')
        when (i) {
            1 -> rsa.append(rrsa.replace('m', 'M').replace('8', '0'))
            6 -> rsa.append(rrsa.replace('B', 'i').replace('z', 'X'))
            else -> rsa.append(rrsa)
        }

    }
    return rsa.toString()
}

fun TabLayout.addTabListener(callBack: (tab: TabLayout.Tab?) -> Unit) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            callBack.invoke(tab)
        }

    })
}

fun ViewPager.addPageListener(callBack: (Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            callBack.invoke(position)
        }

    })
}

fun TabLayout.setViewPager(viewpager: ViewPager) {
    addTabListener { tab -> tab?.run { viewpager.currentItem = position } }
    viewpager.addPageListener { position -> selectTab(getTabAt(position)) }
}

