package com.example.realtimestockmarket.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.realtimestockmarket.R
import com.example.realtimestockmarket.data.model.CryptoCurrency
import com.example.realtimestockmarket.data.repository.BinanceRepository
import com.example.realtimestockmarket.ui.components.ErrorDialog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailScreenUiState(
    val cryptoCurrency: CryptoCurrency = CryptoCurrency("btc", ""),
    val prices: List<Pair<Float, Float>> = emptyList(),
    val error: Throwable? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val repository: BinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var xValue = 0f

    init {
        connectToWebSocket()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectToWebSocket() {
        viewModelScope.launch {
            repository.connectWebSocket(
                "btcusdt", // TODO HANDLE
                onOpen = {},
                onUpdate = { price ->
                    updatePrice(price.toFloat())
                },
                onFailure = { error ->
                    _uiState.update {
                        DetailScreenUiState(error = error)
                    }
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePrice(newPrice: Float) {
        viewModelScope.launch {
            val updatedList = _uiState.value.prices.toMutableList().apply {
                add(Pair(xValue, newPrice))
                xValue += 1f

                val minTime = xValue - 60 * 5
                removeAll { it.first < minTime }
            }
            _uiState.emit(
                DetailScreenUiState(
                    cryptoCurrency = CryptoCurrency("btc", price = newPrice.toString()),
                    prices = updatedList
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.closeWebSocket()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailScreen(
    viewModel: DetailScreenViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.error != null) {
        ErrorDialog(
            onDismiss = { onBackClick() },
            onRetry = { viewModel.connectToWebSocket() },
        )
    }

    DetailScreenContent(
        cryptoCurrency = uiState.cryptoCurrency,
        prices = uiState.prices,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun DetailScreenContent(
    cryptoCurrency: CryptoCurrency,
    prices: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "${cryptoCurrency.symbol} ${stringResource(R.string.price)}",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text(
                text = "${cryptoCurrency.symbol} to USD: 1 ${cryptoCurrency.symbol} " +
                        "${stringResource(R.string.equals)} " +
                        "$${cryptoCurrency.price} USD",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LineChartView(priceList = prices)
    }
}

@Composable
private fun LineChartView(priceList: List<Pair<Float, Float>>) {
    val context = LocalContext.current
    val chart = remember { LineChart(context) }

    val entries = priceList.map { (time, price) ->
        Entry(time, price)
    }

    LaunchedEffect(priceList) {
        val dataSet = LineDataSet(entries, "Price Data")
        dataSet.color = ColorTemplate.MATERIAL_COLORS[0]
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
    }

    AndroidView(
        factory = { chart },
        modifier = Modifier.fillMaxSize()
    ) {
        it.invalidate()
    }
}