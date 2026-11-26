package com.limonatalp4.glasscalcproject.data.repository

import android.content.Context
import androidx.compose.runtime.Immutable
import com.limonatalp4.glasscalcproject.data.preferences.GlassCalcPreferences
import com.limonatalp4.glasscalcproject.data.remote.FrankfurterApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@Immutable
data class CurrencyRatesUiState(
    val rates: Map<String, Double> = emptyMap(),
    val base: String = "EUR",
    val date: String = "",
    val fetchedAtMillis: Long = 0L,
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val isOfflineFallback: Boolean = false,
    val hasError: Boolean = false
) {
    val hasRates: Boolean get() = rates.isNotEmpty()
}

class CurrencyRepository(
    context: Context,
    private val preferences: GlassCalcPreferences,
    apiServiceOverride: FrankfurterApiService? = null
) {
    private val appContext = context.applicationContext ?: context
    private val refreshMutex = Mutex()

    private val okHttpClient: OkHttpClient by lazy {
        val cacheDir = File(appContext.cacheDir, HTTP_CACHE_DIR)
        OkHttpClient.Builder()
            .cache(Cache(cacheDir, HTTP_CACHE_SIZE_BYTES))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=3600")
                    .build()
            }
            .build()
    }

    private val apiService: FrankfurterApiService by lazy {
        apiServiceOverride ?: run {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()
            Retrofit.Builder()
                .baseUrl(FrankfurterApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(FrankfurterApiService::class.java)
        }
    }

    private val _ratesState = MutableStateFlow(CurrencyRatesUiState())
    val ratesState: StateFlow<CurrencyRatesUiState> = _ratesState.asStateFlow()

    suspend fun loadRatesIfNeeded(forceRefresh: Boolean = false) {
        refreshMutex.withLock {
            val now = System.currentTimeMillis()
            val cached = runCatching { preferences.cachedCurrencyRatesFlow.first() }.getOrNull()

            if (!forceRefresh && _ratesState.value.hasRates && (now - _ratesState.value.fetchedAtMillis) < CACHE_TTL_MS) {
                return
            }

            if (cached != null && cached.rates.isNotEmpty()) {
                val normalizedCached = buildNormalizedRatesMap(cached.base, cached.rates)
                val cacheFresh = (now - cached.fetchedAtMillis) < CACHE_TTL_MS
                if (!forceRefresh && cacheFresh) {
                    _ratesState.value = CurrencyRatesUiState(
                        rates = normalizedCached,
                        base = cached.base,
                        date = cached.date,
                        fetchedAtMillis = cached.fetchedAtMillis,
                        isLoading = false,
                        isFromCache = true,
                        isOfflineFallback = false,
                        hasError = false
                    )
                    return
                }
                _ratesState.value = CurrencyRatesUiState(
                    rates = normalizedCached,
                    base = cached.base,
                    date = cached.date,
                    fetchedAtMillis = cached.fetchedAtMillis,
                    isLoading = true,
                    isFromCache = true,
                    isOfflineFallback = false,
                    hasError = false
                )
            } else {
                _ratesState.value = _ratesState.value.copy(
                    isLoading = true,
                    hasError = false,
                    isOfflineFallback = false
                )
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    if (forceRefresh) {
                        okHttpClient.cache?.evictAll()
                    }
                    apiService.getLatestRates(base = DEFAULT_BASE)
                }
                val normalized = buildNormalizedRatesMap(response.base, response.rates)
                val fetchedAt = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    preferences.saveCachedCurrencyRates(
                        base = response.base,
                        date = response.date,
                        rates = normalized,
                        fetchedAtMillis = fetchedAt
                    )
                }
                _ratesState.value = CurrencyRatesUiState(
                    rates = normalized,
                    base = response.base,
                    date = response.date,
                    fetchedAtMillis = fetchedAt,
                    isLoading = false,
                    isFromCache = false,
                    isOfflineFallback = false,
                    hasError = false
                )
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Throwable) {
                val fallback = _ratesState.value
                if (fallback.hasRates) {
                    _ratesState.value = fallback.copy(
                        isLoading = false,
                        isFromCache = true,
                        isOfflineFallback = true,
                        hasError = false
                    )
                } else {
                    _ratesState.value = CurrencyRatesUiState(
                        rates = emptyMap(),
                        base = DEFAULT_BASE,
                        date = "",
                        fetchedAtMillis = 0L,
                        isLoading = false,
                        isFromCache = false,
                        isOfflineFallback = false,
                        hasError = true
                    )
                }
            }
        }
    }

    fun clearInMemoryState() {
        _ratesState.value = CurrencyRatesUiState()
    }

    companion object {
        const val DEFAULT_BASE = "EUR"
        const val CACHE_TTL_MS = 60L * 60L * 1000L
        private const val HTTP_CACHE_DIR = "currency_http_cache"
        private const val HTTP_CACHE_SIZE_BYTES = 5L * 1024L * 1024L

        fun buildNormalizedRatesMap(base: String, rawRates: Map<String, Double>): Map<String, Double> {
            val map = LinkedHashMap<String, Double>(rawRates.size + 1)
            map[base.uppercase()] = 1.0
            rawRates.forEach { (code, value) ->
                if (value.isFinite() && value > 0.0) {
                    map[code.uppercase()] = value
                }
            }
            return map
        }

        fun convertCurrency(
            amount: Double,
            fromCurrency: String,
            toCurrency: String,
            rates: Map<String, Double>
        ): Double? {
            if (!amount.isFinite() || rates.isEmpty()) return null
            val fromCode = fromCurrency.uppercase()
            val toCode = toCurrency.uppercase()
            if (fromCode == toCode) return amount
            val fromRate = rates[fromCode] ?: return null
            val toRate = rates[toCode] ?: return null
            if (!fromRate.isFinite() || !toRate.isFinite() || fromRate <= 0.0 || toRate <= 0.0) return null
            val converted = amount * (toRate / fromRate)
            return if (converted.isFinite()) converted else null
        }

        fun formatExchangeRate(
            fromCurrency: String,
            toCurrency: String,
            rates: Map<String, Double>
        ): Double? = convertCurrency(1.0, fromCurrency, toCurrency, rates)
    }
}
