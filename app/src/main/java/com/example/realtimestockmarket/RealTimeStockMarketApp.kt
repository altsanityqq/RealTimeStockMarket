package com.example.realtimestockmarket

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.realtimestockmarket.ui.navigation.RealTimeStockMarketNavGraph
import com.example.realtimestockmarket.ui.navigation.RealTimeStockMarketNavigationActions

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RealTimeStockMarketApp(
    navController: NavHostController = rememberNavController()
) {
    val navigationActions = remember(navController) {
        RealTimeStockMarketNavigationActions(navController)
    }
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            RealTimeStockMarketNavGraph(
                navController = navController,
                navigationActions = navigationActions,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}