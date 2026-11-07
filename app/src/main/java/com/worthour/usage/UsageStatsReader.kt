package com.worthour.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.LocalDate
import java.time.ZoneId

class UsageStatsReader(private val context: Context) {

    private val permissionChecker = UsagePermissionChecker(context)

    /**
     * Calculates usage time per package for a specific date [date].
     * Returns a map of PackageName -> UsageTimeMillis.
     * Returns an empty map if permission is not granted or an error occurs.
     */
    fun getUsageForDate(date: LocalDate): Map<String, Long> {
        if (!permissionChecker.hasUsageStatsPermission()) {
            return emptyMap()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val zoneId = ZoneId.systemDefault()
        val beginTime = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val isToday = date == LocalDate.now()
        val endTime = if (isToday) {
            System.currentTimeMillis()
        } else {
            date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        }

        val queryStartTime = beginTime - 86400000L // 1 day before midnight

        val events = try {
            usageStatsManager.queryEvents(queryStartTime, endTime)
        } catch (e: Exception) {
            return emptyMap()
        } ?: return emptyMap()

        val packageClassResumedMap = mutableMapOf<String, MutableMap<String, Long>>()
        val packageIntervals = mutableMapOf<String, MutableList<Pair<Long, Long>>>()

        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: ""
            val className = event.className ?: ""
            val timestamp = event.timeStamp

            if (event.eventType == UsageEvents.Event.DEVICE_SHUTDOWN ||
                event.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) {
                for ((pkg, rMap) in packageClassResumedMap) {
                    val intervals = packageIntervals.getOrPut(pkg) { mutableListOf() }
                    for ((_, rTime) in rMap) {
                        intervals.add(rTime to timestamp)
                    }
                    rMap.clear()
                }
            } else if (packageName.isNotEmpty()) {
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val rMap = packageClassResumedMap.getOrPut(packageName) { mutableMapOf() }

                    val prevTime = rMap[className]
                    if (prevTime != null) {
                        val intervals = packageIntervals.getOrPut(packageName) { mutableListOf() }
                        intervals.add(prevTime to timestamp)
                    }
                    rMap[className] = timestamp
                } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED ||
                    event.eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                    val rMap = packageClassResumedMap.getOrPut(packageName) { mutableMapOf() }
                    val intervals = packageIntervals.getOrPut(packageName) { mutableListOf() }

                    val rTime = rMap.remove(className)
                    if (rTime != null) {
                        intervals.add(rTime to timestamp)
                    }
                }
            }
        }

        // Still open at endTime
        for ((pkg, rMap) in packageClassResumedMap) {
            val intervals = packageIntervals.getOrPut(pkg) { mutableListOf() }
            for ((_, rTime) in rMap) {
                intervals.add(rTime to endTime)
            }
        }

        // Merge intervals and calculate total time per package
        val eventUsageMap = mutableMapOf<String, Long>()
        for ((pkg, rawIntervals) in packageIntervals) {
            if (rawIntervals.isEmpty()) continue

            // Clamp intervals to [beginTime, endTime]
            val clampedIntervals = rawIntervals.mapNotNull { (start, end) ->
                val cStart = maxOf(start, beginTime)
                val cEnd = minOf(end, endTime)
                if (cStart < cEnd) cStart to cEnd else null
            }.toMutableList()

            if (clampedIntervals.isEmpty()) continue

            clampedIntervals.sortBy { it.first }
            var totalTime = 0L
            var currentStart = clampedIntervals[0].first
            var currentEnd = clampedIntervals[0].second

            for (i in 1 until clampedIntervals.size) {
                val (start, end) = clampedIntervals[i]
                if (start <= currentEnd) {
                    if (end > currentEnd) {
                        currentEnd = end
                    }
                } else {
                    totalTime += (currentEnd - currentStart)
                    currentStart = start
                    currentEnd = end
                }
            }
            totalTime += (currentEnd - currentStart)
            if (totalTime > 0) {
                eventUsageMap[pkg] = totalTime
            }
        }

        return eventUsageMap
    }

    /**
     * Get usage per app for each of the last [days] (default 7 days, including today).
     * Returns a map of Date -> (PackageName -> UsageTimeMillis).
     */
    fun getDailyUsageForLastDays(days: Int = 7): Map<LocalDate, Map<String, Long>> {
        if (!permissionChecker.hasUsageStatsPermission()) {
            return emptyMap()
        }
        val today = LocalDate.now()
        val result = mutableMapOf<LocalDate, Map<String, Long>>()
        for (i in (days - 1) downTo 0) {
            val date = today.minusDays(i.toLong())
            result[date] = getUsageForDate(date)
        }
        return result
    }

    /**
     * Calculates total usage time for a subscription by summing the usage of its linked apps
     * over a given date range [startDate]..[endDate].
     */
    fun getSubscriptionUsageTime(
        packageNames: List<String>,
        startDate: LocalDate,
        endDate: LocalDate
    ): Long {
        if (packageNames.isEmpty() || !permissionChecker.hasUsageStatsPermission()) {
            return 0L
        }
        var totalMillis = 0L
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val dailyUsage = getUsageForDate(currentDate)
            for (pkg in packageNames) {
                totalMillis += (dailyUsage[pkg] ?: 0L)
            }
            currentDate = currentDate.plusDays(1)
        }
        return totalMillis
    }
}
