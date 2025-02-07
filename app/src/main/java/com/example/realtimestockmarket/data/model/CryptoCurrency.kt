package com.example.realtimestockmarket.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CryptoCurrency(
    val symbol: String,
    val price: String
) : Parcelable