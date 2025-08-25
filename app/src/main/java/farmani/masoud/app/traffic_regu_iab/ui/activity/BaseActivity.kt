package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import farmani.masoud.app.traffic_regu_iab.R

/*هدف از این کلاس مجتمع کردن کد تغییر تم در یک کلاس
* به جای تک تک اکتیویتی هاست*/
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Azera)
        super.onCreate(savedInstanceState)
    }
}