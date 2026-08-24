package com.example.ui.theme
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AppThemeMode

data class BachatColors(
  val background: Color,
  val surface: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val divider: Color
)

val LightBachatColors = BachatColors(
  background = BachatBackground,
  surface = BachatSurface,
  textPrimary = BachatTextPrimary,
  textSecondary = BachatTextSecondary,
  divider = BachatDivider
)

val DarkBachatColors = BachatColors(
  background = BachatBackgroundDark,
  surface = BachatSurfaceDark,
  textPrimary = BachatTextPrimaryDark,
  textSecondary = BachatTextSecondaryDark,
  divider = BachatDividerDark
)


val LocalBachatColors = staticCompositionLocalOf { LightBachatColors }

object BachatTheme {
  val colors: BachatColors
    @Composable
    get() = LocalBachatColors.current
}

private val LightColorScheme = lightColorScheme(
  primary = BachatInk,
  onPrimary = BachatOnColor,
  primaryContainer = BachatSurface,
  onPrimaryContainer = BachatTextPrimary,
  secondary = BachatTextSecondary,
  onSecondary = BachatOnColor,
  background = BachatBackground,
  onBackground = BachatTextPrimary,
  surface = BachatBackground,
  onSurface = BachatTextPrimary,
  surfaceVariant = BachatSurface,
  onSurfaceVariant = BachatTextSecondary,
  outline = BachatDivider,
  error = BachatDanger,
  onError = BachatOnColor,
  errorContainer = BachatDangerTint,
  onErrorContainer = BachatDanger
)

private val DarkColorScheme = darkColorScheme(
  primary = BachatInkDark,
  onPrimary = BachatBackgroundDark,
  primaryContainer = BachatSurfaceDark,
  onPrimaryContainer = BachatTextPrimaryDark,
  secondary = BachatTextSecondaryDark,
  onSecondary = BachatBackgroundDark,
  background = BachatBackgroundDark,
  onBackground = BachatTextPrimaryDark,
  surface = BachatSurfaceDark,
  onSurface = BachatTextPrimaryDark,
  surfaceVariant = BachatSurfaceDark,
  onSurfaceVariant = BachatTextSecondaryDark,
  outline = BachatDividerDark,
  error = BachatDanger,
  onError = BachatOnColor,
  errorContainer = Color(0xFF450A0A),
  onErrorContainer = Color(0xFFFCA5A5)
)

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.LIGHT,
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = LightColorScheme
  val bachatColors = LightBachatColors

  androidx.compose.runtime.CompositionLocalProvider(LocalBachatColors provides bachatColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
