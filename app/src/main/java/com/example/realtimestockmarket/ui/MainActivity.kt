package com.example.realtimestockmarket.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.realtimestockmarket.RealTimeStockMarketApp
import com.example.realtimestockmarket.ui.theme.RealTimeStockMarketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RealTimeStockMarketTheme {
                RealTimeStockMarketApp()
            }
        }
    }
}