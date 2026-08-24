package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Baloo 2 style rounded geometric typography
val DisplayRupeeStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.ExtraBold,
  fontSize = 44.sp,
  lineHeight = 48.sp,
  color = BachatTextPrimary
)

val ScreenTitleStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.Bold,
  fontSize = 24.sp,
  lineHeight = 28.sp,
  color = BachatTextPrimary
)

val SectionHeaderStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.Bold,
  fontSize = 20.sp,
  lineHeight = 24.sp,
  color = BachatTextPrimary
)

val CardTitleStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.SemiBold,
  fontSize = 18.sp,
  lineHeight = 22.sp,
  color = BachatTextPrimary
)

val ListAmountStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.Bold,
  fontSize = 17.sp,
  lineHeight = 20.sp
)

val EyebrowStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.SemiBold,
  fontSize = 11.5.sp,
  lineHeight = 14.sp,
  letterSpacing = 0.6.sp,
  color = BachatTextSecondary
)

val CaptionStyle = TextStyle(
  fontFamily = FontFamily.SansSerif,
  fontWeight = FontWeight.Medium,
  fontSize = 13.5.sp,
  lineHeight = 17.sp,
  color = BachatTextSecondary
)

val Typography = Typography(
  displayLarge = DisplayRupeeStyle,
  headlineLarge = ScreenTitleStyle,
  headlineMedium = SectionHeaderStyle,
  titleLarge = CardTitleStyle,
  titleMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 20.sp,
    color = BachatTextPrimary
  ),
  bodyMedium = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    color = BachatTextSecondary
  ),
  labelSmall = EyebrowStyle
)
