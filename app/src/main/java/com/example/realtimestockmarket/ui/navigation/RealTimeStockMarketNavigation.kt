package com.example.realtimestockmarket.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.example.realtimestockmarket.R

sealed class RealTimeStockMarketScreen(@StringRes val title: Int, val route: String) {
    data object HomeScreen : RealTimeStockMarketScreen(R.string.home, "home_screen")
    data object DetailScreen : RealTimeStockMarketScreen(R.string.detail, "detail_screen")
}

class RealTimeStockMarketNavigationActions(navController: NavController) {
    val navigateBack: () -> Unit = {
        navController.navigateUp()
    }

    val navigateToDetail: () -> Unit = {
        navController.navigate(route = RealTimeStockMarketScreen.DetailScreen.route) {
            navWithStackOption()
        }
    }

    private fun NavOptionsBuilder.navWithStackOption() {
        launchSingleTop = true
    }
}