package com.worthour.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class UsageRepository(private val usageRecordDao: UsageRecordDao) {

    suspend fun insertUsageRecord(record: UsageRecordEntity): Long {
        return usageRecordDao.insertUsageRecord(record)
    }

    suspend fun getUsageRecord(packageName: String, date: LocalDate): UsageRecordEntity? {
        return usageRecordDao.getUsageRecord(packageName, date)
    }

    fun getUsageRecords(packageName: String, startDate: LocalDate, endDate: LocalDate): Flow<List<UsageRecordEntity>> {
        return usageRecordDao.getUsageRecords(packageName, startDate, endDate)
    }
}
