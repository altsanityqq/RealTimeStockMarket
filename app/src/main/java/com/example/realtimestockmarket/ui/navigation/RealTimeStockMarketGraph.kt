package com.example.realtimestockmarket.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.realtimestockmarket.ui.screens.DetailScreen
import com.example.realtimestockmarket.ui.screens.HomeScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RealTimeStockMarketNavGraph(
    navController: NavHostController,
    navigationActions: RealTimeStockMarketNavigationActions,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = RealTimeStockMarketScreen.HomeScreen.route,
        modifier = modifier
    ) {
        composable(route = RealTimeStockMarketScreen.HomeScreen.route) {
            HomeScreen(onItemClick = {
                navigationActions.navigateToDetail
            })
        }
        composable(route = RealTimeStockMarketScreen.DetailScreen.route) {
            DetailScreen(onBackClick = {
                navigationActions.navigateBack
            })
        }
    }
}