package com.darts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

//private val DarkColorPalette = ColorScheme(
//  primary = LightBlue,
//  inversePrimary = DarkBlue,
//  secondary = DarkRed
//)
//
//private val LightColorPalette = ColorScheme(
//  primary = DarkBlue,
//  primaryVariant = Purple700,
//  secondary = DarkRed

  /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
//)

@Composable
fun DartsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//  val colors = if (darkTheme) {
//    DarkColorPalette
//  } else {
//    LightColorPalette
//  }

  MaterialTheme(
    // TODO -- update colors
    colorScheme = MaterialTheme.colorScheme,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}