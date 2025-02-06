package com.example.realtimestockmarket.data.dto

import com.squareup.moshi.Json

data class ExchangeInfoResponse(
    @Json(name = "symbols") val symbols: List<ExchangeSymbol>
)

data class ExchangeSymbol(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "baseAsset") val baseAsset: String
)

data class CryptoPrice(
    @Json(name = "symbol") val symbol: String,
    @Json(name = "price") val price: String
)