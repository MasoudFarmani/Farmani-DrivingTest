package farmani.masoud.app.traffic_regu_iab.util

import android.util.Log
import farmani.masoud.app.traffic_regu_iab.application.App

class LogCat {

    companion object {
        fun log(tag: String = "LOG", msg: String, level: Char = 'd') {
            log(tag, msg, level, null)
        }

        fun log(tag: String = "LOG", msg: String, level: Char = 'd', exception: Exception?) {
            if (App.DEBUG_MODE) when (level) {
                'd' -> Log.d(tag, msg, exception)
                'e' -> Log.e(tag, msg, exception)
            }
        }
    }
}