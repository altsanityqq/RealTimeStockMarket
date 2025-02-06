package com.example.realtimestockmarket.data.repository

import android.util.Log
import com.example.realtimestockmarket.data.BinanceApiService
import com.example.realtimestockmarket.data.model.CryptoCurrency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinanceRepository @Inject constructor(
    private val apiService: BinanceApiService,
    private val client: OkHttpClient
) {
    private val _cryptoList = MutableStateFlow<List<CryptoCurrency>>(emptyList())
    val cryptoList: StateFlow<List<CryptoCurrency>> = _cryptoList

    private val _livePrice = MutableStateFlow("Loading...")
    val livePrice: StateFlow<String> = _livePrice

    private var webSocket: WebSocket? = null

    suspend fun fetchCryptoList() {
        try {
            val exchangeInfo = apiService.getExchangeInfo()
            val prices = apiService.getPrices()

            val symbolMap = exchangeInfo.symbols.associate { it.symbol to it.baseAsset }
            val tempList = prices.filter { it.symbol.endsWith("USDT") }
                .map {
                    CryptoCurrency(
                        symbol = it.symbol,
                        name = symbolMap[it.symbol] ?: it.symbol,
                        price = it.price
                    )
                }
            _cryptoList.value = tempList
        } catch (e: Exception) {
            Log.e("BinanceRepository", "Error: ${e.message}")
        }
    }

    fun connectWebSocket(cryptoSymbol: String) {
        val url = ""
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val jsonObject = JSONObject(text)
                val price = jsonObject.getString("p")
                _livePrice.value = price
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }
        })
    }

    fun closeWebSocket() {
        webSocket?.close(1000, "App closed")
    }
}
