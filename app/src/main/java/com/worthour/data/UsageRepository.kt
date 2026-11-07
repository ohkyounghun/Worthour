package com.worthour.data

import com.worthour.usage.UsageStatsReader
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class UsageRepository(
    private val usageRecordDao: UsageRecordDao,
    private val usageStatsReader: UsageStatsReader
) {

    suspend fun insertUsageRecord(record: UsageRecordEntity): Long {
        return usageRecordDao.insertUsageRecord(record)
    }

    suspend fun getUsageRecord(packageName: String, date: LocalDate): UsageRecordEntity? {
        return usageRecordDao.getUsageRecord(packageName, date)
    }

    fun getUsageRecords(packageName: String, startDate: LocalDate, endDate: LocalDate): Flow<List<UsageRecordEntity>> {
        return usageRecordDao.getUsageRecords(packageName, startDate, endDate)
    }

    fun getUsageForDate(date: LocalDate): Map<String, Long> {
        return usageStatsReader.getUsageForDate(date)
    }

    fun getDailyUsageForLastDays(days: Int = 7): Map<LocalDate, Map<String, Long>> {
        return usageStatsReader.getDailyUsageForLastDays(days)
    }

    fun getSubscriptionUsageTime(
        packageNames: List<String>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Long {
        return usageStatsReader.getSubscriptionUsageTime(packageNames, startDate, endDate)
    }
}
