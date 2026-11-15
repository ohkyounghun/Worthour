package com.worthour.usage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UsageSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val permissionChecker = UsagePermissionChecker(applicationContext)
        if (!permissionChecker.hasUsageStatsPermission()) {
            return Result.success()
        }

        UsageSyncManager.syncRecentDays(applicationContext, days = 7)
        return Result.success()
    }
}
