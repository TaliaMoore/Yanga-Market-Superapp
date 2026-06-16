package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BrightNeonPurple,
    secondary = PaleNeonYellow,
    tertiary = BoldGold,
    background = CosmicPurpleBg,
    surface = CosmicCardPurple,
    onPrimary = CosmicPurpleBg,
    onSecondary = CosmicPurpleBg,
    onTertiary = CosmicPurpleBg,
    onBackground = CosmicTextWhite,
    onSurface = CosmicTextWhite,
    outline = BrightNeonPurple
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    secondary = SecondaryYellow,
    tertiary = BoldGold,
    background = PlayfulBg,
    surface = WarmCardWhite,
    onPrimary = Color.White,
    onSecondary = CharcoalBlack,
    onTertiary = Color.White,
    onBackground = CharcoalBlack,
    onSurface = CharcoalBlack,
    surfaceVariant = PlayfulCream,
    onSurfaceVariant = CharcoalBlack,
    outline = PrimaryPurple
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Permanently force light theme as requested!
    dynamicColor: Boolean = false, // Disable to always enforce our custom brand aesthetic
    content: @Composable () -> Unit,
) {
    // Lock color scheme to LightColorScheme permanently to satisfy user intent and avoid transition
    val colors = LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
