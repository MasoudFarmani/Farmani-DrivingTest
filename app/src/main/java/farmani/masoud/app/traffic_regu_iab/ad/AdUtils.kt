package farmani.masoud.app.traffic_regu_iab.ad

import android.content.Context
import android.widget.Toast
import ir.tapsell.sdk.*

fun requestRewardAd(context: Context, onAvailableCallback: (ad: TapsellAd?) -> Unit) {
    val adRequestOptions = TapsellAdRequestOptions()
    adRequestOptions.cacheType = TapsellAdRequestOptions.CACHE_TYPE_STREAMED
    val adRequestListener = object : TapsellAdRequestListener {
        override fun onAdAvailable(tapsellAd: TapsellAd?) {
            onAvailableCallback.invoke(tapsellAd)
        }

        override fun onExpiring(p0: TapsellAd?) {
        }

        override fun onNoAdAvailable() {
            Toast.makeText(context, "تبلیغی موجود نیست، چند ثانیه دیگه امتحان کنید!", Toast.LENGTH_SHORT).show()
        }

        override fun onError(p0: String?) {
            Toast.makeText(context, "خطایی رخ داده، بعدا امتحان کنید", Toast.LENGTH_SHORT).show()
        }

        override fun onNoNetwork() {
            Toast.makeText(context, "اینترنت متصل نیست!!", Toast.LENGTH_SHORT).show()
        }

    }

    Tapsell.requestAd(context, null, adRequestOptions, adRequestListener)
}

fun setAdRewardListener(block: () -> Unit) {
    Tapsell.setRewardListener { tapsellAd, completed ->
        if (completed && tapsellAd.isRewardedAd) {
            block.invoke()
        }
    }
}

fun showRewardAd(context: Context, ad: TapsellAd?) {
    val adShowOptions = TapsellShowOptions()
    adShowOptions.isBackDisabled = false
    adShowOptions.isImmersiveMode = true
    adShowOptions.isShowDialog = true
    ad?.show(context, adShowOptions)
            ?: Toast.makeText(context, "تبلیغی موجود نیست، چند ثانیه دیگه امتحان کنید!", Toast.LENGTH_SHORT).show()

}