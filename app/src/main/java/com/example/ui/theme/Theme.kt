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
  darkColorScheme(
    primary = SleekIndigo500,
    secondary = SleekViolet500,
    tertiary = SleekEmerald500,
    background = SleekDarkBg,
    surface = SleekDarkSurface,
    surfaceVariant = SleekDarkSurfaceVariant,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = SleekDarkOnBg,
    onSurface = SleekDarkOnSurface,
    onSurfaceVariant = SleekDarkOnSurfaceVariant,
    outline = SleekDarkOutline,
    primaryContainer = SleekIndigo600,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SleekDarkSurfaceVariant,
    onSecondaryContainer = SleekDarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekIndigo600,
    secondary = SleekViolet600,
    tertiary = SleekEmerald600,
    background = SleekLightBg,
    surface = SleekLightSurface,
    surfaceVariant = SleekLightSurfaceVariant,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = SleekLightOnBg,
    onSurface = SleekLightOnSurface,
    onSurfaceVariant = SleekLightOnSurfaceVariant,
    outline = SleekLightOutline,
    primaryContainer = SleekIndigo500,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SleekLightSurfaceVariant,
    onSecondaryContainer = SleekLightOnSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to force our premium brand style
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
