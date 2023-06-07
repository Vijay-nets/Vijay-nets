package com.example.piashop.ui.views.checkout

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

// currency locale region
data class CurrencyLocale(
    val currencyCode: String,
    val languageCode: String,
    val countryCode: String
)

val currencies: List<CurrencyLocale> = listOf(
    CurrencyLocale(currencyCode = "EUR", languageCode = "en", countryCode = "FI"),
    CurrencyLocale(currencyCode = "SEK", languageCode = "en", countryCode = "SE"),
    CurrencyLocale(currencyCode = "DKK", languageCode = "en", countryCode = "DK"),
    CurrencyLocale(currencyCode = "NOK", languageCode = "en", countryCode = "NO")
)


/** Format integer value to currency as per country and language  */
fun Int.localeCurrencyFormat(
    languageCode: String,
    countryCode: String
) : String {
    val currencyFormat = (NumberFormat
        .getCurrencyInstance(Locale(languageCode, countryCode)) as DecimalFormat)

    val currencySymbol = (currencyFormat.decimalFormatSymbols as DecimalFormatSymbols)
    currencySymbol.currencySymbol = ""
    currencyFormat.decimalFormatSymbols = currencySymbol
    return currencyFormat.format(this)
}


// end region