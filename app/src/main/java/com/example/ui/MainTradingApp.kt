package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Mt5ConnectionState
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCard
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticSurfaceElevated
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary

@Composable
fun MainTradingApp(
    tradingViewModel: TradingEngineViewModel = viewModel(),
    mt5ViewModel: Mt5LoginViewModel = viewModel()
) {
    val tradingState by tradingViewModel.uiState.collectAsStateWithLifecycle()
    val mt5State by mt5ViewModel.uiState.collectAsStateWithLifecycle()
    val isConnected = mt5State.connectionState is Mt5ConnectionState.Connected
    val activeOrdersCount = tradingState.openOrders.size

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_trading_app"),
        topBar = {
            Surface(
                color = ArtisticSurface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().border(0.5.dp, ArtisticBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ArtisticPrimary, Color(0xFF005288))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "MT5 Scalper Bot",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ArtisticTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ArtisticPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "10p TP / 15p SL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArtisticPrimary
                                    )
                                }
                            }
                            Text(
                                text = if (isConnected) "Exness Real • Online" else "Demo Exness MT5 • Live",
                                fontSize = 11.sp,
                                color = if (isConnected) ArtisticGreen else ArtisticTextMuted
                            )
                        }

                        // Auto-Trading Indicator Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (tradingState.botConfig.isAutoTradingEnabled) ArtisticGreen.copy(alpha = 0.18f)
                                    else ArtisticCard
                                )
                                .border(
                                    1.dp,
                                    if (tradingState.botConfig.isAutoTradingEnabled) ArtisticGreen else ArtisticBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (tradingState.botConfig.isAutoTradingEnabled) ArtisticGreen else ArtisticTextMuted)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (tradingState.botConfig.isAutoTradingEnabled) "BOT ON" else "BOT OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tradingState.botConfig.isAutoTradingEnabled) ArtisticGreen else ArtisticTextMuted
                            )
                        }
                    }

                    // Division Tab Bar (Three Divisions as requested)
                    TabRow(
                        selectedTabIndex = tradingState.selectedDivision,
                        containerColor = ArtisticSurfaceElevated,
                        contentColor = ArtisticPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[tradingState.selectedDivision]),
                                color = ArtisticPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        // Division 1: Gateway / Account
                        Tab(
                            selected = tradingState.selectedDivision == 0,
                            onClick = { tradingViewModel.setDivision(0) },
                            modifier = Modifier.testTag("division_tab_gateway"),
                            text = {
                                Text(
                                    text = "1. Gateway",
                                    fontWeight = if (tradingState.selectedDivision == 0) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (tradingState.selectedDivision == 0) ArtisticPrimary else ArtisticTextMuted
                                )
                            }
                        )

                        // Division 2: Chart & Orders
                        Tab(
                            selected = tradingState.selectedDivision == 1,
                            onClick = { tradingViewModel.setDivision(1) },
                            modifier = Modifier.testTag("division_tab_chart"),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "2. Chart",
                                        fontWeight = if (tradingState.selectedDivision == 1) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (tradingState.selectedDivision == 1) ArtisticPrimary else ArtisticTextMuted
                                    )
                                    if (activeOrdersCount > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(ArtisticPrimary)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text("$activeOrdersCount", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        )

                        // Division 3: AI Analyzer & Scalper Bot
                        Tab(
                            selected = tradingState.selectedDivision == 2,
                            onClick = { tradingViewModel.setDivision(2) },
                            modifier = Modifier.testTag("division_tab_bot"),
                            text = {
                                Text(
                                    text = "3. Bot Analysis",
                                    fontWeight = if (tradingState.selectedDivision == 2) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (tradingState.selectedDivision == 2) ArtisticPrimary else ArtisticTextMuted
                                )
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ArtisticSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.border(0.5.dp, ArtisticBorder)
            ) {
                // Nav 1: Exness MT5 Gateway
                NavigationBarItem(
                    selected = tradingState.selectedDivision == 0,
                    onClick = { tradingViewModel.setDivision(0) },
                    icon = {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Gateway")
                    },
                    label = { Text("Gateway", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArtisticPrimary,
                        selectedTextColor = ArtisticPrimary,
                        indicatorColor = ArtisticPrimary.copy(alpha = 0.15f),
                        unselectedIconColor = ArtisticTextMuted,
                        unselectedTextColor = ArtisticTextMuted
                    ),
                    modifier = Modifier.testTag("nav_gateway")
                )

                // Nav 2: Market Chart & Orders
                NavigationBarItem(
                    selected = tradingState.selectedDivision == 1,
                    onClick = { tradingViewModel.setDivision(1) },
                    icon = {
                        if (activeOrdersCount > 0) {
                            BadgedBox(badge = {
                                Badge(containerColor = ArtisticGreen) {
                                    Text("$activeOrdersCount")
                                }
                            }) {
                                Icon(Icons.Default.ShowChart, contentDescription = "Chart")
                            }
                        } else {
                            Icon(Icons.Default.ShowChart, contentDescription = "Chart")
                        }
                    },
                    label = { Text("Market Chart", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArtisticPrimary,
                        selectedTextColor = ArtisticPrimary,
                        indicatorColor = ArtisticPrimary.copy(alpha = 0.15f),
                        unselectedIconColor = ArtisticTextMuted,
                        unselectedTextColor = ArtisticTextMuted
                    ),
                    modifier = Modifier.testTag("nav_chart")
                )

                // Nav 3: Analyzer & 10-Pip Scalper Bot
                NavigationBarItem(
                    selected = tradingState.selectedDivision == 2,
                    onClick = { tradingViewModel.setDivision(2) },
                    icon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Analysis & Bot")
                    },
                    label = { Text("Analysis & Bot", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ArtisticPrimary,
                        selectedTextColor = ArtisticPrimary,
                        indicatorColor = ArtisticPrimary.copy(alpha = 0.15f),
                        unselectedIconColor = ArtisticTextMuted,
                        unselectedTextColor = ArtisticTextMuted
                    ),
                    modifier = Modifier.testTag("nav_bot")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (tradingState.selectedDivision) {
                0 -> {
                    // Division 1: MT5 Exness Gateway & Credentials
                    Mt5LoginScreen(viewModel = mt5ViewModel)
                }
                1 -> {
                    // Division 2: Market Chart (Multi-market, 1m/5m/15m/30m/1hr/4hr, On-Chart Order Visualizer, Quick Buy/Sell, Indicators & Pip Control)
                    MarketChartScreen(
                        uiState = tradingState,
                        onSymbolSelected = { tradingViewModel.setSymbol(it) },
                        onTimeFrameSelected = { tradingViewModel.setTimeFrame(it) },
                        onLotSizeSelected = { tradingViewModel.setLotSize(it) },
                        onPlaceOrder = { tradingViewModel.placeOrder(it, isManual = true) },
                        onCloseOrder = { tradingViewModel.closeOrderManual(it) },
                        onDismissNotification = { tradingViewModel.clearNotification() },
                        onToggleMaMatrix = { tradingViewModel.toggleIndicatorMaMatrix(it) },
                        onToggleRsi = { tradingViewModel.toggleIndicatorRsi(it) },
                        onToggleMacd = { tradingViewModel.toggleIndicatorMacd(it) },
                        onUpdateDefaultTp = { tradingViewModel.updateDefaultTakeProfitPips(it) },
                        onUpdateDefaultSl = { tradingViewModel.updateDefaultStopLossPips(it) },
                        onResetDefaults = { tradingViewModel.resetDefaultExitPips() },
                        onStepOrderExitPips = { id, dtp, dsl -> tradingViewModel.stepOrderExitPips(id, dtp, dsl) },
                        onUpdateOrderExitPips = { id, tp, sl -> tradingViewModel.updateOrderExitPips(id, tp, sl) },
                        onSetBreakeven = { tradingViewModel.setOrderBreakeven(it) },
                        onResetOrderToNormal = { tradingViewModel.resetOrderToNormalExit(it) },
                        onSyncWorldMarket = { tradingViewModel.syncWorldMarketData(forceRefresh = true) }
                    )
                }
                2 -> {
                    // Division 3: AI Trade Analyzer & Automated 10-pip TP / 15-pip SL Scalper Bot (with Indicators Configuration & Pip Control Area)
                    AnalysisAndBotScreen(
                        uiState = tradingState,
                        onToggleAutoTrading = { tradingViewModel.toggleAutoTrading(it) },
                        onExecuteSignalOrder = { tradingViewModel.executeRecommendedSignal(it) },
                        onRefreshAnalysis = { tradingViewModel.updateAnalysis() },
                        onCloseOrder = { tradingViewModel.closeOrderManual(it) },
                        onToggleMaMatrix = { tradingViewModel.toggleIndicatorMaMatrix(it) },
                        onToggleRsi = { tradingViewModel.toggleIndicatorRsi(it) },
                        onToggleMacd = { tradingViewModel.toggleIndicatorMacd(it) },
                        onUpdateDefaultTp = { tradingViewModel.updateDefaultTakeProfitPips(it) },
                        onUpdateDefaultSl = { tradingViewModel.updateDefaultStopLossPips(it) },
                        onResetDefaults = { tradingViewModel.resetDefaultExitPips() },
                        onStepOrderExitPips = { id, dtp, dsl -> tradingViewModel.stepOrderExitPips(id, dtp, dsl) },
                        onUpdateOrderExitPips = { id, tp, sl -> tradingViewModel.updateOrderExitPips(id, tp, sl) },
                        onSetBreakeven = { tradingViewModel.setOrderBreakeven(it) },
                        onResetOrderToNormal = { tradingViewModel.resetOrderToNormalExit(it) },
                        onSyncWorldMarket = { tradingViewModel.syncWorldMarketData(forceRefresh = true) },
                        onSymbolSelected = { tradingViewModel.setSymbol(it) },
                        onSetAccuracyOverride = { tradingViewModel.setAccuracyOverride(it) },
                        onSimulateFivePercentTp = { tradingViewModel.simulateFivePercentTakeProfit(it) }
                    )
                }
            }
        }
    }
}
