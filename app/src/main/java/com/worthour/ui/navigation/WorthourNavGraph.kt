package com.worthour.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worthour.ui.add.AddSubscriptionScreen
import com.worthour.ui.detail.SubscriptionDetailScreen
import com.worthour.ui.home.HomeScreen
import com.worthour.ui.insights.InsightsScreen
import com.worthour.ui.notification.NotificationScreen
import com.worthour.ui.permission.PermissionScreen

object Screen {
    const val HOME = "home"
    const val ADD_SUBSCRIPTION = "add_subscription"
    const val PERMISSION = "permission"
    const val DETAIL = "detail/{subscriptionId}"
    const val INSIGHTS = "insights"
    const val NOTIFICATIONS = "notifications"

    fun detailRoute(subscriptionId: Long) = "detail/$subscriptionId"
}

@Composable
fun WorthourNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.HOME) {
            HomeScreen(
                onNavigateToAdd = { navController.navigate(Screen.ADD_SUBSCRIPTION) },
                onNavigateToDetail = { id -> navController.navigate(Screen.detailRoute(id)) },
                onNavigateToInsights = { navController.navigate(Screen.INSIGHTS) },
                onNavigateToNotifications = { navController.navigate(Screen.NOTIFICATIONS) },
                onNavigateToPermission = { navController.navigate(Screen.PERMISSION) }
            )
        }

        composable(Screen.ADD_SUBSCRIPTION) {
            AddSubscriptionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PERMISSION) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.DETAIL,
            arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: 0L
            SubscriptionDetailScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { /* Navigate to edit screen later */ }
            )
        }

        composable(Screen.INSIGHTS) {
            InsightsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NOTIFICATIONS) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
