package com.example.realtimestockmarket.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.realtimestockmarket.ui.components.ProgressDialog
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
    val isLoading: Boolean = false,
    val price: String = "",
    val prices: List<Pair<Float, Float>> = emptyList(),
    val error: Throwable? = null,
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DetailScreenViewModel @Inject constructor(
    private val repository: BinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailScreenUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _updateInterval = MutableStateFlow(1000L)

    fun setUpdateInterval(interval: Long) {
        _updateInterval.value = interval
        repository.updateInterval(interval)
    }

    private var xValue = 0f

    @RequiresApi(Build.VERSION_CODES.O)
    fun connectToWebSocket(cryptoSymbol: String) {
        viewModelScope.launch {
            repository.connectWebSocket(
                cryptoSymbol,
                onOpen = {},
                onUpdate = { price ->
                    updatePrice(price.toFloat())
                },
                onFailure = { error ->
                    _uiState.update { DetailScreenUiState(error = error) }
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
                removeAll { it.first < xValue - 60 * 5 }
            }
            _uiState.emit(
                DetailScreenUiState(
                    isLoading = false,
                    price = newPrice.toString(),
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
    cryptoCurrency: CryptoCurrency,
    viewModel: DetailScreenViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    viewModel.connectToWebSocket(cryptoCurrency.symbol)

    if (uiState.isLoading) {
        ProgressDialog(
            onDismiss = onBackClick
        )
    }

    if (uiState.error != null) {
        ErrorDialog(
            onDismiss = { onBackClick() },
            onRetry = { viewModel.connectToWebSocket(cryptoCurrency.symbol) },
        )
    }

    DetailScreenContent(
        cryptoCurrency = cryptoCurrency,
        price = uiState.price,
        prices = uiState.prices,
        onIntervalSelected = { viewModel.setUpdateInterval(it) },
        onDismiss = {
            onBackClick()
        },
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun DetailScreenContent(
    cryptoCurrency: CryptoCurrency,
    price: String,
    prices: List<Pair<Float, Float>>,
    onIntervalSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = "${cryptoCurrency.symbol} ${stringResource(R.string.price)}",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text(
                text = "${cryptoCurrency.symbol} to USD: 1 ${cryptoCurrency.symbol} " +
                        "${stringResource(R.string.equals)} $$price USD",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LineChartView(priceList = prices)
        Spacer(modifier = Modifier.height(10.dp))
        IntervalSelectionDropdown(onIntervalSelected, onDismiss = onDismiss)
    }
}

@Composable
private fun IntervalSelectionDropdown(onIntervalSelected: (Long) -> Unit, onDismiss: () -> Unit) {
    val intervals = listOf(1000L, 2000L, 5000L)
    var selectedInterval by remember { mutableStateOf(intervals.first()) }

    DropdownMenu(expanded = true, onDismissRequest = onDismiss) {
        intervals.forEach { interval ->
            DropdownMenuItem(
                text = { Text("${interval / 1000} sec") },
                onClick = {
                    selectedInterval = interval
                    onIntervalSelected(interval)
                }
            )
        }
    }
}

@Composable
private fun LineChartView(priceList: List<Pair<Float, Float>>) {
    val context = LocalContext.current
    val chart = remember { LineChart(context) }
    val entries = priceList.map { (time, price) -> Entry(time, price) }
    LaunchedEffect(priceList) {
        val dataSet = LineDataSet(entries, "Price Data").apply {
            color = ColorTemplate.MATERIAL_COLORS[0]
            setDrawValues(false)
        }
        chart.data = LineData(dataSet)
        chart.invalidate()
    }
    AndroidView(factory = { chart }, modifier = Modifier.fillMaxSize()) { it.invalidate() }
}