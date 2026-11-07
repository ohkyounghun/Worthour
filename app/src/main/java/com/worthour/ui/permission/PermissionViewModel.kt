package com.worthour.ui.permission

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.worthour.usage.UsagePermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionViewModel(application: Application) : AndroidViewModel(application) {
    private val permissionChecker = UsagePermissionChecker(application)

    private val _isGranted = MutableStateFlow(permissionChecker.hasUsageStatsPermission())
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    fun checkPermission() {
        _isGranted.value = permissionChecker.hasUsageStatsPermission()
    }
}
