package com.crw.myteacher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MyTeacherColorScheme = lightColorScheme(
    primary = BrandBlue,
    secondary = LightBlue,
    tertiary = BrandBlue,
    background = ScreenBackground,
    surface = ScreenBackground,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun MyTeacherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MyTeacherColorScheme,
        typography = Typography,
        content = content
    )
}
