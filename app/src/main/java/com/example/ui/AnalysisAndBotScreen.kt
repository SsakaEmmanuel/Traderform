package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BotConfig
import com.example.model.MarketSymbol
import com.example.model.OrderStatus
import com.example.model.OrderType
import com.example.model.SignalType
import com.example.ui.components.PipControlArea
import com.example.ui.components.TradingConfigDialog
import com.example.ui.components.WorldMarketHubBottomSheet
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCard
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalysisAndBotScreen(
    uiState: TradingUiState,
    onToggleAutoTrading: (Boolean) -> Unit,
    onExecuteSignalOrder: (OrderType) -> Unit,
    onRefreshAnalysis: () -> Unit,
    onCloseOrder: (String) -> Unit,
    onToggleMaMatrix: (Boolean) -> Unit = {},
    onToggleRsi: (Boolean) -> Unit = {},
    onToggleMacd: (Boolean) -> Unit = {},
    onUpdateDefaultTp: (Double) -> Unit = {},
    onUpdateDefaultSl: (Double) -> Unit = {},
    onResetDefaults: () -> Unit = {},
    onStepOrderExitPips: (String, Double, Double) -> Unit = { _, _, _ -> },
    onUpdateOrderExitPips: (String, Double, Double) -> Unit = { _, _, _ -> },
    onSetBreakeven: (String) -> Unit = {},
    onResetOrderToNormal: (String) -> Unit = {},
    onSyncWorldMarket: () -> Unit = {},
    onSymbolSelected: (MarketSymbol) -> Unit = {},
    onSetAccuracyOverride: (Int?) -> Unit = {},
    onSimulateFivePercentTp: (OrderType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val analysis = uiState.analysis
    val symbol = uiState.selectedSymbol
    val isAutoTrading = uiState.botConfig.isAutoTradingEnabled
    val currentSymbolOrders = uiState.openOrders.filter { it.symbolId == symbol.id }
    var isConfigDialogOpen by remember { mutableStateOf(false) }
    var isWorldHubOpen by remember { mutableStateOf(false) }

    if (isWorldHubOpen) {
        WorldMarketHubBottomSheet(
            symbol = uiState.selectedSymbol,
            worldStats = uiState.worldMarketStats,
            marketSessions = uiState.marketSessions,
            macroEvents = uiState.macroEvents,
            allPairsStats = uiState.allPairsStats,
            isSyncing = uiState.isWorldSyncing,
            onManualSync = onSyncWorldMarket,
            onSelectSymbol = onSymbolSelected,
            onDismiss = { isWorldHubOpen = false }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (isConfigDialogOpen) {
        TradingConfigDialog(
            indicatorConfig = uiState.indicatorConfig,
            defaultTpPips = uiState.botConfig.targetProfitPips,
            defaultSlPips = uiState.botConfig.riskStopPips,
            onToggleMaMatrix = onToggleMaMatrix,
            onToggleRsi = onToggleRsi,
            onToggleMacd = onToggleMacd,
            onUpdateDefaultTp = onUpdateDefaultTp,
            onUpdateDefaultSl = onUpdateDefaultSl,
            onResetDefaults = onResetDefaults,
            onDismiss = { isConfigDialogOpen = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArtisticBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("division_analysis_bot")
    ) {
        // Section Header with Refresh Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ArtisticPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trade Analyzer & Scalper Bot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArtisticTextPrimary
                    )
                }
                Text(
                    text = "${symbol.name} • Timeframe ${uiState.selectedTimeFrame.label} • 10-Pip Scalp Engine",
                    fontSize = 12.sp,
                    color = ArtisticTextMuted
                )
            }

            IconButton(
                onClick = onRefreshAnalysis,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ArtisticCard)
                    .border(1.dp, ArtisticBorder, CircleShape)
                    .size(38.dp)
                    .testTag("refresh_analysis_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Signal Analysis",
                    tint = ArtisticPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CONFIGURATION MENU TRIGGER CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ArtisticBorder, RoundedCornerShape(12.dp))
                .clickable { isConfigDialogOpen = true }
                .testTag("open_indicators_config_button"),
            colors = CardDefaults.cardColors(containerColor = ArtisticCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = ArtisticPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Indicator & Risk Configuration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = ArtisticTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ArtisticPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("SETUP", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ArtisticPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ConfigChipBadge("4-MA", uiState.indicatorConfig.showMaMatrix)
                            ConfigChipBadge("RSI", uiState.indicatorConfig.showRsi)
                            ConfigChipBadge("MACD", uiState.indicatorConfig.showMacd)
                            Text("•", fontSize = 10.sp, color = ArtisticTextMuted)
                            Text(
                                "TP +${String.format("%.1f", uiState.botConfig.targetProfitPips)}p",
                                fontSize = 10.sp,
                                color = ArtisticGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "SL -${String.format("%.1f", uiState.botConfig.riskStopPips)}p",
                                fontSize = 10.sp,
                                color = ArtisticRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Configure",
                    tint = ArtisticPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live World Market Statistical Intelligence Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ArtisticCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isWorldHubOpen = true }
                .testTag("world_market_stats_overview_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ArtisticPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = ArtisticPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "World Market Statistical Intelligence",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ArtisticTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(ArtisticGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("LIVE SYNC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ArtisticGreen)
                                }
                            }
                            Text(
                                text = "${uiState.selectedSymbol.name} • ${uiState.worldMarketStats?.lastSyncTimeFormatted ?: "UTC Live"}",
                                fontSize = 10.sp,
                                color = ArtisticTextMuted
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { isWorldHubOpen = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("World Hub", fontSize = 10.sp, color = ArtisticPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                uiState.worldMarketStats?.let { ws ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: 24h Pip Range
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("24h Pip Range", fontSize = 9.sp, color = ArtisticTextMuted)
                                Text(
                                    "${String.format("%.1f", ws.dailyPipRange)} pips",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisticTextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Stat 2: World Sentiment
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("World Sentiment", fontSize = 9.sp, color = ArtisticTextMuted)
                                Text(
                                    "${ws.bullishSentimentPercent}% Bullish",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ws.bullishSentimentPercent >= 50) ArtisticGreen else ArtisticRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Stat 3: ATR (14)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("ATR (14)", fontSize = 9.sp, color = ArtisticTextMuted)
                                Text(
                                    "${String.format("%.1f", ws.atr14Pips)} pips",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArtisticPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto-Trading Master Bot Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isAutoTrading) ArtisticPrimary.copy(alpha = 0.08f) else ArtisticCard
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAutoTrading) ArtisticPrimary else ArtisticBorder
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("auto_trading_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (isAutoTrading) ArtisticGreen else ArtisticTextMuted)
                                .alpha(if (isAutoTrading) pulseAlpha else 1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto-Pilot 10-Pip Scalper",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ArtisticTextPrimary
                            )
                            Text(
                                text = if (isAutoTrading) "Active & scanning ticks" else "Paused (manual control only)",
                                fontSize = 11.sp,
                                color = if (isAutoTrading) ArtisticGreen else ArtisticTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = isAutoTrading,
                        onCheckedChange = onToggleAutoTrading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ArtisticGreen,
                            uncheckedThumbColor = ArtisticTextMuted,
                            uncheckedTrackColor = ArtisticCard
                        ),
                        modifier = Modifier.testTag("auto_trading_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // User Strategy Mandates (10 Pips TP & 15 Pips SL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rule 1: 10 Pips TP
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ArtisticGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto Take Profit", fontSize = 11.sp, color = ArtisticGreen, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Only @ 5% Profit", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticTextPrimary)
                            Text("Inserts on BUY & SELL sides", fontSize = 9.sp, color = ArtisticTextMuted)
                        }
                    }

                    // Rule 2: 15 Pips SL Risk Limit
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = ArtisticRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Risk Stop Loss", fontSize = 11.sp, color = ArtisticRed, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("-15.0 Pips", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticTextPrimary)
                            Text("Auto-cancels trade", fontSize = 9.sp, color = ArtisticTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dynamic Bot Status Log
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArtisticSurface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Status: ${uiState.botStatusMessage}",
                        fontSize = 11.sp,
                        color = ArtisticPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ACCURACY MULTI-TRADE PROTOCOL CARD
                val currentAccuracy = analysis?.confidencePercent ?: 75
                val currentTradeTarget = BotConfig.getTradeCountForAccuracy(currentAccuracy)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArtisticSurface)
                        .border(1.dp, ArtisticBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = ArtisticPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dynamic Multi-Trade Protocol",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisticTextPrimary
                            )
                        }

                        // Current accuracy tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (currentTradeTarget > 0) ArtisticPrimary.copy(alpha = 0.2f)
                                    else ArtisticBorder
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$currentAccuracy% Accuracy",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTradeTarget > 0) ArtisticPrimary else ArtisticTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Tier Execution Matrix:
                    // 1 trade @ 50%, 2 trades @ 72%, 3 trades @ >72%
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // TIER 1: 50% -> 1 Trade
                        val isTier1Active = currentAccuracy in 50..71
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTier1Active) ArtisticPrimary.copy(alpha = 0.15f) else ArtisticCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isTier1Active) ArtisticPrimary else ArtisticBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isTier1Active) {
                                    Text("⚡ ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticPrimary)
                                } else {
                                    Text("TIER 1", fontSize = 8.sp, color = ArtisticTextMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("50% Acc", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                                Text(
                                    "1 Trade",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isTier1Active) ArtisticPrimary else ArtisticTextPrimary
                                )
                            }
                        }

                        // TIER 2: 72% -> 2 Trades
                        val isTier2Active = currentAccuracy == 72
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTier2Active) ArtisticPrimary.copy(alpha = 0.15f) else ArtisticCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isTier2Active) ArtisticPrimary else ArtisticBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isTier2Active) {
                                    Text("⚡ ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticPrimary)
                                } else {
                                    Text("TIER 2", fontSize = 8.sp, color = ArtisticTextMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("72% Acc", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                                Text(
                                    "2 Trades",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isTier2Active) ArtisticPrimary else ArtisticTextPrimary
                                )
                            }
                        }

                        // TIER 3: Above 72% -> 3 Trades
                        val isTier3Active = currentAccuracy > 72
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTier3Active) ArtisticPrimary.copy(alpha = 0.15f) else ArtisticCard
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isTier3Active) ArtisticPrimary else ArtisticBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (isTier3Active) {
                                    Text("⚡ ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticPrimary)
                                } else {
                                    Text("TIER 3", fontSize = 8.sp, color = ArtisticTextMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("> 72% Acc", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                                Text(
                                    "3 Trades",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isTier3Active) ArtisticPrimary else ArtisticTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Accuracy Test / Calibration Quick Switcher
                    Text("Calibrate / Test Accuracy Scenario:", fontSize = 10.sp, color = ArtisticTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val override = uiState.simulatedAccuracyOverride
                        // Auto Dynamic Market Mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (override == null) ArtisticPrimary.copy(alpha = 0.25f) else ArtisticCard)
                                .border(1.dp, if (override == null) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(6.dp))
                                .clickable { onSetAccuracyOverride(null) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Auto",
                                fontSize = 11.sp,
                                fontWeight = if (override == null) FontWeight.Bold else FontWeight.Normal,
                                color = ArtisticTextPrimary
                            )
                        }

                        // 50% Preset (1 Trade)
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (override == 50) ArtisticPrimary.copy(alpha = 0.25f) else ArtisticCard)
                                .border(1.dp, if (override == 50) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(6.dp))
                                .clickable { onSetAccuracyOverride(50) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "50% (1 Trade)",
                                fontSize = 11.sp,
                                fontWeight = if (override == 50) FontWeight.Bold else FontWeight.Normal,
                                color = ArtisticTextPrimary
                            )
                        }

                        // 72% Preset (2 Trades)
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (override == 72) ArtisticPrimary.copy(alpha = 0.25f) else ArtisticCard)
                                .border(1.dp, if (override == 72) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(6.dp))
                                .clickable { onSetAccuracyOverride(72) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "72% (2 Trades)",
                                fontSize = 11.sp,
                                fontWeight = if (override == 72) FontWeight.Bold else FontWeight.Normal,
                                color = ArtisticTextPrimary
                            )
                        }

                        // 85% Preset (3 Trades)
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (override == 85) ArtisticPrimary.copy(alpha = 0.25f) else ArtisticCard)
                                .border(1.dp, if (override == 85) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(6.dp))
                                .clickable { onSetAccuracyOverride(85) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "85% (3 Trades)",
                                fontSize = 11.sp,
                                fontWeight = if (override == 85) FontWeight.Bold else FontWeight.Normal,
                                color = ArtisticTextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5% AUTOMATIC TAKE PROFIT PROTOCOL CARD (BOTH SIDES: BUY & SELL)
        Card(
            colors = CardDefaults.cardColors(containerColor = ArtisticCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticGreen.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("auto_tp_protocol_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(ArtisticGreen.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ArtisticGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Auto-TP Protocol (5% Rule)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = ArtisticTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ArtisticGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("BOTH SIDES", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticGreen)
                                }
                            }
                            Text(
                                text = "Takes profit only after detecting +5.0% profit on BUY or SELL",
                                fontSize = 10.sp,
                                color = ArtisticTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ArtisticGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "5.0% Trigger",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ArtisticGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Mandate: Take profit is withheld upon entry and automatically inserted on both sides once the bot verifies profit reaches 5%.",
                    fontSize = 11.sp,
                    color = ArtisticTextMuted,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dual-Side Protocol Status Badges (BUY & SELL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BUY SIDE CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00B0FF).copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color(0xFF00B0FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("BUY Side", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00B0FF))
                                }
                                Text("ARMED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ArtisticGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Inserts @ +5.0% Profit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onSimulateFivePercentTp(OrderType.BUY) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth().height(30.dp).testTag("simulate_buy_5pct_button")
                            ) {
                                Text("Test BUY 5% TP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }

                    // SELL SIDE CARD
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9100),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SELL Side", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFFF9100))
                                }
                                Text("ARMED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ArtisticGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Inserts @ +5.0% Profit", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ArtisticTextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onSimulateFivePercentTp(OrderType.SELL) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth().height(30.dp).testTag("simulate_sell_5pct_button")
                            ) {
                                Text("Test SELL 5% TP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Active Order Profit Tracker
                val openOrder = uiState.openOrders.firstOrNull { it.symbolId == symbol.id }
                if (openOrder != null) {
                    val progress = (openOrder.profitPercent / 5.0).coerceIn(0.0, 1.0).toFloat()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticSurface)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Active #${openOrder.id} (${openOrder.type.name}): ${if (openOrder.profitPercent >= 0) "+" else ""}${String.format("%.1f", openOrder.profitPercent)}% profit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (openOrder.profitPercent >= 5.0) ArtisticGreen else ArtisticTextPrimary
                            )
                            Text(
                                text = if (openOrder.profitPercent >= 5.0) "5% Target Hit!" else "Target: 5.0%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (openOrder.profitPercent >= 5.0) ArtisticGreen else ArtisticTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(2.5.dp)),
                            color = if (openOrder.profitPercent >= 5.0) ArtisticGreen else ArtisticPrimary,
                            trackColor = ArtisticCard
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticSurface)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting trade entry. Auto-TP will trigger on 5% profit detection.",
                            fontSize = 10.sp,
                            color = ArtisticTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIP CONTROL AREA (Determine exit pips & manual adjustment after it sets to normal)
        PipControlArea(
            openOrders = uiState.openOrders,
            symbol = symbol,
            defaultTpPips = uiState.botConfig.targetProfitPips,
            defaultSlPips = uiState.botConfig.riskStopPips,
            onStepOrderExitPips = onStepOrderExitPips,
            onUpdateOrderExitPips = onUpdateOrderExitPips,
            onSetBreakeven = onSetBreakeven,
            onResetOrderToNormal = onResetOrderToNormal,
            onCloseOrderManual = onCloseOrder,
            onUpdateDefaultTp = onUpdateDefaultTp,
            onUpdateDefaultSl = onUpdateDefaultSl,
            onResetDefaults = onResetDefaults
        )

        Spacer(modifier = Modifier.height(16.dp))
        analysis?.let { ana ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("signal_analysis_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Position Recommendation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ArtisticTextPrimary
                        )

                        // Signal Type Badge
                        val (signalColor, signalText) = when (ana.signal) {
                            SignalType.STRONG_BUY -> ArtisticGreen to "STRONG BUY"
                            SignalType.BUY -> ArtisticGreen to "BUY"
                            SignalType.STRONG_SELL -> ArtisticRed to "STRONG SELL"
                            SignalType.SELL -> ArtisticRed to "SELL"
                            SignalType.NEUTRAL -> ArtisticTextMuted to "WAIT / NEUTRAL"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(signalColor.copy(alpha = 0.18f))
                                .border(1.dp, signalColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("recommended_signal_badge")
                        ) {
                            Text(
                                text = signalText,
                                color = signalColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Accuracy & Multi-Trade Tier
                    val targetTrades = BotConfig.getTradeCountForAccuracy(ana.confidencePercent)
                    val tierLabel = BotConfig.getAccuracyTierLabel(ana.confidencePercent)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Accuracy / Confidence", fontSize = 12.sp, color = ArtisticTextMuted)
                        Text(
                            "${ana.confidencePercent}% • $tierLabel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (targetTrades > 0) ArtisticPrimary else ArtisticTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { ana.confidencePercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (ana.signal in listOf(SignalType.STRONG_BUY, SignalType.BUY)) ArtisticGreen else ArtisticRed,
                        trackColor = ArtisticBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Technical Indicators Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // RSI
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("RSI (14)", fontSize = 10.sp, color = ArtisticTextMuted)
                                Text(
                                    text = String.format("%.1f", ana.rsi14),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (ana.rsi14 < 40) ArtisticGreen else if (ana.rsi14 > 60) ArtisticRed else ArtisticTextPrimary
                                )
                            }
                        }

                        // Support Zone
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Key Support", fontSize = 10.sp, color = ArtisticTextMuted)
                                Text(
                                    text = "%.${symbol.digits}f".format(ana.supportLevel),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ArtisticGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Resistance Zone
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("Key Resistance", fontSize = 10.sp, color = ArtisticTextMuted)
                                Text(
                                    text = "%.${symbol.digits}f".format(ana.resistanceLevel),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = ArtisticRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deep Technical Rationale
                    Text(
                        text = ana.summaryText,
                        fontSize = 12.sp,
                        color = ArtisticTextPrimary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1-Tap Execution Based on Recommendation & Accuracy Tier
                    val isBuySignal = ana.signal in listOf(SignalType.STRONG_BUY, SignalType.BUY)
                    val actionColor = if (isBuySignal) ArtisticGreen else ArtisticRed
                    val targetTradeCount = BotConfig.getTradeCountForAccuracy(ana.confidencePercent)
                    val tradeNoun = if (targetTradeCount == 1) "1 Trade" else "$targetTradeCount Trades"

                    Button(
                        onClick = {
                            onExecuteSignalOrder(if (isBuySignal) OrderType.BUY else OrderType.SELL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("execute_signal_entry_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBuySignal) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Execute Recommended ${if (isBuySignal) "BUY" else "SELL"} ($tradeNoun • +10p TP)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Orders on This Market
        if (currentSymbolOrders.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("active_orders_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Active Live Positions (${currentSymbolOrders.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    currentSymbolOrders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArtisticSurface)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = order.type.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (order.type == OrderType.BUY) ArtisticGreen else ArtisticRed,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "#${order.id} • ${order.lotSize} lots",
                                        fontSize = 11.sp,
                                        color = ArtisticTextMuted
                                    )
                                }
                                Text(
                                    text = "Entry: %.${symbol.digits}f | TP: +%.1fp | SL: -%.1fp".format(order.entryPrice, order.targetProfitPips, order.riskStopPips),
                                    fontSize = 10.sp,
                                    color = ArtisticTextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    val sign = if (order.profitPips >= 0) "+" else ""
                                    Text(
                                        text = "$sign${String.format("%.1f", order.profitPips)} pips",
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.profitPips >= 0) ArtisticGreen else ArtisticRed,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "$sign$${String.format("%.2f", order.profitUsd)}",
                                        fontSize = 11.sp,
                                        color = ArtisticTextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                IconButton(
                                    onClick = { onCloseOrder(order.id) },
                                    modifier = Modifier.size(28.dp).testTag("cancel_active_order_${order.id}")
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Close", tint = ArtisticRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Scalper Bot Statistics Card
        Card(
            colors = CardDefaults.cardColors(containerColor = ArtisticCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("bot_stats_card")
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Scalper Bot Performance Track",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisticTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Win Rate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Win Rate", fontSize = 10.sp, color = ArtisticTextMuted)
                            Text(
                                text = "${String.format("%.1f", uiState.botStatistics.winRatePercent)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ArtisticGreen
                            )
                        }
                    }

                    // Total Pips Banked
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Total Pips", fontSize = 10.sp, color = ArtisticTextMuted)
                            Text(
                                text = "${if (uiState.botStatistics.totalPips >= 0) "+" else ""}${String.format("%.1f", uiState.botStatistics.totalPips)}p",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ArtisticPrimary
                            )
                        }
                    }

                    // Net Profit ($)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Net Profit", fontSize = 10.sp, color = ArtisticTextMuted)
                            Text(
                                text = "+$${String.format("%.0f", uiState.botStatistics.totalProfitUsd)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ArtisticGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Completed Trades Journal (Showing 10-pip TP hits and 15-pip SL cancellations)
        if (uiState.closedOrders.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ArtisticCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("closed_trades_history_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Recent Completed Trades",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.closedOrders.take(5).forEach { closed ->
                        val isWin = closed.profitPips > 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isWin) ArtisticGreen.copy(alpha = 0.2f) else ArtisticRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isWin) Icons.Default.Check else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (isWin) ArtisticGreen else ArtisticRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${closed.type.name} • ${closed.symbolId}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ArtisticTextPrimary
                                    )
                                    Text(
                                        text = closed.closeNote ?: if (isWin) "TP +10.0 pips hit" else "SL -15.0 pips cut",
                                        fontSize = 10.sp,
                                        color = ArtisticTextMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (isWin) "+" else ""}${String.format("%.1f", closed.profitPips)} pips",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isWin) ArtisticGreen else ArtisticRed,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${if (isWin) "+" else ""}$${String.format("%.2f", closed.profitUsd)}",
                                    fontSize = 10.sp,
                                    color = ArtisticTextMuted
                                )
                            }
                        }
                        Divider(color = ArtisticBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigChipBadge(label: String, isEnabled: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isEnabled) ArtisticPrimary.copy(alpha = 0.2f) else ArtisticCard)
            .border(0.5.dp, if (isEnabled) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = "$label ${if (isEnabled) "ON" else "OFF"}",
            fontSize = 9.sp,
            fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Normal,
            color = if (isEnabled) ArtisticPrimary else ArtisticTextMuted
        )
    }
}
