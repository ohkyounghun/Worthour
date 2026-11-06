package com.worthour.data

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromPaymentCycle(value: String?): PaymentCycle? {
        return value?.let { enumValueOf<PaymentCycle>(it) }
    }

    @TypeConverter
    fun paymentCycleToValue(cycle: PaymentCycle?): String? {
        return cycle?.name
    }

    @TypeConverter
    fun fromAnalysisType(value: String?): AnalysisType? {
        return value?.let { enumValueOf<AnalysisType>(it) }
    }

    @TypeConverter
    fun analysisTypeToValue(type: AnalysisType?): String? {
        return type?.name
    }
}
