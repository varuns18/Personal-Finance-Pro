package com.ramphal.personalfinancepro.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)