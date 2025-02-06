package com.example.realtimestockmarket.data
import com.example.realtimestockmarket.data.dto.CryptoPrice
import com.example.realtimestockmarket.data.dto.ExchangeInfoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApiService {
    @GET("")
    suspend fun getExchangeInfo(): ExchangeInfoResponse

    @GET("")
    suspend fun getPrices(): List<CryptoPrice>
}