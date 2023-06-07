package com.example.piashop.persistence

import android.content.Context
import kotlin.reflect.KProperty


//region Persisted properties (data)

val PersistedConfiguration.merchantID get() = if (isTestMode) test.merchantID else prod.merchantID
val PersistedConfiguration.baseURL get() = if (isTestMode) test.baseURL else prod.baseURL

//endregion

//region Setter Accessors (primarily used for setting)

val Context.configurations: PersistedConfiguration get() = PersistedConfiguration(context = this)

//endregion

// region - Persistence

/** Set of keys used to identify persisted properties.*/
enum class PersistenceKey {
    // Use `name` of these keys along with a unique prefix to persist and retrieve data
    // e.g. "${keyPrefix}.${PersistenceKey.isTestMode.name}"

    isTestMode,
    shouldHideCardTokenizationOption,
    customerID,
    userPhoneNumber,

    testMerchantID,
    testBaseURL,

    merchantID,
    baseURL,

    excludeDankortAndMastercard,
    disableCoBrandedDankortCard
}

/** Accessor to persisted properties that do not depend on test/prod environment. */
class PersistedConfiguration(context: Context) {

    val prod = Prod(context = context)
    val test = Test(context = context)

    var isTestMode by context.persisted(PersistenceKey.isTestMode, true)
    var shouldHideCardTokenizationOption by context.persisted(
        key = PersistenceKey.shouldHideCardTokenizationOption,
        defaultValue = true
    )
    var customerID by context.persisted(PersistenceKey.customerID, "000013")
    var userPhoneNumber by context.persisted(PersistenceKey.userPhoneNumber, "")

    var excludeDankortAndMastercard by context
        .persisted(PersistenceKey.excludeDankortAndMastercard, false)
    var disableCoBrandedDankortCard by context
        .persisted(PersistenceKey.disableCoBrandedDankortCard, false)

    /** Accessors to prod environment persisted properties. */
    class Prod(context: Context) {
        var merchantID by context.persisted(PersistenceKey.merchantID, NetsProd.merchantID)
        var baseURL by context.persisted(PersistenceKey.baseURL, NetsProd.baseURL)
    }

    /** Accessors to test environment persisted properties. */
    class Test(context: Context) {
        var merchantID by context.persisted(PersistenceKey.testMerchantID, NetsTest.merchantID)
        var baseURL by context.persisted(PersistenceKey.testBaseURL, NetsTest.baseURL)
    }
}



//endregion

//region - Persistence (in shared preferences)

private class PersistedItem<T>(
    context: Context,
    val key: PersistenceKey,
    var defaultValue: T
) {

    private val sharedPref = context.getSharedPreferences("Setting", Context.MODE_PRIVATE)
    private val sharedPrefKey = "${context.packageName}." + key.name

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        try {
            return when (property.returnType.classifier) {
                Int::class -> sharedPref.getInt(sharedPrefKey, defaultValue as Int)
                Long::class -> sharedPref.getLong(sharedPrefKey, defaultValue as Long)
                Boolean::class -> sharedPref.getBoolean(sharedPrefKey, defaultValue as Boolean)
                String::class -> sharedPref.getString(sharedPrefKey, defaultValue as String)
                else -> IllegalArgumentException(
                    "TODO: Missing accessor for type -- ${property.returnType.classifier}"
                )
            } as T
        } catch (e: ClassCastException) {
            return defaultValue
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        sharedPref.edit().apply {
            when (value) {
                is Int -> putInt(sharedPrefKey, value)
                is Long -> putLong(sharedPrefKey, value)
                is Boolean -> putBoolean(sharedPrefKey, value)
                is String -> putString(sharedPrefKey, value)
                else -> IllegalArgumentException(
                    "TODO: Missing setter for type -- ${property.returnType.classifier}"
                )
            }
            apply()
        }
    }
}

private fun <T> Context.persisted(key: PersistenceKey, defaultValue: T) : PersistedItem<T> {
    return PersistedItem(context = this, key = key, defaultValue = defaultValue)
}

//endregion

// region Nets Defaults

private object NetsProd {
/*#external_code_section_start
    const val merchantID: String = "YOUR PRODUCTION NETAXEPT MERCHANT ID HERE"
    const val baseURL: String = "YOUR PRODUCTION BACKEND BASE URL HERE"
#external_code_section_end*/
//#internal_code_section_start
    const val merchantID: String = "733255"
    const val baseURL: String = "https://api-gateway-pp.nets.eu/pia/merchantdemo/"
//#internal_code_section_end
}

private object NetsTest {
/*#external_code_section_start
    const val merchantID: String = "YOUR TEST NETAXEPT MERCHANT ID HERE"
    const val baseURL: String = "YOUR TEST BACKEND BASE URL HERE"
#external_code_section_end*/
//#internal_code_section_start
    const val merchantID: String = "12002835"
    const val baseURL: String = "https://api-gateway-pp.nets.eu/pia/test/merchantdemo/"
//#internal_code_section_end
}

//endregion