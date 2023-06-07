package com.example.piashop.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piashop.BuildConfig
import com.example.piashop.R
import com.example.piashop.persistence.configurations
import com.example.piashop.ui.theme.PiaShopTheme
import com.example.piashop.ui.views.error.FieldStatus
import com.example.piashop.ui.views.error.urlValidation
import com.example.piashop.ui.views.error.validation
import com.example.piashop.ui.widget.SettingTextFieldState
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


/**
 * Backend Setting Item Data definitions
 */
// TODO: Figure out a way to pass text with string resource in `name`
sealed class BackendSettingItem(
    open val name: String,
    val keyboardType: KeyboardType = KeyboardType.Text
) {
    data class TestEnvironment(
        override val name: String = "Test Environment",
        var url: String,
        val validation: (String) -> FieldStatus = { text -> urlValidation(name, text) }
    ) : BackendSettingItem(name = name)

    data class ProductionEnvironment(
        override val name: String = "Production Environment",
        var url: String,
        val validation: (String) -> FieldStatus = { text -> urlValidation(name, text) }
    ) : BackendSettingItem(name = name)

    data class TestMerchantID(
        var id: String,
        override val name: String = "Test Merchant ID",
        val characterLimit: Int = -1,
        val validation: (String) -> FieldStatus = { text -> validation(name, text, characterLimit) }
    ) : BackendSettingItem(name = name, keyboardType = KeyboardType.Number)

    data class ProductionMerchantID(
        var id: String,
        override val name: String = "Production Merchant ID",
        val characterLimit: Int = -1,
        val validation: (String) -> FieldStatus = { text -> validation(name, text, characterLimit) }
    ) : BackendSettingItem(name = name, keyboardType = KeyboardType.Number)

}

/**
 * BESetting Item data
 */
private val beBaseUrlItemList: List<BackendSettingItem> = listOf(
    BackendSettingItem.TestEnvironment(url = BuildConfig.MERCHANT_BACKEND_URL_TEST),
    BackendSettingItem.ProductionEnvironment(url = BuildConfig.MERCHANT_BACKEND_URL_PROD)
)

private val beMerchantIdItemList: List<BackendSettingItem> = listOf(
    BackendSettingItem.TestMerchantID(id = BuildConfig.MERCHANT_ID_TEST),
    BackendSettingItem.ProductionMerchantID(id = BuildConfig.MERCHANT_ID_PROD)
)

// endregion

// BESetting Screen UI
/**
 * Merchant Screen Ui function
 */
@Composable
@Preview
fun MerchantSettingsTab() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.merchant_settings),
            modifier = Modifier.padding(vertical = 30.dp, horizontal = 10.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.W900
        )

        Text(
            text = stringResource(id = R.string.merchant_backend_base_url),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.W500
        )

        BackendSettingUiLazyColumn(
            beSettingItemList = beBaseUrlItemList,
            sectionTitle = { return@BackendSettingUiLazyColumn it.name },
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 10.dp)
        )

        Text(
            text = stringResource(id = R.string.netaxept_merchant_id),
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.W500
        )

        BackendSettingUiLazyColumn(
            beSettingItemList = beMerchantIdItemList,
            sectionTitle = { return@BackendSettingUiLazyColumn it.name },
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 10.dp)
        )
    }
}


@Composable
fun BackendSettingUiLazyColumn(
    beSettingItemList: List<BackendSettingItem>,
    sectionTitle: (BackendSettingItem) -> String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val sections: MutableMap<String, MutableList<BackendSettingItem>> = beSettingItemList.fold(mutableMapOf())
    { acc, settingItem ->
        val sectionTitle = sectionTitle.invoke(settingItem)
        if (acc[sectionTitle] == null) acc[sectionTitle] = mutableListOf(settingItem)
        else acc[sectionTitle]?.add(settingItem)
        acc
    }

    LazyColumn(
        modifier = Modifier
            .wrapContentHeight()
            .composed { modifier }
    ) {

        sections.forEach { (section, beSettingItemList) ->

            item {
                Text(
                    text = section,
                    modifier = Modifier
                        .clickable(onClick = {})
                        .padding(top = 10.dp, bottom = 5.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Normal
                )
            }

            items(beSettingItemList) { beSettingItem ->
                when (beSettingItem) {
                    is BackendSettingItem.TestEnvironment -> {
                        beSettingItem.testEnvironmentUrlTextField(context, section)
                    }

                    is BackendSettingItem.ProductionEnvironment -> {
                        beSettingItem.productionEnvironmentUrlTextField(context, section)
                    }

                    is BackendSettingItem.TestMerchantID -> {
                        beSettingItem.testMerchantIdTextField(context, section)
                    }

                    is BackendSettingItem.ProductionMerchantID -> {
                        beSettingItem.productionMerchantIdTextField(context, section)
                    }
                }
            }
        }
    }
}


/**
 * Test environment url text field with shared preferences data
 */
private val testEnvironmentUrlTextField:
        @Composable BackendSettingItem.TestEnvironment.(Context, String) -> Unit = { context, placeholder ->
    SettingTextFieldState(
        placeholder = placeholder,
        text = context.configurations.test.baseURL,
        keyboardType = this.keyboardType,
        validation = this.validation
    ) {
        context.configurations.test.baseURL = it
    }
}

/**
 * Production environment url text field with shared preferences data
 */
private val productionEnvironmentUrlTextField:
        @Composable BackendSettingItem.ProductionEnvironment.(Context, String) -> Unit = { context, placeholder ->
    SettingTextFieldState(
        placeholder = placeholder,
        text = context.configurations.prod.baseURL,
        keyboardType = this.keyboardType,
        validation = this.validation
    ) {
        context.configurations.prod.baseURL = it
    }
}

/**
 * Test Merchant Id text field with shared preferences data
 */
private val testMerchantIdTextField:
        @Composable BackendSettingItem.TestMerchantID.(Context, String) -> Unit = { context, placeholder ->
    SettingTextFieldState(
        placeholder = placeholder,
        text = context.configurations.test.merchantID,
        characterLimit = this.characterLimit,
        keyboardType = this.keyboardType,
        validation = this.validation
    ) {
        context.configurations.test.merchantID = it
    }
}

/**
 * Production Merchant Id text field with shared preferences data
 */
private val productionMerchantIdTextField:
        @Composable BackendSettingItem.ProductionMerchantID.(Context, String) -> Unit = { context, placeholder ->
    SettingTextFieldState(
        placeholder = placeholder,
        text = context.configurations.prod.merchantID,
        characterLimit = this.characterLimit,
        keyboardType = this.keyboardType,
        validation = this.validation
    ) {
        context.configurations.prod.merchantID = it
    }
}

// endregion

// Preview Setting Screen
@Preview
@Composable
private fun PreviewSettingsScreenDarkMode() {
    PiaShopTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .fillMaxSize()
        ) {
            MerchantSettingsTab()
        }
    }
}

@Preview
@Composable
private fun PreviewSettingsScreenLightMode() {
    PiaShopTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .fillMaxSize()
        ) {
            MerchantSettingsTab()
        }
    }
}

// endregion