package com.example.currencyconverter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import kotlin.collections.ArrayList

class DBHelper (private val context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                BASE_CURRENCY_CODE_COL + " TEXT, " +
                BASE_CURRENCY_DESCRIPTION_COL + " TEXT, " +
                BASE_CURRENCY_AMOUNT_COL + " DOUBLE, " +
                TARGET_CURRENCY_CODE_COL + " TEXT, " +
                TARGET_CURRENCY_DESCRIPTION_COL + " TEXT, " +
                TARGET_CURRENCY_AMOUNT_COL + " DOUBLE, " +
                RECORD_DATE_TIME_COL + " DATETIME)"
                )

        db.execSQL(query)

    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addRecord(record : CurrencyConversionRecord?) {

        val values = ContentValues()
        if (record != null){
            with(values){
                put(ID_COL, record.id)
                put(BASE_CURRENCY_CODE_COL, record.baseCurrencyCode)
                put(BASE_CURRENCY_DESCRIPTION_COL, record.baseCurrencyDescription)
                put(BASE_CURRENCY_AMOUNT_COL, record.baseCurrencyAmount)
                put(TARGET_CURRENCY_CODE_COL, record.targetCurrencyCode)
                put(TARGET_CURRENCY_DESCRIPTION_COL, record.targetCurrencyDescription)
                put(TARGET_CURRENCY_AMOUNT_COL, record.targetCurrencyAmount)
                put(RECORD_DATE_TIME_COL, record.recordDateTime.toString())
            }
        }

        val db = this.writableDatabase

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    @SuppressLint("Recycle")
    fun getRecords() : ArrayList<ItemsViewModel> {

        val itemsViewModelArrayList : ArrayList<ItemsViewModel> = ArrayList()

        try{
            val db = this.readableDatabase
            val sqlQuery = "SELECT " + BASE_CURRENCY_CODE_COL + ", " + BASE_CURRENCY_DESCRIPTION_COL  + ", " +
                    BASE_CURRENCY_AMOUNT_COL  + ", " + TARGET_CURRENCY_CODE_COL  + ", " +
                    TARGET_CURRENCY_DESCRIPTION_COL  + ", " + TARGET_CURRENCY_AMOUNT_COL  + ", " +
                    RECORD_DATE_TIME_COL + " FROM " + TABLE_NAME

            val cursorRecords : Cursor = db.rawQuery(sqlQuery, null)

            if (cursorRecords.moveToFirst()) {
                do {
                    val itemsViewModel = ItemsViewModel(
                        cursorRecords.getString(1) + "(" + cursorRecords.getString(0) + ")",
                        cursorRecords.getDouble(2),
                        cursorRecords.getString(4) + "(" + cursorRecords.getString(3) + ")",
                        cursorRecords.getDouble(5),
                        cursorRecords.getString(6)
                    )

                    itemsViewModelArrayList.add(itemsViewModel)
                } while (cursorRecords.moveToNext())
            }
        }
        catch(Err:Error){
            val message = "Error getting records: " + Err.message
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        return itemsViewModelArrayList
    }

    fun deleteRecords() {
        try{
            val db = this.readableDatabase

            db.execSQL("DELETE FROM $TABLE_NAME")

        }
        catch(Err:Error){
            val message = "Error deleting records: " + Err.message
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {

        private const val DATABASE_NAME = "CURRENCY_CONVERSION_HISTORY"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "conversion_history"
        const val ID_COL = "id"
        const val BASE_CURRENCY_CODE_COL = "base_currency_code"
        const val BASE_CURRENCY_DESCRIPTION_COL = "base_currency_description"
        const val BASE_CURRENCY_AMOUNT_COL = "base_currency_amount"
        const val TARGET_CURRENCY_CODE_COL = "target_currency_code"
        const val TARGET_CURRENCY_DESCRIPTION_COL = "target_currency_description"
        const val TARGET_CURRENCY_AMOUNT_COL = "target_currency_amount"
        const val RECORD_DATE_TIME_COL = "record_date_time"
    }

}