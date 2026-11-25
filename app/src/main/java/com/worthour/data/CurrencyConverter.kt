package com.worthour.data

import java.util.Locale

object CurrencyConverter {

    /**
     * Converts USD cents or KRW won into monthly KRW price.
     * Returns null if subscription is USD and usdToKrwRate is null.
     */
    fun toMonthlyKrw(
        price: Int,
        currency: Currency,
        cycle: PaymentCycle,
        usdToKrwRate: Double?
    ): Double? {
        val basePriceDouble = if (currency == Currency.USD) {
            val rate = usdToKrwRate ?: return null
            val dollars = price / 100.0
            dollars * rate
        } else {
            price.toDouble()
        }

        return if (cycle == PaymentCycle.YEARLY) {
            basePriceDouble / 12.0
        } else {
            basePriceDouble
        }
    }

    /**
     * Formats subscription price based on currency.
     * USD (price in cents): $9.99
     * KRW (price in Won): ₩10,000
     */
    fun formatPrice(price: Int, currency: Currency): String {
        return if (currency == Currency.USD) {
            val dollars = price / 100.0
            String.format(Locale.US, "$%.2f", dollars)
        } else {
            String.format(Locale.US, "₩%,d", price)
        }
    }

    /**
     * Formats subscription price with cycle suffix (/mo, /yr).
     */
    fun formatPriceWithCycle(price: Int, currency: Currency, cycle: PaymentCycle): String {
        val formatted = formatPrice(price, currency)
        val suffix = if (cycle == PaymentCycle.YEARLY) "/yr" else "/mo"
        return "$formatted$suffix"
    }
}
