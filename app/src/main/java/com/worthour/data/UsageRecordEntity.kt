package com.worthour.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "usage_records",
    indices = [Index(value = ["packageName", "date"], unique = true)]
)
data class UsageRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val date: LocalDate,
    val usageTimeMillis: Long
)
