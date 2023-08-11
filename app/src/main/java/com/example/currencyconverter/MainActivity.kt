package com.example.currencyconverter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableString
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime



class MainActivity : AppCompatActivity() {

    private lateinit var svMainScroll : ScrollView
    private lateinit var tvBaseCurrencyCode : TextView
    private lateinit var etBaseCurrencyAmount : EditText
    private lateinit var tvTargetCurrencyCode : TextView
    private lateinit var tvTargetCurrencyAmount : TextView
    private lateinit var spBaseCurrencyDescriptions : Spinner
    private lateinit var spTargetCurrencyDescriptions : Spinner
    private lateinit var ivGo : ImageView
    private lateinit var ivSwitch : ImageView

    private lateinit var currencyMap : Map<String, String>
    private lateinit var currencyCodeArray : Array<String?>
    private lateinit var currencyDescriptionArray : Array<String?>

    companion object {
        private const val TAG = "MainActivity"
        private const val rapidAPIKey = "xxxxxxxxxxx"
    }

    // declares UI and setups the UI
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        svMainScroll = findViewById(R.id.svMainScroll)
        tvBaseCurrencyCode = findViewById(R.id.tvBaseCurrencyCode)
        etBaseCurrencyAmount = findViewById(R.id.etBaseCurrencyAmount)
        tvTargetCurrencyCode = findViewById(R.id.tvTargetCurrencyCode)
        tvTargetCurrencyAmount = findViewById(R.id.tvTargetCurrencyAmount)
        spBaseCurrencyDescriptions = findViewById(R.id.spBaseCurrencyDescriptions)
        spTargetCurrencyDescriptions = findViewById(R.id.spTargetCurrencyDescriptions)
        ivGo = findViewById(R.id.ivGo)
        ivSwitch = findViewById(R.id.ivSwitch)

        etBaseCurrencyAmount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(10,2))

        spBaseCurrencyDescriptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // not used, but declaration needed
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val currencyCode = getCurrencyCode(spBaseCurrencyDescriptions.selectedItem as String)
                tvBaseCurrencyCode.text = currencyCode
            }
        }

        spTargetCurrencyDescriptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                // not used, but declaration needed
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val currencyCode = getCurrencyCode(spTargetCurrencyDescriptions.selectedItem as String)
                tvTargetCurrencyCode.text = currencyCode
            }
        }

        ivGo.setOnClickListener {
            fetchCurrencyConversion()
        }

        ivSwitch.setOnClickListener {
            val codeHolder = tvTargetCurrencyCode.text
            tvTargetCurrencyCode.text = tvBaseCurrencyCode.text
            tvBaseCurrencyCode.text = codeHolder

            val descriptionHolder = spTargetCurrencyDescriptions.selectedItemPosition
            spTargetCurrencyDescriptions.setSelection(spBaseCurrencyDescriptions.selectedItemPosition)
            spBaseCurrencyDescriptions.setSelection(descriptionHolder)

            val amountHolder = tvTargetCurrencyAmount.text
            tvTargetCurrencyAmount.text = etBaseCurrencyAmount.text
            etBaseCurrencyAmount.setText(amountHolder)

        }
        // After UI components declared and initialized
        fetchInitDataAndUpdateUI()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val res = resources
        val portrait = res.getDrawable(R.drawable.money_portrait)
        val landscape = res.getDrawable(R.drawable.money_landscape)

        // Remember to add android:configChanges="orientation|keyboardHidden|screenSize"
        // to AndroidManifest.xml
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            svMainScroll.background = landscape
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {;
            svMainScroll.background = portrait
        }
    }


    // creates the options menu in the top-right corner
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    // handles the event when an options item is clicked
    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.mi_history -> {
                val intent = Intent(this, HistoryDisplayActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    private fun fetchInitDataAndUpdateUI()  {
        var result : Map<String, String>

        lifecycleScope.launch(Dispatchers.IO) {
            result = retrieveCurrencyData()

            val currDescriptionsArray = retrieveCurrencyDescriptions(result)
            val currCodesArray = retrieveCurrencyCodes(result)

            currDescriptionsArray.sort()
            currCodesArray.sort()

            withContext(Dispatchers.Main) {
                var currArrayAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, currDescriptionsArray)

                currArrayAdapter.setDropDownViewResource(R.layout.spinner_style)

                spBaseCurrencyDescriptions.adapter = currArrayAdapter
                spTargetCurrencyDescriptions.adapter = currArrayAdapter

                spBaseCurrencyDescriptions.setSelection(152)
                spTargetCurrencyDescriptions.setSelection(147)

                currencyCodeArray = currCodesArray
                currencyDescriptionArray = currDescriptionsArray
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchCurrencyConversion() {
        var resultMap : Map<String, String>
        var conversionAmount : String? = null

        val baseCode = SpannableString(tvBaseCurrencyCode.text).toString()
        val targetCode = SpannableString(tvTargetCurrencyCode.text).toString()
        val baseAmount = SpannableString(etBaseCurrencyAmount.text).toString()

        lifecycleScope.launch(Dispatchers.IO) {
            resultMap = convertCurrencyAmount(baseCode, targetCode, baseAmount)

            val conversionStatus = getConversionStatus(resultMap)
            val message : String
            if (conversionStatus == "success") {
                message = "API call is a success"
                conversionAmount = getConversionAmount(resultMap)
                Log.i(TAG,message)
            }
            else{
                message = "API call not successful"
                Log.i(TAG, message)
            }

            withContext(Dispatchers.Main) {
                if (conversionAmount != null) {
                    tvTargetCurrencyAmount.text = conversionAmount
                }
                else {
                    tvTargetCurrencyAmount.text = "NaN"
                }

                val currencyConversionRecord = getConversionRecordData()
                insertRecordIntoDB(currencyConversionRecord)
            }
        }
    }

    private fun retrieveCurrencyData() : Map<String, String> {
        val result : String?
        val client = OkHttpClient()
        var resultMap  = mapOf<String, String>()

        try {
            val request = Request.Builder()
                .url("https://currency-converter5.p.rapidapi.com/currency/list")
                .get()
                .addHeader("X-RapidAPI-Key", rapidAPIKey)
                .addHeader("X-RapidAPI-Host", "currency-converter5.p.rapidapi.com")
                .build()

            val response = client.newCall(request).execute()
            result = response.body?.string()

            if(result != null) {
                try{
                    val gson = Gson()
                    resultMap = gson.fromJson<Map<String, String>>(result, MutableMap::class.java)
                }
                catch(err:Error) {
                    print("Error when parsing Json: " + err.localizedMessage)
                }
            }
            else{
                print("API returned no response")
            }
        }
        catch(err:Error){
            print("Error occurred when CurrencyTypes request was executed: " + err.localizedMessage)
        }
        return resultMap

    }

    private fun getConversionRecordData(): CurrencyConversionRecord {
        val baseCode = tvBaseCurrencyCode.text as String
        val baseDescription = spBaseCurrencyDescriptions.selectedItem as String
        val baseAmount = SpannableString(etBaseCurrencyAmount.text).toString()
        val targetCode = tvTargetCurrencyCode.text as String
        val targetDescription = spTargetCurrencyDescriptions.selectedItem as String
        val targetAmount = tvTargetCurrencyAmount.text as String
        val localDateTime = LocalDateTime.now()

        return CurrencyConversionRecord(
            null,
            baseCode,
            baseDescription,
            baseAmount.toDouble(),
            targetCode,
            targetDescription,
            targetAmount.toDouble(),
            localDateTime
        )
    }

    private fun insertRecordIntoDB(currencyConversionRecord: CurrencyConversionRecord?) {

        val message : String = if (currencyConversionRecord != null) {
            val db = DBHelper(this, null)
            db.addRecord(currencyConversionRecord)
            "Record saved into database"
        } else{
            "Unable to save record in database"
        }

        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
    }

    private fun getCurrencyCode(value: String): String {
        for (key in currencyMap.keys) {
            if (value == currencyMap[key]) {
                return key
            }
        }
        return ""
    }

    private fun getConversionAmount(map: Map<String, String>): String? {
        var conversionAmount : String?
        for (entry in map) {
            if(entry.key == "rates"){
                val ratesMap = entry.value as Map<String, String>
                for (ratesEntry in ratesMap) {
                    if(ratesEntry.key == tvTargetCurrencyCode.text as String){
                        val currencyCodeMap = ratesEntry.value as Map<String, String>
                        conversionAmount = currencyCodeMap["rate_for_amount"]

                        if (conversionAmount != null){
                            val doubleTypeAmount : Double = conversionAmount.toDouble()
                            val decimalFormat = DecimalFormat("#.##")
                            decimalFormat.roundingMode = RoundingMode.CEILING
                            conversionAmount = decimalFormat.format(doubleTypeAmount)
                        }
                        return conversionAmount
                    }

                }
            }
        }
        return null
    }

    private fun getConversionStatus(map: Map<String, String>): String {
        for (entry in map) {
            if(entry.key == "status"){
                return entry.value
            }
        }
        return ""
    }

    private fun convertCurrencyAmount(baseCurrencyCode: String, targetCurrencyCode: String, amount:String) : Map<String, String> {
        val result : String?
        val client = OkHttpClient()
        var resultMap  = mapOf<String, String>()

        try {
            val request = Request.Builder()
                .url("https://currency-converter5.p.rapidapi.com/currency/convert?format=json&from=" + baseCurrencyCode + "&to=" + targetCurrencyCode +
                        "&amount=" + amount)
                .get()
                .addHeader("X-RapidAPI-Key", rapidAPIKey)
                .addHeader("X-RapidAPI-Host", "currency-converter5.p.rapidapi.com")
                .build()

            val response = client.newCall(request).execute()
            result = response.body?.string()

            if(result != null) {
                try{
                    val gson = Gson()
                    resultMap = gson.fromJson<Map<String, String>>(result, MutableMap::class.java)
                }
                catch(err:Error) {
                    print("Error when parsing Json: " + err.localizedMessage)
                }
            }
            else{
                print("API returned no response")
            }
        }
        catch(err:Error){
            print("Error occurred when CurrencyTypes request was executed: " + err.localizedMessage)
        }
        return resultMap

    }

    private fun retrieveCurrencyCodes(map: Map<String, String>): Array<String?> {
        val currCodeList = mutableListOf<String>()
        for (entry in map) {
            if (entry.key == "currencies") {
                val currMap = entry.value as Map<String, String>
                currencyMap = currMap
                for (entry2 in currMap) {
                    currCodeList.add(entry2.key)
                }
            }
        }

        return currCodeList.toTypedArray()
    }

    private fun retrieveCurrencyDescriptions(map: Map<String, String>): Array<String?> {
        val currDescList = mutableListOf<String>()
        for (entry in map) {
            if (entry.key == "currencies") {
                val curr = entry.value as Map<String, String>
                for (entry2 in curr) {
                    currDescList.add(entry2.value)
                }
            }
        }

        return currDescList.toTypedArray()
    }
}