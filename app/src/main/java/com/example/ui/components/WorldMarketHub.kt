package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketCatalog
import com.example.model.MarketSessionInfo
import com.example.model.MarketSymbol
import com.example.model.WorldMacroEvent
import com.example.model.WorldMarketStats
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCard
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary
import kotlin.math.abs

/**
 * High-visibility World Market Synchronization strip displayed above the chart.
 * Shows live world status, latency, UTC timestamp, and key statistical badges.
 */
@Composable
fun WorldMarketLiveSyncStrip(
    worldStats: WorldMarketStats?,
    symbol: MarketSymbol,
    isSyncing: Boolean,
    onManualSync: () -> Unit,
    onOpenWorldHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spinRotation"
    )

    Surface(
        color = ArtisticCard,
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, ArtisticBorder)
            .clickable { onOpenWorldHub() }
            .testTag("world_market_sync_strip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live status & World session
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (worldStats?.isLiveSynced == true) ArtisticGreen.copy(alpha = pulseAlpha)
                            else ArtisticPrimary
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isSyncing) "SYNCING WORLD MARKET..." else "WORLD MARKET SYNC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (worldStats?.isLiveSynced == true) ArtisticGreen else ArtisticPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "• ${worldStats?.syncLatencyMs ?: 38}ms",
                            fontSize = 9.sp,
                            color = ArtisticTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = worldStats?.lastSyncTimeFormatted ?: "LIVE UTC",
                            fontSize = 9.sp,
                            color = ArtisticTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Key statistical metrics row
                    worldStats?.let { stats ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "24h Range: ${String.format("%.1f", stats.dailyPipRange)}p",
                                fontSize = 10.sp,
                                color = ArtisticTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text("•", fontSize = 9.sp, color = ArtisticTextMuted)
                            Text(
                                text = "Sentiment: ${stats.bullishSentimentPercent}% Bull",
                                fontSize = 10.sp,
                                color = if (stats.bullishSentimentPercent >= 50) ArtisticGreen else ArtisticRed,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("•", fontSize = 9.sp, color = ArtisticTextMuted)
                            Text(
                                text = "Vol: $${formatVolume(stats.volume24hUsd)}",
                                fontSize = 10.sp,
                                color = ArtisticTextMuted
                            )
                        }
                    }
                }
            }

            // Sync trigger and Open Hub button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onManualSync,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("manual_world_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync World Market",
                        tint = ArtisticPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (isSyncing) Modifier.rotate(spinRotation) else Modifier)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ArtisticPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "WORLD STATS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticPrimary
                    )
                }
            }
        }
    }
}

/**
 * Comprehensive World Market Hub Modal Bottom Sheet.
 * Displays Statistical Data of Pairs, Active World Sessions, Macro Information, and Multi-Pair Matrix.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMarketHubBottomSheet(
    symbol: MarketSymbol,
    worldStats: WorldMarketStats?,
    marketSessions: List<MarketSessionInfo>,
    macroEvents: List<WorldMacroEvent>,
    allPairsStats: Map<String, WorldMarketStats>,
    isSyncing: Boolean,
    onManualSync: () -> Unit,
    onSelectSymbol: (MarketSymbol) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Pair Stats", "World Sessions", "Macro Intel", "All Pairs")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArtisticBg,
        dragHandle = null,
        modifier = Modifier.testTag("world_market_hub_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = ArtisticPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "World Market Hub & Intelligence",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticTextPrimary
                        )
                        Text(
                            text = "Live statistical telemetry • ${symbol.name}",
                            fontSize = 11.sp,
                            color = ArtisticTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onManualSync,
                        modifier = Modifier.size(32.dp).testTag("hub_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ArtisticPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("hub_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArtisticTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ArtisticCard,
                contentColor = ArtisticPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ArtisticPrimary,
                        height = 2.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) ArtisticPrimary else ArtisticTextMuted
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> PairStatsTab(symbol, worldStats)
                    1 -> WorldSessionsTab(marketSessions)
                    2 -> MacroIntelTab(symbol, macroEvents)
                    3 -> AllPairsMatrixTab(allPairsStats, onSelectSymbol, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun PairStatsTab(symbol: MarketSymbol, worldStats: WorldMarketStats?) {
    if (worldStats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connecting to global market liquidity feeds...", color = ArtisticTextMuted, fontSize = 12.sp)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Price Range Visualizer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, ArtisticBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("24h World Range", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                        Text(
                            text = "${String.format("%.1f", worldStats.dailyPipRange)} Pips Total",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val priceRatio = if (worldStats.high24h > worldStats.low24h) {
                        ((worldStats.price - worldStats.low24h) / (worldStats.high24h - worldStats.low24h)).toFloat().coerceIn(0.05f, 0.95f)
                    } else 0.5f

                    LinearProgressIndicator(
                        progress = { priceRatio },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = ArtisticPrimary,
                        trackColor = ArtisticSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("24h Low", fontSize = 9.sp, color = ArtisticTextMuted)
                            Text(
                                "%.${symbol.digits}f".format(worldStats.low24h),
                                fontSize = 11.sp,
                                color = ArtisticRed,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current World Price", fontSize = 9.sp, color = ArtisticTextMuted)
                            Text(
                                "%.${symbol.digits}f".format(worldStats.price),
                                fontSize = 12.sp,
                                color = ArtisticTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("24h High", fontSize = 9.sp, color = ArtisticTextMuted)
                            Text(
                                "%.${symbol.digits}f".format(worldStats.high24h),
                                fontSize = 11.sp,
                                color = ArtisticGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item {
            // Global Trader Sentiment Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, ArtisticBorder, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Global Retail & Institutional Positioning", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                        Text(
                            text = if (worldStats.bullishSentimentPercent >= 50) "Bullish Bias" else "Bearish Bias",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (worldStats.bullishSentimentPercent >= 50) ArtisticGreen else ArtisticRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
                        Box(
                            modifier = Modifier
                                .weight(worldStats.bullishSentimentPercent.toFloat().coerceAtLeast(1f))
                                .fillMaxSize()
                                .background(ArtisticGreen)
                        )
                        Box(
                            modifier = Modifier
                                .weight(worldStats.bearishSentimentPercent.toFloat().coerceAtLeast(1f))
                                .fillMaxSize()
                                .background(ArtisticRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "▲ ${worldStats.bullishSentimentPercent}% Long (Buyers)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticGreen
                        )
                        Text(
                            "▼ ${worldStats.bearishSentimentPercent}% Short (Sellers)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticRed
                        )
                    }
                }
            }
        }

        item {
            // Statistical Metrics Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricCard(
                    title = "24h Volume (Est)",
                    value = "$${formatVolume(worldStats.volume24hUsd)}",
                    subtitle = worldStats.liquidityRating,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "ATR (14-Period)",
                    value = "${String.format("%.1f", worldStats.atr14Pips)} pips",
                    subtitle = "Volatility: ${worldStats.volatilityLevel}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricCard(
                    title = "USD Correlation",
                    value = String.format("%+.2f", worldStats.usdCorrelation),
                    subtitle = if (worldStats.usdCorrelation < -0.5) "Inverse to DXY" else "Positive to DXY",
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Data Telemetry",
                    value = "${worldStats.syncLatencyMs} ms",
                    subtitle = worldStats.dataSource.take(20),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WorldSessionsTab(sessions: List<MarketSessionInfo>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticPrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, ArtisticPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = ArtisticPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real UTC Session Clock: Active sessions dictate global currency liquidity, spread tightening, and breakout velocity.",
                        fontSize = 11.sp,
                        color = ArtisticTextPrimary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        items(sessions) { session ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (session.isOpen) ArtisticGreen.copy(alpha = 0.5f) else ArtisticBorder,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(text = session.flag, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = session.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisticTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (session.isOpen) ArtisticGreen.copy(alpha = 0.2f) else ArtisticSurface)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (session.isOpen) "OPEN" else "CLOSED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (session.isOpen) ArtisticGreen else ArtisticTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = session.currentRole,
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${session.openUtcHour}:00 - ${session.closeUtcHour}:00 UTC",
                            fontSize = 11.sp,
                            color = ArtisticTextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${session.activeVolumeSharePercent}% global share",
                            fontSize = 10.sp,
                            color = ArtisticPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroIntelTab(symbol: MarketSymbol, macroEvents: List<WorldMacroEvent>) {
    if (macroEvents.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No high-impact macro news recorded for this session.", color = ArtisticTextMuted, fontSize = 12.sp)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(macroEvents) { event ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().border(0.5.dp, ArtisticBorder, RoundedCornerShape(8.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        when (event.impact) {
                                            "HIGH" -> ArtisticRed.copy(alpha = 0.2f)
                                            "MEDIUM" -> ArtisticPrimary.copy(alpha = 0.2f)
                                            else -> ArtisticSurface
                                        }
                                    )
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${event.impact} IMPACT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (event.impact) {
                                        "HIGH" -> ArtisticRed
                                        "MEDIUM" -> ArtisticPrimary
                                        else -> ArtisticTextMuted
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = event.timeAgo, fontSize = 10.sp, color = ArtisticTextMuted)
                        }

                        Text(
                            text = event.sentiment,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (event.sentiment.contains("Bullish")) ArtisticGreen else if (event.sentiment.contains("Bearish")) ArtisticRed else ArtisticTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = event.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = event.details,
                        fontSize = 11.sp,
                        color = ArtisticTextMuted,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Source: ${event.source}",
                        fontSize = 9.sp,
                        color = ArtisticPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AllPairsMatrixTab(
    allPairsStats: Map<String, WorldMarketStats>,
    onSelectSymbol: (MarketSymbol) -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Tap any pair to switch charts & analyze world statistics:",
                fontSize = 11.sp,
                color = ArtisticTextMuted
            )
        }

        items(MarketCatalog.symbols) { pair ->
            val stats = allPairsStats[pair.id]
            val livePrice = stats?.price ?: pair.basePrice
            val changePct = stats?.changePercent24h ?: 0.25
            val isPositive = changePct >= 0

            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, ArtisticBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        onSelectSymbol(pair)
                        onDismiss()
                    }
                    .testTag("select_pair_${pair.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pair.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisticTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = pair.category,
                                fontSize = 9.sp,
                                color = ArtisticTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "24h Range: ${String.format("%.1f", stats?.dailyPipRange ?: 60.0)} pips • ${stats?.bullishSentimentPercent ?: 50}% Bull",
                            fontSize = 10.sp,
                            color = ArtisticTextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%.${pair.digits}f".format(livePrice),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ArtisticTextPrimary
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${String.format("%.2f", changePct)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) ArtisticGreen else ArtisticRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ArtisticCard),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(0.5.dp, ArtisticBorder, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = ArtisticTextMuted)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisticTextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = ArtisticPrimary)
        }
    }
}

private fun formatVolume(volume: Double): String {
    return when {
        volume >= 1_000_000_000_000.0 -> String.format("%.2fT", volume / 1_000_000_000_000.0)
        volume >= 1_000_000_000.0 -> String.format("%.2fB", volume / 1_000_000_000.0)
        volume >= 1_000_000.0 -> String.format("%.1fM", volume / 1_000_000.0)
        else -> String.format("%.0f", volume)
    }
}
