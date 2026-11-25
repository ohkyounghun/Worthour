package com.worthour.data

import android.content.SharedPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ExchangeRateRepositoryTest {

    private class FakeSharedPreferences : SharedPreferences {
        private val map = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = (map[key] as? MutableSet<String>) ?: defValues
        override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = map.containsKey(key)
        override fun edit(): SharedPreferences.Editor = FakeEditor(map)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

        private class FakeEditor(private val map: MutableMap<String, Any?>) : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) map[key] = value
                return this
            }
            override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
                if (key != null) map[key] = values
                return this
            }
            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) map[key] = value
                return this
            }
            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) map[key] = value
                return this
            }
            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) map[key] = value
                return this
            }
            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) map[key] = value
                return this
            }
            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) map.remove(key)
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                map.clear()
                return this
            }
            override fun commit(): Boolean = true
            override fun apply() {}
        }
    }

    private class FakeFrankfurterService(
        private val response: FrankfurterResponse? = null,
        private val exceptionToThrow: Exception? = null
    ) : FrankfurterService {
        override suspend fun getUsdToKrwRate(): FrankfurterResponse {
            if (exceptionToThrow != null) {
                throw exceptionToThrow
            }
            return response ?: throw IllegalStateException("Response not configured")
        }
    }

    private lateinit var fakeSharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        fakeSharedPreferences = FakeSharedPreferences()
    }

    @Test
    fun fetchUsdToKrwRate_success_savesRateAndDate() = runBlocking {
        val fakeResponse = FrankfurterResponse(
            date = "2026-11-24",
            base = "USD",
            quote = "KRW",
            rate = 1343.4
        )
        val service = FakeFrankfurterService(response = fakeResponse)
        val repository = ExchangeRateRepository(fakeSharedPreferences, service)

        val success = repository.fetchUsdToKrwRate()

        assertTrue(success)
        assertEquals(1343.4, repository.getSavedUsdToKrwRate()!!, 0.001)
        assertEquals("2026-11-24", repository.getSavedDate())
    }

    @Test
    fun fetchUsdToKrwRate_networkFailure_returnsFalseAndPreservesSavedRate() = runBlocking {
        val service = FakeFrankfurterService(exceptionToThrow = IOException("Network error"))
        val repository = ExchangeRateRepository(fakeSharedPreferences, service)
        repository.saveExchangeRate(1300.0, "2026-11-20")

        val success = repository.fetchUsdToKrwRate()

        assertFalse(success)
        assertEquals(1300.0, repository.getSavedUsdToKrwRate()!!, 0.001)
        assertEquals("2026-11-20", repository.getSavedDate())
    }

    @Test(expected = CancellationException::class)
    fun fetchUsdToKrwRate_cancellationException_rethrowsException(): Unit = runBlocking {
        val service = FakeFrankfurterService(exceptionToThrow = CancellationException("Job cancelled"))
        val repository = ExchangeRateRepository(fakeSharedPreferences, service)

        repository.fetchUsdToKrwRate()
    }

    @Test
    fun getSavedUsdToKrwRate_returnsNullWhenNothingSaved() {
        val service = FakeFrankfurterService(response = null)
        val repository = ExchangeRateRepository(fakeSharedPreferences, service)

        assertNull(repository.getSavedUsdToKrwRate())
        assertNull(repository.getSavedDate())
    }
}
