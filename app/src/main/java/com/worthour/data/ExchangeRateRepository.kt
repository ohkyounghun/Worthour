package com.worthour.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ExchangeRateRepository(
    private val sharedPreferences: SharedPreferences,
    private val frankfurterService: FrankfurterService = RetrofitClient.frankfurterService
) {
    constructor(
        context: Context,
        frankfurterService: FrankfurterService = RetrofitClient.frankfurterService
    ) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        frankfurterService
    )

    suspend fun fetchUsdToKrwRate(): Boolean {
        return try {
            val response = frankfurterService.getUsdToKrwRate()
            saveExchangeRate(response.rate, response.date)
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            false
        }
    }

    fun getSavedUsdToKrwRate(): Double? {
        val rateStr = sharedPreferences.getString(KEY_RATE, null) ?: return null
        return rateStr.toDoubleOrNull()
    }

    fun getSavedUsdToKrwRateFlow(): Flow<Double?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_RATE || key == null) {
                trySend(getSavedUsdToKrwRate())
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSavedUsdToKrwRate())

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun getSavedDate(): String? {
        return sharedPreferences.getString(KEY_DATE, null)
    }

    fun saveExchangeRate(rate: Double, date: String) {
        sharedPreferences.edit {
            putString(KEY_RATE, rate.toString())
            putString(KEY_DATE, date)
        }
    }

    companion object {
        const val PREFS_NAME = "worthour_exchange_rate_prefs"
        const val KEY_RATE = "usd_to_krw_rate"
        const val KEY_DATE = "usd_to_krw_date"
    }
}
