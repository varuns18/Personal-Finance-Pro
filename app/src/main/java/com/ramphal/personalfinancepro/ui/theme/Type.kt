package com.ramphal.personalfinancepro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ramphal.personalfinancepro.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

val myFont = FontFamily(
    Font(resId = R.font.pro_regular, weight = FontWeight.Normal),
    Font(resId = R.font.pro_bold, weight = FontWeight.Bold),
    Font(resId = R.font.pro_medium, weight = FontWeight.SemiBold)
)