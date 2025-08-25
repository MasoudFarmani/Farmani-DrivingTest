package farmani.masoud.app.traffic_regu_iab.application

import android.content.res.Resources
import android.os.Handler
import androidx.multidex.MultiDexApplication
import ir.tapsell.sdk.Tapsell
import java.util.*

/**
 * Created by Masoud Farmani on 7/31/2018.
 */

private val tapSellAppKey = "kmcmahposeligicqpqgfggellhommfdlhicopehqkfarsqmkbckgimbadnnkhmcogfjpjl"


class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        appResources = resources
        azeraPackageName = packageName
        Tapsell.initialize(this, tapSellAppKey)
    }

    companion object {
        var adReward = false
        const val DEBUG_MODE = true
        var rated = false
        lateinit var instance: App
        lateinit var azeraPackageName: String
            private set
        lateinit var appResources: Resources
            private set
        val uiHandler = Handler()
        val iranLocale = Locale("fa")
    }
}
