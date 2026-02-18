package com.thumbwar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ThumbWarColorScheme = darkColorScheme(
    primary = MenuAccent,
    secondary = RingGold,
    tertiary = ArenaGreenLight,
    background = MenuBackground,
    surface = MenuSurface,
    onPrimary = MenuText,
    onSecondary = MenuBackground,
    onTertiary = MenuText,
    onBackground = MenuText,
    onSurface = MenuText
)

@Composable
fun ThumbWarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ThumbWarColorScheme,
        typography = Typography,
        content = content
    )
}
