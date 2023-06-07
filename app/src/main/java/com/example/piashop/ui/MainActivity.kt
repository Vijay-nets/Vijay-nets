package com.example.piashop.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.piashop.R
import com.example.piashop.integration.*
import com.example.piashop.ui.extensions.CollapsibleView
import com.example.piashop.ui.theme.*
import com.example.piashop.ui.views.NavigationView
import com.piasdk.api.CardView
import com.piasdk.api.PiaSDK


class MainActivity : ComponentActivity() {

    private lateinit var shopTabViewModelApi: ShopTabViewModelApi

    @Composable
    private fun PiaCardView() {
        PiaSDK.CardView(
            cardProcess = shopTabViewModelApi.cardPayment,
            onCanSubmit = shopTabViewModelApi::enableButtonForCardProcess,
            onResult = shopTabViewModelApi::onResult
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shopTabViewModelApi = shopTabViewModelApi(completionMessage = this::toastCompletionMessage)

        setContent {
            window.statusBarColor = statusBarColor()

            PiaShopTheme(darkTheme = isSystemInDarkTheme()) {
                Surface {
                    NavigationView(
                        shopTabViewModelApi = shopTabViewModelApi,
                        cardEntryView = {
                            CollapsibleView(
                                title = getString(R.string.pay_with_card),
                                borderColor = Color.PiaCardViewBorder,
                                content = { PiaCardView() }
                            )
                        }
                    )
                }
            }
        }

    }
}

fun Context.toastCompletionMessage(message: CompletionMessage) {
    Toast.makeText(this, message.source, Toast.LENGTH_LONG).show()
}

@Composable
@ReadOnlyComposable
fun statusBarColor() : Int = MaterialTheme.colors.onSurface.copy(
    alpha = if (isSystemInDarkTheme()) 1.0f else 0.4f
).toArgb()