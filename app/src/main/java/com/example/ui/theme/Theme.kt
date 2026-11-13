package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AccentPalette
import com.example.data.model.InterfaceDensity
import com.example.data.model.ThemeModePreference
import com.example.data.model.UserAppPreferences

val LocalInterfaceSpacing = staticCompositionLocalOf { 16.dp }

private val AppShapes = Shapes(
  small = RoundedCornerShape(10.dp),
  medium = RoundedCornerShape(18.dp),
  large = RoundedCornerShape(24.dp),
  extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun IStudentTheme(
  preferences: UserAppPreferences = UserAppPreferences(),
  content: @Composable () -> Unit,
) {
  val isDark = when (preferences.themeMode) {
    ThemeModePreference.SYSTEM -> isSystemInDarkTheme()
    ThemeModePreference.DARK -> true
    ThemeModePreference.LIGHT -> false
  }
  val primaryAccent = Color(preferences.accentPalette.hex)
  val secondaryAccent = when (preferences.accentPalette) {
    AccentPalette.ROYAL_PURPLE -> BrandDeepViolet
    AccentPalette.DEEP_VIOLET -> BrandRoyalPurple
    AccentPalette.MIDNIGHT_NAVY -> BrandCyan
    AccentPalette.EMERALD_SCHOLAR -> BrandRoyalPurple
    AccentPalette.AMBER_SPARK -> BrandDeepViolet
  }

  val colorScheme = if (isDark) {
    darkColorScheme(
      primary = if (preferences.highContrast) Color(0xFFC4B5FD) else Color(0xFFA78BFA),
      onPrimary = Color(0xFF140736),
      primaryContainer = primaryAccent.copy(alpha = 0.28f),
      onPrimaryContainer = Color(0xFFEDE9FE),
      secondary = secondaryAccent,
      onSecondary = BrandPureWhite,
      secondaryContainer = BrandNavyCard,
      onSecondaryContainer = BrandSoftLavender,
      tertiary = BrandScholarGold,
      onTertiary = Color(0xFF1F1300),
      background = if (preferences.highContrast) Color(0xFF070A14) else BrandDarkNavy,
      onBackground = BrandPureWhite,
      surface = if (preferences.highContrast) Color(0xFF0D1324) else BrandNavySurface,
      onSurface = BrandPureWhite,
      surfaceVariant = BrandNavyCard,
      onSurfaceVariant = if (preferences.highContrast) Color(0xFFE2E8F0) else BrandMutedTextDark,
      outline = Color(0xFF334155),
      error = BrandCoral,
    )
  } else {
    lightColorScheme(
      primary = primaryAccent,
      onPrimary = BrandPureWhite,
      primaryContainer = BrandSoftLavender,
      onPrimaryContainer = BrandDeepViolet,
      secondary = secondaryAccent,
      onSecondary = BrandPureWhite,
      secondaryContainer = Color(0xFFF3F0FF),
      onSecondaryContainer = BrandDarkNavy,
      tertiary = BrandScholarGold,
      onTertiary = BrandDarkNavy,
      background = if (preferences.highContrast) BrandPureWhite else BrandOffWhite,
      onBackground = BrandDarkNavy,
      surface = BrandPureWhite,
      onSurface = BrandDarkNavy,
      surfaceVariant = Color(0xFFF1EEFF),
      onSurfaceVariant = if (preferences.highContrast) BrandDarkNavy else BrandMutedTextLight,
      outline = BrandLavenderBorder,
      error = BrandCoral,
    )
  }

  val cardSpacing: Dp = when (preferences.density) {
    InterfaceDensity.COMFORTABLE -> 16.dp
    InterfaceDensity.COMPACT -> 10.dp
  }

  CompositionLocalProvider(LocalInterfaceSpacing provides cardSpacing) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = buildAppTypography(preferences.fontScale),
      shapes = AppShapes,
      content = content,
    )
  }
}
