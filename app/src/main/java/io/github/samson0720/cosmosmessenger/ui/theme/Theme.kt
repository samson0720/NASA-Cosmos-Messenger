package io.github.samson0720.cosmosmessenger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CosmosViolet,
    onPrimary = Color.White,
    primaryContainer = CosmosVioletLight,
    onPrimaryContainer = CosmosVioletDim,
    secondary = CosmosCyanDark,
    background = CosmosSurface,
    surface = CosmosSurface,
    surfaceVariant = CosmosCard,
    onSurface = CosmosOnSurface,
    onSurfaceVariant = CosmosOnSurface,
    outline = CosmosSubtext,
)

private val DarkColors = darkColorScheme(
    primary = CosmosViolet,
    onPrimary = Color.White,
    primaryContainer = CosmosVioletDim,
    onPrimaryContainer = CosmosVioletLight,
    secondary = CosmosCyan,
    background = CosmosNight,
    surface = CosmosNightSurface,
    surfaceVariant = CosmosNightCard,
    onSurfaceVariant = CosmosVioletLight,
    outline = CosmosSubtext,
)

@Composable
fun CosmosMessengerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
