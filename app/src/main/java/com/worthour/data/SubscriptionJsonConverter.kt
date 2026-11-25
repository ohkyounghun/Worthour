package com.worthour.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object SubscriptionJsonConverter {

    private const val KEY_NAME = "name"
    private const val KEY_PRICE = "price"
    private const val KEY_NEXT_PAYMENT_DATE = "nextPaymentDate"
    private const val KEY_FREE_TRIAL_END_DATE = "freeTrialEndDate"
    private const val KEY_CYCLE = "cycle"
    private const val KEY_ANALYSIS_TYPE = "analysisType"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_LINKED_APPS = "linkedAppPackageNames"

    /**
     * Converts a list of ShareableSubscription objects to a JSON string.
     */
    fun toJson(subscriptions: List<ShareableSubscription>): String {
        val jsonArray = JSONArray()
        for (sub in subscriptions) {
            jsonArray.put(subscriptionToJsonObject(sub))
        }
        return jsonArray.toString()
    }

    /**
     * Converts a single ShareableSubscription object to a JSON string.
     */
    fun toJson(subscription: ShareableSubscription): String {
        return subscriptionToJsonObject(subscription).toString()
    }

    /**
     * Parses a JSON string representing a list of subscriptions back into a list of ShareableSubscription objects.
     */
    fun listFromJson(jsonString: String): List<ShareableSubscription> {
        if (jsonString.isBlank()) return emptyList()
        val list = mutableListOf<ShareableSubscription>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            list.add(jsonObjectToSubscription(jsonObject))
        }
        return list
    }

    /**
     * Parses a JSON string representing a single subscription back into a ShareableSubscription object.
     */
    fun fromJson(jsonString: String): ShareableSubscription {
        val jsonObject = JSONObject(jsonString)
        return jsonObjectToSubscription(jsonObject)
    }

    private fun subscriptionToJsonObject(sub: ShareableSubscription): JSONObject {
        return JSONObject().apply {
            put(KEY_NAME, sub.name)
            put(KEY_PRICE, sub.price)
            put(KEY_NEXT_PAYMENT_DATE, sub.nextPaymentDate.toString())
            put(KEY_FREE_TRIAL_END_DATE, sub.freeTrialEndDate?.toString() ?: JSONObject.NULL)
            put(KEY_CYCLE, sub.cycle.name)
            put(KEY_ANALYSIS_TYPE, sub.analysisType.name)
            put(KEY_CURRENCY, sub.currency.name)

            val appsArray = JSONArray()
            sub.linkedAppPackageNames.forEach { appsArray.put(it) }
            put(KEY_LINKED_APPS, appsArray)
        }
    }

    private fun jsonObjectToSubscription(jsonObject: JSONObject): ShareableSubscription {
        val name = jsonObject.getString(KEY_NAME)
        val price = jsonObject.getInt(KEY_PRICE)
        val nextPaymentDate = LocalDate.parse(jsonObject.getString(KEY_NEXT_PAYMENT_DATE))

        val trialDateStr = if (jsonObject.has(KEY_FREE_TRIAL_END_DATE) && !jsonObject.isNull(KEY_FREE_TRIAL_END_DATE)) {
            jsonObject.getString(KEY_FREE_TRIAL_END_DATE)
        } else null

        val freeTrialEndDate = if (!trialDateStr.isNullOrBlank() && trialDateStr != "null") {
            LocalDate.parse(trialDateStr)
        } else null

        val cycle = PaymentCycle.valueOf(jsonObject.getString(KEY_CYCLE))
        val analysisType = AnalysisType.valueOf(jsonObject.getString(KEY_ANALYSIS_TYPE))

        val currencyStr = if (jsonObject.has(KEY_CURRENCY)) jsonObject.getString(KEY_CURRENCY) else "KRW"
        val currency = try { Currency.valueOf(currencyStr) } catch (_: Exception) { Currency.KRW }

        val linkedApps = mutableListOf<String>()
        if (jsonObject.has(KEY_LINKED_APPS)) {
            val appsArray = jsonObject.getJSONArray(KEY_LINKED_APPS)
            for (j in 0 until appsArray.length()) {
                linkedApps.add(appsArray.getString(j))
            }
        }

        return ShareableSubscription(
            name = name,
            price = price,
            nextPaymentDate = nextPaymentDate,
            freeTrialEndDate = freeTrialEndDate,
            cycle = cycle,
            analysisType = analysisType,
            currency = currency,
            linkedAppPackageNames = linkedApps
        )
    }
}
