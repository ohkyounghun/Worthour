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
}
