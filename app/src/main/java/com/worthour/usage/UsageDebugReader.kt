package com.worthour.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import android.util.Log
import java.util.Calendar

class UsageDebugReader(private val context: Context) {

    @Suppress("DEPRECATION")
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun printUsageStats() {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val beginTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        val queryStartTime = beginTime - 86400000L // 1 day before midnight

        // Method A: queryEvents
        val events = usageStatsManager.queryEvents(queryStartTime, endTime)
        
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
        
        // Still open now
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
            eventUsageMap[pkg] = totalTime
        }

        // Method B: queryAndAggregateUsageStats
        val aggregatedStats = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime)
        val aggregatedUsageMap = aggregatedStats.mapValues { it.value.totalTimeInForeground }

        // Combine and Log
        val allPackages = eventUsageMap.keys + aggregatedUsageMap.keys
        
        val results = allPackages.map { pkg ->
            val eventTime = eventUsageMap[pkg] ?: 0L
            val aggTime = aggregatedUsageMap[pkg] ?: 0L
            Triple(pkg, eventTime, aggTime)
        }.sortedByDescending { it.second }

        Log.d("USAGE", "--- Usage Stats (Midnight to Now) ---")
        results.forEach { (pkg, eventTime, aggTime) ->
            if (eventTime > 0 || aggTime > 0) {
                Log.d("USAGE", "Pkg: $pkg | queryEvents: ${formatTime(eventTime)} | aggregated: ${formatTime(aggTime)}")
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }
}
