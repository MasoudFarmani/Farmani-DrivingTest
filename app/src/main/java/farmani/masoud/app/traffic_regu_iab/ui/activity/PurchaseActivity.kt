package farmani.masoud.app.traffic_regu_iab.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import farmani.masoud.app.traffic_regu_iab.R
import farmani.masoud.app.traffic_regu_iab.application.App
import farmani.masoud.app.traffic_regu_iab.database.entity.Exam
import farmani.masoud.app.traffic_regu_iab.extensions.gone
import farmani.masoud.app.traffic_regu_iab.extensions.setup
import farmani.masoud.app.traffic_regu_iab.extensions.visible
import farmani.masoud.app.traffic_regu_iab.iab.BillingProcessor
import farmani.masoud.app.traffic_regu_iab.iab.Constants
import farmani.masoud.app.traffic_regu_iab.iab.TransactionDetails
import farmani.masoud.app.traffic_regu_iab.repository.AppRepository
import farmani.masoud.app.traffic_regu_iab.util.LogCat
import kotlinx.android.synthetic.main.activity_purchase_layout.*

/**
 * overridden Activity's Lifecycle functions [onCreate] [onDestroy]
 *
 * @author Masoud Farmani on 9/7/2017  5:15 AM
 * Converted to Kotlin on 12/05/2019 8:11 AM
 */

class PurchaseActivity : BaseActivity(), BillingProcessor.IBillingHandler {

    private var billingProcessor: BillingProcessor? = null
    private val textViews = arrayOfNulls<MaterialTextView>(9)

    companion object {
        const val RESULT_CODE_SUCCESS = 20
        private const val SKU = "full_version"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_layout)
        setupTextViewList()
        addSupportActionBar()
        createBillingProcessor()
        btnPurchase.setOnClickListener { billingProcessor?.purchase(this, SKU) }
    }


    /**
     * Create new Instance of BillingProcessor and assign it to [billingProcessor]
     */
    private fun createBillingProcessor() {
        billingProcessor = BillingProcessor(
                this,
                generateCafeBazaarRsa(), null,
                this
        )
    }

    /**
     * Releases currentInstance of BillingProcessor if exists and assigns *null* to [billingProcessor]
     */
    private fun destroyBillingProcessor() {
        billingProcessor?.release()
        billingProcessor = null
    }

    /**
     * simply sets [toolbar] defined in Layout as supportActionBar
     * and set title for it
     */
    private fun addSupportActionBar() {
        setSupportActionBar(purchaseToolbar)
        supportActionBar?.setup()
        purchaseToolbar.title = "صفحه خرید نسخه کامل"
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * injects [R.array.why_to_purchase] items
     * and [R.array.why_to_purchase_drawables] into each
     * [textViews] TextView
     *
     */
    private fun setupTextViewList() {
        for (i in textViews.indices) {
            textViews[i] = findViewById(resources.getIdentifier("tv" + (i + 1), "id", packageName))
        }
        val whyToPurchaseStringArray = resources.getStringArray(R.array.why_to_purchase)
        val iconIdsArray = resources.obtainTypedArray(R.array.why_to_purchase_drawables)
        textViews.forEachIndexed { index, textView ->
            textView?.text = whyToPurchaseStringArray[index]

            textView?.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconIdsArray.getResourceId(index, 0), 0)
        }
        iconIdsArray.recycle()
    }


    /**
     *
     * when cafeBazaar billing service is connected
     * hides [progressLoading] and shows [btnPurchase]
     * and activates products already purchased before
     */
    override fun onBillingInitialized() {
        btnPurchase?.visible()
        progressLoading?.gone()
        val ownedList = billingProcessor?.listOwnedProducts()
        ownedList?.forEach { sku -> activateProduct(sku) }
    }


    /**
     * when product purchased successfully activates it
     *
     * @param sku identifier of product that purchased
     * @param transactionDetails details of successful transaction
     */
    override fun onProductPurchased(sku: String, transactionDetails: TransactionDetails?) {
        activateProduct(sku)
    }


    /**
     * NO-OPERATION!
     */
    override fun onPurchaseHistoryRestored() {}


    /**
     * Reports User Any Error occurred during purchase process by Showing
     * a SnackBar with an action based on the Error Code
     *
     * @param errorCode predefined code for the error occurred
     * @param throwable The error object that passed occasionally, usually *null*
     */
    override fun onBillingError(errorCode: Int, throwable: Throwable?) {
        var errorAction = View.OnClickListener { finish() }//Default Behavior
        var actionText = "خروج"
        val errorMessage: String =
                when (errorCode) {
                    Constants.CAFE_BAZAAR_NOT_INSTALLED -> {
                        "خطای $errorCode کافه بازار نصب نیست!"
                    }
                    Constants.BILLING_RESPONSE_RESULT_ERROR -> {
                        errorAction = View.OnClickListener { /*NO-OP*/ }
                        actionText = "خُب"
                        "خطای " + errorCode + "مشکل در دریافت اطلاعات پرداخت، اینترنت دستگاه را بررسی کنید"
                    }
                    Constants.BILLING_RESPONSE_RESULT_USER_CANCELED -> {
                        errorAction = View.OnClickListener { billingProcessor?.purchase(this, SKU) }
                        actionText = "تلاش مجدد"
                        "پرداخت ناموفق بود!"
                    }
                    else -> {// Default Behavior will be triggered
                        "خطای شماره " + errorCode + "رخ داد"
                    }
                }
        Snackbar.make(window.decorView, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(actionText, errorAction).show()
    }


    /**
     * give user access to Full Version if user purchased [SKU] successfully
     * or user did purchase one of [R.array.azera_sku_list] items
     * in Azera project(previous failed project that this project is an update for it)
     *
     *
     * @param ownedProduct sku of product user just owned
     */
    private fun activateProduct(ownedProduct: String) {
        LogCat.log(msg = "owned : $ownedProduct")
        val azeraSkuList = resources.getStringArray(R.array.azera_sku_list)
        if (ownedProduct == SKU || azeraSkuList.contains(ownedProduct)) {
            for (examCat in Exam.Category.values()) {
                AppRepository.getInstance(App.instance).activateExamAsync(examCat)
            }
            Toast.makeText(this, "کاربر ویژه شدید", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CODE_SUCCESS)
            this.finish()
        }
    }

    /**
     * if result is not handled by [billingProcessor] then let
     * activity handle it
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        billingProcessor?.apply {
            if (!handleActivityResult(requestCode, resultCode, data))
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * is called when activity is *destroyed*, also releases [billingProcessor]
     */
    public override fun onDestroy() {
        destroyBillingProcessor()
        super.onDestroy()
    }

    /**
     * @return RSA public key from cafeBazaar
     */
    private fun generateCafeBazaarRsa(): String {
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
            if (i == 3) rrsa.replace(121.toChar(), 89.toChar()).replace(100.toChar(), 109.toChar())
            if (ii == 7) rrsa.replace(113.toChar(), 81.toChar()).replace(114.toChar(), 82.toChar())
            when (i) {
                1 -> rsa.append(
                        rrsa.replace(109.toChar(), 77.toChar()).replace(
                                56.toChar(),
                                48.toChar()
                        )
                )
                6 -> rsa.append(
                        rrsa.replace(66.toChar(), 105.toChar()).replace(
                                122.toChar(),
                                88.toChar()
                        )
                )
                else -> rsa.append(rrsa)
            }

        }
        return rsa.toString()
    }


}
