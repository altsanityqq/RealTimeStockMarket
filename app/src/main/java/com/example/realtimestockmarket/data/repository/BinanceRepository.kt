package com.example.realtimestockmarket.data.repository

import com.example.realtimestockmarket.data.model.CryptoCurrency
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinanceRepository @Inject constructor(
    private val client: OkHttpClient
) {
    private var lastUpdateTime = 0L
    private val updateInterval = 500L
    private var webSocket: WebSocket? = null

    fun fetchCryptoList(): List<CryptoCurrency> {
        val request = Request.Builder()
            .url("https://api.binance.com/api/v3/ticker/price")
            .build()
        val response = client.newCall(request).execute()
        val jsonData = response.body?.string() ?: "[]"

        val jsonArray = JSONArray(jsonData)
        val tempList = mutableListOf<CryptoCurrency>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val symbol = item.getString("symbol")
            val price = item.getString("price")
            tempList.add(CryptoCurrency(symbol, price))
        }

        return tempList.toList()
    }

    fun connectWebSocket(
        cryptoSymbol: String,
        onOpen: () -> Unit,
        onUpdate: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val url = "wss://stream.binance.com:9443/ws/${cryptoSymbol.lowercase()}@trade"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onOpen()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val jsonObject = JSONObject(text)
                val price = jsonObject.getString("p")

                val currentTime = System.currentTimeMillis()

                if (currentTime - lastUpdateTime >= updateInterval) {
                    onUpdate(removeTrailingZeros(price))

                    lastUpdateTime = currentTime
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onFailure(t)
            }
        })
    }

    fun closeWebSocket() {
        webSocket?.close(1000, "App closed")
    }

    private fun removeTrailingZeros(value: String): String {
        return value.toDouble().toString()
    }
}