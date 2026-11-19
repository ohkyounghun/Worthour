package com.worthour.data

import com.worthour.ui.home.ValueScore

data class SubscriptionValueItem(
    val subscription: SubscriptionEntity,
    val valueResult: ValueCalculationResult
) {
    val costPerHour: Double? get() = valueResult.costPerHour
    val valueScore: ValueScore get() = valueResult.valueScore
}
