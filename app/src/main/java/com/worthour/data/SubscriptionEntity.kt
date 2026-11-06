package com.worthour.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Int,
    val nextPaymentDate: LocalDate,
    val freeTrialEndDate: LocalDate?,
    val cycle: PaymentCycle,
    val analysisType: AnalysisType
)
