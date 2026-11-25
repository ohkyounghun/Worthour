package com.worthour.usage

import android.content.Context
import androidx.core.content.edit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.worthour.data.AppDatabase
import com.worthour.data.ExchangeRateRepository
import com.worthour.data.UsageRecordEntity
import com.worthour.data.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object UsageSyncManager {

    private const val WORK_NAME_DAILY_SYNC = "worthour_daily_usage_sync"
    private const val PREFS_NAME = "worthour_usage_sync_prefs"
    private const val KEY_INITIAL_SYNC_DONE = "initial_usage_sync_done"

    /**
     * Schedules the daily WorkManager task to run once a day.
     */
    fun scheduleDailySync(context: Context) {
        val syncWorkRequest = PeriodicWorkRequestBuilder<UsageSyncWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_DAILY_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    /**
     * Syncs recent [days] (default 7 days) of usage data into usage_records DB via UsageRepository
     * and fetches the latest exchange rate.
     * Returns the total count of saved records.
     */
    suspend fun syncRecentDays(context: Context, days: Int = 7): Int {
        ExchangeRateRepository(context).fetchUsdToKrwRate()

        val permissionChecker = UsagePermissionChecker(context)
        if (!permissionChecker.hasUsageStatsPermission()) {
            return 0
        }

        return withContext(Dispatchers.IO) {
            val usageStatsReader = UsageStatsReader(context)
            val database = AppDatabase.getDatabase(context)
            val usageRepository = UsageRepository(database.usageRecordDao(), usageStatsReader)

            val today = LocalDate.now()
            var totalSavedCount = 0

            for (i in 1..days) {
                val date = today.minusDays(i.toLong())
                val dailyUsage = usageStatsReader.getUsageForDate(date)

                dailyUsage.forEach { (packageName, usageTimeMillis) ->
                    val existingRecord = usageRepository.getUsageRecord(packageName, date)
                    val record = UsageRecordEntity(
                        id = existingRecord?.id ?: 0,
                        packageName = packageName,
                        date = date,
                        usageTimeMillis = usageTimeMillis
                    )
                    usageRepository.insertUsageRecord(record)
                    totalSavedCount++
                }
            }
            totalSavedCount
        }
    }

    /**
     * Performs initial 7-day usage sync if it hasn't been done yet and permission is granted.
     */
    suspend fun syncInitialLast7DaysIfNeeded(context: Context) {
        val permissionChecker = UsagePermissionChecker(context)
        if (!permissionChecker.hasUsageStatsPermission()) {
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isInitialSyncDone = prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)

        if (!isInitialSyncDone) {
            syncRecentDays(context, days = 7)
            prefs.edit { putBoolean(KEY_INITIAL_SYNC_DONE, true) }
        }
    }

    /**
     * Manually triggers a sync right now for recent 7 days usage into usage_records DB via UsageRepository.
     * Useful for testing from UI.
     */
    suspend fun syncNow(context: Context): Int {
        return syncRecentDays(context, days = 7)
    }
}
