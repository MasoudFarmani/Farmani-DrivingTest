package farmani.masoud.app.traffic_regu_iab.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import farmani.masoud.app.traffic_regu_iab.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by MasoudFarmani on {* 7/7/2017  2:26 AM *}
 */

object ScreenShot {

    /**
     * Take a snapshot of the view.
     */
    private fun snap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun shareView(context: Context, view: View) {
        val bitmap = snap(view)// save bitmap to cache directory
        try {
            val cacheFileName = File(context.cacheDir, "images")

            cacheFileName.mkdirs() // don't forget to make the directory
            val fout = FileOutputStream("$cacheFileName/image.png") // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fout)
            fout.close()
            val imageToShare = File(cacheFileName, "image.png")
            val contentUri = FileProvider.getUriForFile(context, "farmani.masoud.app.traffic_regu_iab.file_provider", imageToShare)
            if (contentUri != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        context.getString(R.string.cafe_bazaar_app_link_title) + context.getString(R.string.cafe_bazaar_app_link_url)
                )
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                context.startActivity(Intent.createChooser(shareIntent, "یه اپلیکیشن انتخاب کنید :"))
            }
            val fireBaseAnalytics = FirebaseAnalytics.getInstance(context)
            val params = bundleOf(
                    FirebaseAnalytics.Param.ITEM_NAME to context::class.java.simpleName,
                    FirebaseAnalytics.Param.CONTENT_TYPE to "image"
            )
            fireBaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, params)
        } catch (e: NullPointerException) {
            Snackbar.make(view, "خطایی رخ داده ، بعدا امتحان کنید!", Snackbar.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Snackbar.make(view, "خطایی رخ داده ، بعدا امتحان کنید!", Snackbar.LENGTH_SHORT).show()
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(view, "هیچ برنامه ای برای ارسال پیدا نشد!", Snackbar.LENGTH_SHORT).show()
        }

    }
}
