package com.worthour.data

import java.util.Locale

object CurrencyConverter {

    /**
     * Converts price in USD cents or KRW won into monthly price in target currency (KRW or USD).
     * Returns null if conversion to/from USD is needed but usdToKrwRate is null.
     */
    fun toMonthlyPrice(
        price: Int,
        fromCurrency: Currency,
        targetCurrency: Currency,
        cycle: PaymentCycle,
        usdToKrwRate: Double?
    ): Double? {
        val baseMonthlyDouble = if (fromCurrency == Currency.USD) {
            val dollars = price / 100.0
            if (cycle == PaymentCycle.YEARLY) dollars / 12.0 else dollars
        } else {
            val won = price.toDouble()
            if (cycle == PaymentCycle.YEARLY) won / 12.0 else won
        }

        if (fromCurrency == targetCurrency) {
            return baseMonthlyDouble
        }

        val rate = usdToKrwRate ?: return null

        return if (fromCurrency == Currency.USD && targetCurrency == Currency.KRW) {
            baseMonthlyDouble * rate
        } else if (fromCurrency == Currency.KRW && targetCurrency == Currency.USD) {
            baseMonthlyDouble / rate
        } else {
            baseMonthlyDouble
        }
    }

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
        return toMonthlyPrice(
            price = price,
            fromCurrency = currency,
            targetCurrency = Currency.KRW,
            cycle = cycle,
            usdToKrwRate = usdToKrwRate
        )
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
