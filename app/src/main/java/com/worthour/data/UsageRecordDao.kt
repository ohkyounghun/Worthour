package com.worthour.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UsageRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageRecord(record: UsageRecordEntity): Long

    @Query("SELECT * FROM usage_records WHERE packageName = :packageName AND date = :date")
    suspend fun getUsageRecord(packageName: String, date: LocalDate): UsageRecordEntity?

    @Query("SELECT * FROM usage_records WHERE packageName = :packageName AND date >= :startDate AND date <= :endDate")
    fun getUsageRecords(packageName: String, startDate: LocalDate, endDate: LocalDate): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE packageName IN (:packageNames) AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getUsageRecordsForPackages(packageNames: List<String>, startDate: LocalDate, endDate: LocalDate): Flow<List<UsageRecordEntity>>

    @Query("SELECT SUM(usageTimeMillis) FROM usage_records WHERE packageName IN (:packageNames) AND date >= :startDate")
    suspend fun getTotalUsageTimeForPackages(packageNames: List<String>, startDate: LocalDate): Long?

    @Query("SELECT SUM(usageTimeMillis) FROM usage_records WHERE packageName IN (:packageNames) AND date >= :startDate")
    fun getTotalUsageTimeForPackagesFlow(packageNames: List<String>, startDate: LocalDate): Flow<Long?>

    @Query("SELECT COUNT(DISTINCT date) FROM usage_records WHERE date >= :startDate")
    suspend fun getRecordedDaysCount(startDate: LocalDate): Int

    @Query("SELECT COUNT(DISTINCT date) FROM usage_records WHERE date >= :startDate")
    fun getRecordedDaysCountFlow(startDate: LocalDate): Flow<Int>
}
