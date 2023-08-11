package com.example.currencyconverter

import java.time.LocalDateTime

class CurrencyConversionRecord (
    var id : Int?,
    var baseCurrencyCode : String,
    var baseCurrencyDescription : String,
    var baseCurrencyAmount : Double,
    var targetCurrencyCode : String,
    var targetCurrencyDescription : String,
    var targetCurrencyAmount : Double,
    var recordDateTime : LocalDateTime
    )