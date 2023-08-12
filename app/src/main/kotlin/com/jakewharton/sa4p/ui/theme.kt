package com.jakewharton.sa4p.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Theme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	val colors = if (darkTheme) {
		DarkColorPalette
	} else {
		LightColorPalette
	}
	MaterialTheme(
		colors = colors,
		typography = Typography,
		shapes = Shapes,
		content = content,
	)
}

val Purple200 = Color(0xFFBB86FC)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

val Typography = Typography(
	h3 = TextStyle(
		fontFamily = FontFamily.Default,
		fontWeight = FontWeight.Normal,
		fontSize = 36.sp,
	),
)

private val DarkColorPalette = darkColors(
	primary = Purple200,
	primaryVariant = Purple700,
	secondary = Teal200,
)

private val LightColorPalette = lightColors(
	primary = Purple200,
	primaryVariant = Purple700,
	secondary = Teal200,
)

private val Shapes = Shapes(
	small = RoundedCornerShape(4.dp),
	medium = RoundedCornerShape(4.dp),
	large = RoundedCornerShape(0.dp),
)
