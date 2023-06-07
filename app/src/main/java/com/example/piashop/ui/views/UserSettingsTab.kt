package com.example.piashop.ui.views

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import com.example.piashop.ui.views.SettingItem.*
import com.example.piashop.ui.views.error.FieldStatus
import com.example.piashop.ui.views.error.validation
import com.example.piashop.ui.widget.SettingRadioState
import com.example.piashop.ui.widget.SettingSwitchState
import com.example.piashop.ui.widget.SettingTextFieldState
import com.example.piashop.ui.widget.addImageVector
import com.piasdk.api.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


/**
 * Setting Item Data definitions
 */
private const val registerNewCardResource = R.string.register_new_card
private const val registerSBusinessCardResource = R.string.register_s_business_card
const val useTestEnvironmentTitleResource = R.string.use_test_environment
const val excludeDankortAndMastercardTitleResource = R.string.exclude_dankort_and_mastercard
const val disableCoBrandedDankortCardTitleResource= R.string.disable_co_branded_dankort
private val sdkLanguages : @Composable () -> List<String> = {
    listOf(
        stringResource(id = R.string.language_system),
        stringResource(id = R.string.language_english),
        stringResource(id = R.string.language_danish),
        stringResource(id = R.string.language_french),
        stringResource(id = R.string.language_finnish)
    )
}

// TODO: Figure out a way to pass text with string resource in `name`
private sealed class SettingItem(open val name: String, val keyboardType: KeyboardType = KeyboardType.Text){
    data class CustomerId(
        var id: String,
        override val name: String = "Consumer ID",
        val characterLimit: Int = 6,
        val validation: (String) -> FieldStatus = { text -> validation(name, text, characterLimit) }
    ) : SettingItem(name = name, keyboardType = KeyboardType.Number)

    data class ApplicationVersion(@StringRes val versionResource: Int, val information: String? = null) :
        SettingItem(name = "Application Version", keyboardType = KeyboardType.Number)

    data class PhoneNumber(
        override val name: String = "Phone Number",
        var number: String,
        val characterLimit: Int = 10,
        val validation: (String) -> FieldStatus = { text -> validation(name, text, characterLimit) }
    ) : SettingItem(name = name, keyboardType = KeyboardType.Phone)

    data class Features(@StringRes val titleResource: Int) : SettingItem(name = "Features")

    data class Configurations(@StringRes val titleResource: Int, var selection: Boolean = false) : SettingItem(name = "Configurations")

    object Language : SettingItem(name = "Languages")
}

/**
 * Setting Item data
 */
private val settingItemList: List<SettingItem> = (0..8).map { index ->
    when (index) {
        0 -> {
            CustomerId(id = "")
        }
        1 -> {
            ApplicationVersion(
                versionResource = R.string.application_version
            )
        }
        2 -> {
            PhoneNumber(number = "")
        }
        in 3..4 -> {
            listOf(
                Features(titleResource = registerNewCardResource),
                Features(titleResource = registerSBusinessCardResource)
            ).get(index = index - 3)
        }
        5 -> {
            Language
        }
        else -> {
            listOf(
                Configurations(titleResource = useTestEnvironmentTitleResource),
                Configurations(titleResource = excludeDankortAndMastercardTitleResource),
                Configurations(titleResource = disableCoBrandedDankortCardTitleResource),
            ).get(index = index - 6)
        }
    }
}

// endregion

// Setting Screen UI
/**
 * Setting Screen Ui function
 */
@Composable
fun UserSettingsTab() {
    Column {
        Text(
            text = stringResource(R.string.user_settings_title),
            modifier = Modifier.padding(vertical = 30.dp, horizontal = 10.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.W900
        )
        SettingUiLazyColumn(
            settingItemList = settingItemList,
            sectionTitle = { return@SettingUiLazyColumn it.name },
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        )
    }
}

@Composable
private fun SettingUiLazyColumn(
    settingItemList: List<SettingItem>,
    sectionTitle: (SettingItem) -> String,
    modifier: Modifier
) {
    val context = LocalContext.current
    val sections: MutableMap<String, MutableList<SettingItem>> = settingItemList.fold(mutableMapOf())
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

        sections.forEach { (section, settingItemList) ->

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

            items(settingItemList) { settingItem ->
                when (settingItem) {
                    is CustomerId -> {
                        settingItem.customerIdTextField(context, section)
                    }
                    is ApplicationVersion -> {
                        settingItem.applicationVersionTextField(context, section)
                    }
                    is PhoneNumber -> {
                        settingItem.phoneNumberTextField(context, section)
                    }
                    is Features -> {
                        SettingSwitchState(
                            text = stringResource(id = settingItem.titleResource),
                            selected = false,
                            imageVector = addImageVector
                        )
                    }

                    is Configurations -> {
                        val configurations = context.configurations

                        when (settingItem.titleResource) {
                            useTestEnvironmentTitleResource -> {
                                settingItem.configurationSwitch(configurations.isTestMode) {
                                    configurations.isTestMode = it
                                }
                            }

                            disableCoBrandedDankortCardTitleResource -> {
                                settingItem.configurationSwitch(configurations.disableCoBrandedDankortCard) {
                                    configurations.disableCoBrandedDankortCard = it
                                    if (configurations.disableCoBrandedDankortCard) {
                                        PiaSDK.excludedCardSchemes.add(DankortVisa)
                                    } else {
                                        PiaSDK.excludedCardSchemes.remove(DankortVisa)
                                    }
                                }
                            }

                            excludeDankortAndMastercardTitleResource -> {
                                settingItem.configurationSwitch(configurations.excludeDankortAndMastercard) {
                                    configurations.excludeDankortAndMastercard = it
                                    if (configurations.excludeDankortAndMastercard) {
                                        setOf(Dankort, MasterCard)
                                            .forEach(PiaSDK.excludedCardSchemes::add)
                                    } else {
                                        setOf(Dankort, MasterCard)
                                            .forEach(PiaSDK.excludedCardSchemes::remove)
                                    }
                                }
                            }
                        }
                    }
                    is Language -> {
                        languageRadioList (PiaSDK.language()) {
                            PiaSDK.language = {
                                when (it) {
                                    "English" -> PiaEnglish
                                    "French" -> PiaFrench
                                    "Finnish" -> PiaFinnish
                                    "Danish" -> PiaDanish
                                    else -> null
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Customer Id text field with shared preferences data
 */
private val customerIdTextField:
        @Composable CustomerId.(Context, String) -> Unit = { context, placeholder ->
    SettingTextFieldState(
        placeholder = placeholder,
        text = context.configurations.customerID,
        keyboardType = this.keyboardType,
        characterLimit = this.characterLimit,
        validation = this.validation
    ) {
        context.configurations.customerID = it
    }
}

/**
 * Application Version text field
 */
private val applicationVersionTextField:
        @Composable ApplicationVersion.(Context, String) -> Unit = { _, placeholder ->
    var text = stringResource(
        id = this.versionResource,
        BuildConfig.VERSION_NAME,
        BuildConfig.APPLICATION_VERSION_INFO_BRANCH,
        BuildConfig.APPLICATION_VERSION_INFO_HASH
    )
    this.information?.let { text = "$text ($it)" }
    SettingTextFieldState(
        placeholder = placeholder,
        text = text,
        keyboardType = this.keyboardType,
        imageVector = { null }
    ) {

    }
}

/**
 * Phone Number text field with shared preferences data
 */
private val phoneNumberTextField:
        @Composable PhoneNumber.(Context, String) -> Unit = { context, placeholder ->
    SettingTextFieldState(
        placeholder = placeholder,
        text = context.configurations.userPhoneNumber,
        characterLimit = this.characterLimit,
        keyboardType = this.keyboardType,
        validation = this.validation
    ) {
        context.configurations.userPhoneNumber = it
    }
}

/**
 * Configuration switch with shared preferences data
 */
private val configurationSwitch:
        @Composable Configurations.(Boolean, (Boolean) -> Unit) -> Unit = {  isSelected, onChange ->
    SettingSwitchState(
        textId = this.titleResource,
        selected = isSelected
    ) {
        onChange(it)
    }
}

/** Configure language list */
private val languageRadioList: @Composable (PiaLanguage?, (String) -> Unit) -> Unit =
    { itemSelected, onOptionSelect ->
        SettingRadioState(
            options = sdkLanguages(),
            onOptionSelect = onOptionSelect,
            selectedItem = itemSelected?.let {
                sdkLanguages().indexOf(it.javaClass.simpleName.removePrefix("Pia"))
            } ?: run { 0 }
        )
    }
// endregion

// Preview Setting Screen
@Preview
@Composable
fun PreviewUserScreenDarkMode() {
    PiaShopTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .fillMaxSize()
        ) {
            UserSettingsTab()
        }
    }
}

@Preview
@Composable
fun PreviewUserScreenLightMode() {
    PiaShopTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.primary)
                .fillMaxSize()
        ) {
            UserSettingsTab()
        }
    }
}

// endregion