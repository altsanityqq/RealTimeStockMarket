package com.example.realtimestockmarket.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.realtimestockmarket.R
import com.example.realtimestockmarket.data.model.CryptoCurrency
import com.example.realtimestockmarket.data.repository.BinanceRepository
import com.example.realtimestockmarket.ui.components.ErrorDialog
import com.example.realtimestockmarket.ui.components.ProgressDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeScreenUiState(
    val isLoading: Boolean = false,
    val cryptoList: List<CryptoCurrency> = emptyList(),
    val error: Exception? = null
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val repository: BinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        //loadFakeCryptoList()
        loadCryptoList()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredCryptoList(): List<CryptoCurrency> {
        val query = searchQuery.value.lowercase()
        return uiState.value.cryptoList.filter {
            it.symbol.lowercase().contains(query)
        }
    }

    fun loadCryptoList() {
        _uiState.update { HomeScreenUiState(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            try {
                val cryptoList = repository.fetchCryptoList()
                _uiState.update {
                    HomeScreenUiState(cryptoList = cryptoList)
                }
            } catch (e: Exception) {
                _uiState.update {
                    HomeScreenUiState(error = e)
                }
            }
        }
    }

    private fun loadFakeCryptoList() {
        val list = List(50) {
            CryptoCurrency(symbol = "Test $it", price = "10$it")
        }
        _uiState.update {
            HomeScreenUiState(cryptoList = list)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onItemClick: (CryptoCurrency) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updatedTime by rememberSaveable { mutableStateOf(getCurrentTime()) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredList = viewModel.getFilteredCryptoList()

    if (uiState.isLoading) {
        ProgressDialog()
    }

    if (uiState.error != null) {
        ErrorDialog(
            onDismiss = {},
            onRetry = {
                viewModel.loadCryptoList()
            }
        )
    }

    HomeScreenContent(
        cryptoCurrencyList = filteredList,
        searchQuery = searchQuery,
        updatedTime = updatedTime,
        onItemClick = { onItemClick(it) },
        onSearchQueryChanged = {
            viewModel.updateSearchQuery(it)
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeScreenContent(
    cryptoCurrencyList: List<CryptoCurrency>,
    searchQuery: String,
    updatedTime: String,
    onItemClick: (CryptoCurrency) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { onSearchQueryChanged(it) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            label = { Text(stringResource(R.string.search)) }
        )

        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
            Text(
                text = stringResource(R.string.updated_on),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = updatedTime,
                style = MaterialTheme.typography.titleLarge
            )
        }
        LazyColumn {
            items(cryptoCurrencyList) {
                HomeScreenItem(
                    onItemClick = { onItemClick(it) },
                    cryptoCurrency = it
                )
            }
        }
    }
}

@Composable
private fun HomeScreenItem(
    onItemClick: () -> Unit,
    cryptoCurrency: CryptoCurrency, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onItemClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cryptoCurrency.symbol,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$${cryptoCurrency.price}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentTime(): String {
    val currentTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return currentTime.format(formatter)
}