package farmani.masoud.app.traffic_regu_iab.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.snackbar.Snackbar
import farmani.masoud.app.traffic_regu_iab.R

object HelperFunctions {

    fun promptUser(
            promptMsg: String,
            view: View,
            showLength: Int = Snackbar.LENGTH_SHORT,
            actionText: String? = null,
            clickListener: View.OnClickListener? = null
    ) {
        val snackBar = Snackbar.make(view, promptMsg, showLength)
        /*نمایش متن اسنک بار به صورت راست به چپ*/
        ViewCompat.setLayoutDirection(snackBar.view, ViewCompat.LAYOUT_DIRECTION_RTL)
        snackBar.run {
            setAction(actionText, clickListener)
            show()
        }
    }

    fun telegramSupport(context: Activity, telegramId: String) {
        val telegram = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://telegram.me/$telegramId"))
        context.startActivity(telegram)
    }

    fun shareWithFriends(context: Activity) {

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.cafe_bazaar_app_link_title)
                        + context.getString(R.string.cafe_bazaar_app_link_url)
        )
        context.startActivity(Intent.createChooser(intent, "ارسال لینک دانلود اپلیکیشن"))
    }

    fun purchaseGuide(context: Activity) {
        try {
            val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://help.cafebazaar.ir/purchase/210242"
                    ))
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            HelperFunctions.promptUser(
                    "برنامه ای برای بازکردن این لینک پیدا نشد!",
                    context.window.decorView
            )
        }

    }

    fun rateApp(context: AppCompatActivity) {
        try {
            val intent = Intent(Intent.ACTION_EDIT)
            intent.data = Uri.parse("bazaar://details?id=" + context.packageName)
            intent.setPackage("com.farsitel.bazaar")
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            MaterialDialog(context).show {
                lifecycleOwner(context)
                title(text = "اپلیکیشن بازار روی دستگاه شما نصب نیست!")
                message(text = "مایلید کافه بازار رو نصب کنید؟")
                positiveButton(text = "دانلود") {
                    context.startActivity(
                            Intent(Intent.ACTION_VIEW)
                                    .setData(Uri.parse("https://cafebazaar.ir/download/bazaar.apk"))
                    )
                }
                negativeButton(text = "انصراف") { dismiss() }
                icon(R.drawable.azera_ic_warning)
            }
        } catch (e1: Exception) {
            Toast.makeText(context, "سیستم با مشکلی مواجه شده است", Toast.LENGTH_SHORT).show()
        }
    }
}
