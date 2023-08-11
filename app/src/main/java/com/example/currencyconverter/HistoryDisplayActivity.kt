package com.example.currencyconverter

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryDisplayActivity : AppCompatActivity() {

    private lateinit var clRoot : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_display)

        clRoot = findViewById(R.id.clRoot)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Currency Converter History"

        updateActivityUI()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_history_display, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.mi_clear -> {
                val db = DBHelper(this, null)
                db.deleteRecords()
                updateActivityUI()
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val res = resources
        val portrait = res.getDrawable(R.drawable.money_portrait)
        val landscape = res.getDrawable(R.drawable.money_landscape)

        // Remember to add android:configChanges="orientation|keyboardHidden|screenSize"
        // to AndroidManifest.xml
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            clRoot.background = landscape
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {;
            clRoot.background = portrait
        }
    }

    private fun updateActivityUI() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val db = DBHelper(this, null)
        val currencyRecords = db.getRecords()
        val adapter = RecyclerViewCustomAdapter(currencyRecords)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}