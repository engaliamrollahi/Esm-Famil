package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = SoftTealDark,
    onPrimary = PersianTealDark,
    primaryContainer = PersianTeal,
    onPrimaryContainer = PersianTealContainer,
    secondary = SoftSaffronDark,
    onSecondary = OnSaffronContainer,
    secondaryContainer = Color(0xFF5A3300),
    onSecondaryContainer = SaffronContainer,
    tertiary = SoftRoseDark,
    onTertiary = OnTerracottaContainer,
    tertiaryContainer = Color(0xFF6B1D28),
    onTertiaryContainer = TerracottaContainer,
    background = DeepInkBg,
    onBackground = CreamTextDark,
    surface = DeepInkSurface,
    onSurface = CreamTextDark,
    surfaceVariant = DeepInkSurfaceVariant,
    onSurfaceVariant = MutedTextDark,
    outline = OutlineDark,
    error = SoftRoseDark,
    onError = OnTerracottaContainer,
)

private val LightColorScheme = lightColorScheme(
    primary = PersianTeal,
    onPrimary = Color.White,
    primaryContainer = PersianTealContainer,
    onPrimaryContainer = OnPersianTealContainer,
    secondary = SaffronGold,
    onSecondary = Color.White,
    secondaryContainer = SaffronContainer,
    onSecondaryContainer = OnSaffronContainer,
    tertiary = TerracottaRose,
    onTertiary = Color.White,
    tertiaryContainer = TerracottaContainer,
    onTertiaryContainer = OnTerracottaContainer,
    background = CreamPaper,
    onBackground = InkDark,
    surface = CreamSurface,
    onSurface = InkDark,
    surfaceVariant = CreamSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outline = OutlineWarm,
    error = ScoreZeroRed,
    onError = Color.White,
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun EsmFamilTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
