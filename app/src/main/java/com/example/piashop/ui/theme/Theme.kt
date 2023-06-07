package com.example.piashop.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = Black, // color displayed most frequently across your app's screens and components;
    primaryVariant = WhiteSmoke, // color is used to distinguish elements using primary colors, such as top app bar and the system bar.
    secondary = Blue, // color provides more ways to accent and distinguish your product. Having a secondary color is optional, and should be applied sparingly to accent select parts of your UI;
    secondaryVariant = SunGlow, // color is used to distinguish elements using secondary colours;
    background = Black, // color appears behind scrollable content;
    surface = Black, // color uses on surfaces of components, like cards and menus;
    onPrimary = White, // color of text and icons displayed on top of the primary color.
    onSecondary = White, // color of text and icons displayed on top of the secondary color;
    onBackground = WhiteSmoke, // color of text and icons displayed on top of the background color;
    onSurface = WhiteSmoke, // color of text and icons displayed on top of the surface color;
)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    primary = White,
    primaryVariant = StormDust,
    secondary = SunGlow,
    secondaryVariant = Blue,
    background = White,
    surface = White,
    onPrimary = Black,
    onSecondary = StormDust,
    onBackground = WhiteSmoke,
    onSurface = AshGrey
)

@Composable
fun PiaShopTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

object ThemeState {
    var isCurrentDarkTheme : MutableState<Boolean> = mutableStateOf(false)
}