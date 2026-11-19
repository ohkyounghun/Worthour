package com.worthour.data

import com.worthour.usage.UsageStatsReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class UsageRepository(
    private val usageRecordDao: UsageRecordDao,
    private val usageStatsReader: UsageStatsReader? = null
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
        return usageStatsReader?.getUsageForDate(date) ?: emptyMap()
    }

    fun getDailyUsageForLastDays(days: Int = 7): Map<LocalDate, Map<String, Long>> {
        return usageStatsReader?.getDailyUsageForLastDays(days) ?: emptyMap()
    }

    fun getSubscriptionUsageTime(
        packageNames: List<String>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Long {
        return usageStatsReader?.getSubscriptionUsageTime(packageNames, startDate, endDate) ?: 0L
    }

    suspend fun getTotalUsageTimeForPackages(packageNames: List<String>, startDate: LocalDate): Long {
        if (packageNames.isEmpty()) return 0L
        return usageRecordDao.getTotalUsageTimeForPackages(packageNames, startDate) ?: 0L
    }

    fun getTotalUsageTimeForPackagesFlow(packageNames: List<String>, startDate: LocalDate): Flow<Long> {
        if (packageNames.isEmpty()) return flowOf(0L)
        return usageRecordDao.getTotalUsageTimeForPackagesFlow(packageNames, startDate).map { it ?: 0L }
    }

    suspend fun getRecordedDaysCount(startDate: LocalDate): Int {
        return usageRecordDao.getRecordedDaysCount(startDate)
    }

    fun getRecordedDaysCountFlow(startDate: LocalDate): Flow<Int> {
        return usageRecordDao.getRecordedDaysCountFlow(startDate)
    }
}
