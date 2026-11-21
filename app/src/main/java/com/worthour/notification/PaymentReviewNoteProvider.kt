package com.worthour.notification

import android.content.Context
import com.worthour.data.SubscriptionEntity

interface PaymentReviewNoteProvider {
    /**
     * Extension point for Issue #14 to attach a review note for low-score subscriptions.
     * Returns null if no review note is needed.
     */
    suspend fun getReviewNote(context: Context, subscription: SubscriptionEntity): String?
}

class DefaultPaymentReviewNoteProvider : PaymentReviewNoteProvider {
    override suspend fun getReviewNote(context: Context, subscription: SubscriptionEntity): String? {
        return null
    }
}
