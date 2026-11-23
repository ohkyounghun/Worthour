package com.worthour.data

import java.time.LocalDate

data class ShareableSubscription(
    val name: String,
    val price: Int,
    val nextPaymentDate: LocalDate,
    val freeTrialEndDate: LocalDate? = null,
    val cycle: PaymentCycle,
    val analysisType: AnalysisType,
    val linkedAppPackageNames: List<String> = emptyList()
) {
    fun toSubscriptionEntity(id: Long = 0): SubscriptionEntity {
        return SubscriptionEntity(
            id = id,
            name = name,
            price = price,
            nextPaymentDate = nextPaymentDate,
            freeTrialEndDate = freeTrialEndDate,
            cycle = cycle,
            analysisType = analysisType
        )
    }

    companion object {
        fun fromEntity(
            subscription: SubscriptionEntity,
            linkedAppPackageNames: List<String> = emptyList()
        ): ShareableSubscription {
            return ShareableSubscription(
                name = subscription.name,
                price = subscription.price,
                nextPaymentDate = subscription.nextPaymentDate,
                freeTrialEndDate = subscription.freeTrialEndDate,
                cycle = subscription.cycle,
                analysisType = subscription.analysisType,
                linkedAppPackageNames = linkedAppPackageNames
            )
        }
    }
}
