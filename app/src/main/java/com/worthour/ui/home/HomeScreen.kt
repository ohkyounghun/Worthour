package com.worthour.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.worthour.R
import com.worthour.data.AnalysisType
import com.worthour.data.PaymentCycle
import com.worthour.data.SubscriptionEntity
import com.worthour.ui.navigation.Screen
import com.worthour.ui.theme.DDayBadgeBg
import com.worthour.ui.theme.DDayBadgeText
import com.worthour.ui.theme.WorthourPrimary
import com.worthour.ui.theme.WorthourSurface
import com.worthour.ui.theme.WorthourTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPermission: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToInsights = onNavigateToInsights,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToPermission = onNavigateToPermission,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonthYear = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.US)).uppercase(Locale.US)
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(onNavigateToNotifications = onNavigateToNotifications)
        },
        bottomBar = {
            WorthourBottomNavigationBar(
                currentScreen = Screen.HOME,
                onNavigateToHome = { /* Already on Home */ },
                onNavigateToInsights = onNavigateToInsights
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = WorthourPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subscription",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Monthly Spending Hero Card
            item {
                MonthlySpendingCard(
                    totalSpending = uiState.totalMonthlySpending,
                    activeCount = uiState.activeSubscriptionsCount,
                    currentMonthYear = currentMonthYear
                )
            }

            // 2. Upcoming Payment Banner Card
            uiState.upcomingPayment?.let { upcoming ->
                item {
                    UpcomingPaymentCard(
                        upcomingItem = upcoming,
                        onClick = { onNavigateToDetail(upcoming.subscription.id) }
                    )
                }
            }

            // 3. Permission Warning Banner if permission missing
            if (uiState.permissionWarning) {
                item {
                    PermissionWarningBanner(onNavigateToPermission = onNavigateToPermission)
                }
            }

            // 4. Subscriptions Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.home_my_subscriptions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(id = R.string.home_active_count, uiState.activeSubscriptionsCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. Subscriptions List Items
            if (uiState.subscriptions.isEmpty()) {
                item {
                    EmptySubscriptionCard(onNavigateToAdd = onNavigateToAdd)
                }
            } else {
                items(
                    items = uiState.subscriptions,
                    key = { it.entity.id }
                ) { item ->
                    SubscriptionItemCard(
                        item = item,
                        onClick = { onNavigateToDetail(item.entity.id) }
                    )
                }
            }

            // 6. Footer Caption Text
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.home_footer_caption),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(WorthourPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToNotifications) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
    )
}

@Composable
fun MonthlySpendingCard(
    totalSpending: Int,
    activeCount: Int,
    currentMonthYear: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = WorthourPrimary,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.home_monthly_spending),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = currentMonthYear,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatPrice(totalSpending),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = stringResource(id = R.string.home_active_subscriptions, activeCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun UpcomingPaymentCard(
    upcomingItem: UpcomingPaymentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WorthourSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DDayBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = WorthourPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${stringResource(id = R.string.home_upcoming_payment)} · ${formatDateShort(upcomingItem.subscription.nextPaymentDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${upcomingItem.subscription.name} · ${formatPrice(upcomingItem.subscription.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DDayBadgeBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = upcomingItem.dDayText,
                    color = DDayBadgeText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun SubscriptionItemCard(
    item: HomeSubscriptionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WorthourSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionServiceIcon(name = item.entity.name)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.entity.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        id = R.string.home_next_bill,
                        formatDate(item.entity.nextPaymentDate)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        id = R.string.home_per_month,
                        String.format(Locale.US, "%,d", item.entity.price)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                ValueScoreBadge(score = item.valueScore)
            }
        }
    }
}

@Composable
fun SubscriptionServiceIcon(
    name: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val logoResId = remember(name, isPreview) {
        if (isPreview) null else SubscriptionLogoMapper.getLogoResId(context, name)
    }

    if (logoResId != null) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = name,
            modifier = modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    } else {
        val normalized = name.lowercase()
        val (bgColor, textColor, label) = when {
            normalized.contains("netflix") -> Triple(Color(0xFF141414), Color(0xFFE50914), "N")
            normalized.contains("youtube") -> Triple(Color(0xFFFFEBEB), Color(0xFFFF0000), "▶")
            normalized.contains("spotify") -> Triple(Color(0xFF1DB954), Color(0xFFFFFFFF), "S")
            normalized.contains("disney") -> Triple(Color(0xFF113CCF), Color(0xFFFFFFFF), "D+")
            else -> Triple(WorthourPrimary.copy(alpha = 0.12f), WorthourPrimary, name.take(1).uppercase())
        }

        Box(
            modifier = modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (label.length > 1) 13.sp else 18.sp
            )
        }
    }
}

@Composable
fun ValueScoreBadge(
    score: ValueScore,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(score.backgroundColor)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = score.label,
            color = score.textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PermissionWarningBanner(
    onNavigateToPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onNavigateToPermission,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(id = R.string.home_permission_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EmptySubscriptionCard(
    onNavigateToAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onNavigateToAdd,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WorthourSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아직 등록된 구독이 없습니다.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "+ 버튼을 눌러 새 구독을 추가해 보세요!",
                style = MaterialTheme.typography.bodySmall,
                color = WorthourPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WorthourBottomNavigationBar(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = WorthourSurface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(id = R.string.nav_home)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WorthourPrimary,
                selectedTextColor = WorthourPrimary,
                indicatorColor = WorthourPrimary.copy(alpha = 0.12f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        NavigationBarItem(
            selected = currentScreen == Screen.INSIGHTS,
            onClick = onNavigateToInsights,
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text(stringResource(id = R.string.nav_insights)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WorthourPrimary,
                selectedTextColor = WorthourPrimary,
                indicatorColor = WorthourPrimary.copy(alpha = 0.12f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

private fun formatPrice(price: Int): String {
    return String.format(Locale.US, "₩%,d", price)
}

private fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return date.format(formatter)
}

private fun formatDateShort(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    return date.format(formatter)
}

// ==========================================
// Previews demonstrating all ValueScore states
// ==========================================

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewAllBadges() {
    WorthourTheme {
        val sampleSubs = listOf(
            HomeSubscriptionItem(
                entity = SubscriptionEntity(
                    id = 1,
                    name = "Netflix",
                    price = 17000,
                    nextPaymentDate = LocalDate.of(2026, 10, 11),
                    freeTrialEndDate = null,
                    cycle = PaymentCycle.MONTHLY,
                    analysisType = AnalysisType.TIME_TRACKING
                ),
                valueScore = ValueScore.LOW
            ),
            HomeSubscriptionItem(
                entity = SubscriptionEntity(
                    id = 2,
                    name = "YouTube Premium",
                    price = 14900,
                    nextPaymentDate = LocalDate.of(2026, 10, 15),
                    freeTrialEndDate = null,
                    cycle = PaymentCycle.MONTHLY,
                    analysisType = AnalysisType.TIME_TRACKING
                ),
                valueScore = ValueScore.GOOD
            ),
            HomeSubscriptionItem(
                entity = SubscriptionEntity(
                    id = 3,
                    name = "Spotify",
                    price = 10900,
                    nextPaymentDate = LocalDate.of(2026, 10, 20),
                    freeTrialEndDate = null,
                    cycle = PaymentCycle.MONTHLY,
                    analysisType = AnalysisType.TIME_TRACKING
                ),
                valueScore = ValueScore.GOOD
            ),
            HomeSubscriptionItem(
                entity = SubscriptionEntity(
                    id = 4,
                    name = "Disney+",
                    price = 6100,
                    nextPaymentDate = LocalDate.of(2026, 10, 25),
                    freeTrialEndDate = null,
                    cycle = PaymentCycle.MONTHLY,
                    analysisType = AnalysisType.TIME_TRACKING
                ),
                valueScore = ValueScore.OK
            ),
            HomeSubscriptionItem(
                entity = SubscriptionEntity(
                    id = 5,
                    name = "ChatGPT Plus",
                    price = 29000,
                    nextPaymentDate = LocalDate.of(2026, 11, 1),
                    freeTrialEndDate = null,
                    cycle = PaymentCycle.MONTHLY,
                    analysisType = AnalysisType.TIME_TRACKING
                ),
                valueScore = ValueScore.MEASURING
            )
        )

        val sampleUiState = HomeUiState(
            totalMonthlySpending = 48900,
            activeSubscriptionsCount = 4,
            upcomingPayment = UpcomingPaymentItem(
                subscription = sampleSubs[0].entity,
                dDayDays = 3
            ),
            subscriptions = sampleSubs,
            permissionWarning = false
        )

        HomeScreenContent(
            uiState = sampleUiState,
            onNavigateToAdd = {},
            onNavigateToDetail = {},
            onNavigateToInsights = {},
            onNavigateToNotifications = {},
            onNavigateToPermission = {}
        )
    }
}
