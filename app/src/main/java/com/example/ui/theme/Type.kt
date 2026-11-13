package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SpaceGroteskFamily = FontFamily(
  Font(R.font.space_grotesk, FontWeight.Normal),
  Font(R.font.space_grotesk, FontWeight.Medium),
  Font(R.font.space_grotesk, FontWeight.Bold),
)

val PlusJakartaSansFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
  Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

fun buildAppTypography(scale: Float = 1.0f): Typography = Typography(
  displaySmall = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = (32f * scale).sp,
    lineHeight = (38f * scale).sp,
    letterSpacing = (-0.5).sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.Bold,
    fontSize = (24f * scale).sp,
    lineHeight = (30f * scale).sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = (20f * scale).sp,
    lineHeight = (26f * scale).sp,
  ),
  titleLarge = TextStyle(
    fontFamily = SpaceGroteskFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = (18f * scale).sp,
    lineHeight = (24f * scale).sp,
  ),
  titleMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = (16f * scale).sp,
    lineHeight = (22f * scale).sp,
  ),
  titleSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = (14f * scale).sp,
    lineHeight = (20f * scale).sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = (15f * scale).sp,
    lineHeight = (22f * scale).sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = (13.5f * scale).sp,
    lineHeight = (19f * scale).sp,
  ),
  bodySmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Normal,
    fontSize = (12f * scale).sp,
    lineHeight = (16f * scale).sp,
  ),
  labelLarge = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = (13.5f * scale).sp,
    lineHeight = (18f * scale).sp,
  ),
  labelMedium = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = (11.5f * scale).sp,
    lineHeight = (15f * scale).sp,
  ),
  labelSmall = TextStyle(
    fontFamily = PlusJakartaSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = (10.5f * scale).sp,
    lineHeight = (14f * scale).sp,
  ),
)

val Typography = buildAppTypography(1.0f)
