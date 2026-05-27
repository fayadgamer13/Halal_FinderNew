package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Default false to enable custom theme accent picker
  customPrimaryColor: androidx.compose.ui.graphics.Color? = null,
  customContainerColor: androidx.compose.ui.graphics.Color? = null,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      customPrimaryColor != null -> {
        if (darkTheme) {
          darkColorScheme(
            primary = customPrimaryColor,
            primaryContainer = customContainerColor ?: androidx.compose.ui.graphics.Color(0xFF2D3C3D),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = customPrimaryColor,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = androidx.compose.ui.graphics.Color(0xFF101414),
            surface = androidx.compose.ui.graphics.Color(0xFF181C1C)
          )
        } else {
          lightColorScheme(
            primary = customPrimaryColor,
            primaryContainer = customContainerColor ?: androidx.compose.ui.graphics.Color(0xFFD1E8D1),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = customPrimaryColor,
            onSecondary = androidx.compose.ui.graphics.Color.White,
            background = androidx.compose.ui.graphics.Color(0xFFF7FBF2),
            surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
          )
        }
      }

      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
