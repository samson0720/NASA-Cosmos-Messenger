package io.github.samson0720.cosmosmessenger.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.samson0720.cosmosmessenger.R

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Nova(route = "nova", labelRes = R.string.tab_nova, icon = Icons.Cosmos.Saturn),
    Favorites(route = "favorites", labelRes = R.string.tab_favorites, icon = Icons.Filled.Star);
}
