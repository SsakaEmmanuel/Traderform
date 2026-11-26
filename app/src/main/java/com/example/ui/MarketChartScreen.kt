package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketCatalog
import com.example.model.MarketSymbol
import com.example.model.OrderStatus
import com.example.model.OrderType
import com.example.model.TimeFrame
import com.example.model.TradeOrder
import com.example.ui.components.MarketChartCanvas
import com.example.ui.components.PipControlArea
import com.example.ui.components.TradingConfigDialog
import com.example.ui.components.WorldMarketHubBottomSheet
import com.example.ui.components.WorldMarketLiveSyncStrip
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticCard
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary

@Composable
fun MarketChartScreen(
    uiState: TradingUiState,
    onSymbolSelected: (MarketSymbol) -> Unit,
    onTimeFrameSelected: (TimeFrame) -> Unit,
    onLotSizeSelected: (Double) -> Unit,
    onPlaceOrder: (OrderType) -> Unit,
    onCloseOrder: (String) -> Unit,
    onDismissNotification: () -> Unit,
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
    modifier: Modifier = Modifier
) {
    var isSymbolMenuOpen by remember { mutableStateOf(false) }
    var isConfigDialogOpen by remember { mutableStateOf(false) }
    var isPipControlExpanded by remember { mutableStateOf(false) }
    var isWorldHubOpen by remember { mutableStateOf(false) }
    val currentSymbolOrders = uiState.openOrders.filter { it.symbolId == uiState.selectedSymbol.id }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ArtisticBg)
            .testTag("division_market_chart")
    ) {
        // Notification Banner (when TP hit or order placed)
        AnimatedVisibility(
            visible = uiState.lastNotification != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            uiState.lastNotification?.let { msg ->
                Surface(
                    color = if (msg.contains("Take Profit") || msg.contains("executed")) ArtisticGreen.copy(alpha = 0.18f) else ArtisticRed.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth().testTag("chart_notification_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (msg.contains("Take Profit")) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (msg.contains("Take Profit")) ArtisticGreen else ArtisticPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = ArtisticTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = onDismissNotification, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Cancel, contentDescription = "Dismiss", tint = ArtisticTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Live World Market Synchronization Strip
        WorldMarketLiveSyncStrip(
            worldStats = uiState.worldMarketStats,
            symbol = uiState.selectedSymbol,
            isSyncing = uiState.isWorldSyncing,
            onManualSync = onSyncWorldMarket,
            onOpenWorldHub = { isWorldHubOpen = true }
        )

        // Top Bar: Symbol Selector & Timeframe Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ArtisticSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Symbol Dropdown Anchor
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticCard)
                            .border(1.dp, ArtisticBorder, RoundedCornerShape(8.dp))
                            .clickable { isSymbolMenuOpen = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("symbol_selector_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.selectedSymbol.name,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticTextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "▼",
                            color = ArtisticPrimary,
                            fontSize = 10.sp
                        )
                    }

                    DropdownMenu(
                        expanded = isSymbolMenuOpen,
                        onDismissRequest = { isSymbolMenuOpen = false },
                        modifier = Modifier.background(ArtisticCard)
                    ) {
                        MarketCatalog.symbols.forEach { sym ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sym.name, fontWeight = FontWeight.SemiBold, color = ArtisticTextPrimary)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(sym.category, fontSize = 11.sp, color = ArtisticTextMuted)
                                    }
                                },
                                onClick = {
                                    onSymbolSelected(sym)
                                    isSymbolMenuOpen = false
                                },
                                modifier = Modifier.testTag("symbol_item_${sym.id}")
                            )
                        }
                    }
                }

                // Live Price, Spread, World Hub & Indicator Config Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%.${uiState.selectedSymbol.digits}f".format(uiState.currentPrice),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = ArtisticPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Spread: ${uiState.selectedSymbol.spreadPips} pips",
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${uiState.priceChangePercent24h}%",
                                fontSize = 11.sp,
                                color = ArtisticGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // World Market Telemetry Hub Button
                    IconButton(
                        onClick = { isWorldHubOpen = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticCard)
                            .border(1.dp, ArtisticBorder, RoundedCornerShape(8.dp))
                            .size(36.dp)
                            .testTag("open_world_market_hub_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "World Market Telemetry & Statistics",
                            tint = ArtisticPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { isConfigDialogOpen = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArtisticCard)
                            .border(1.dp, ArtisticBorder, RoundedCornerShape(8.dp))
                            .size(36.dp)
                            .testTag("open_chart_indicators_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Indicators & Risk Setup",
                            tint = ArtisticPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeframe selection row: 1m, 5m, 15m, 30m, 1hr, 4hr & Indicator Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TF:",
                    color = ArtisticTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 2.dp)
                )

                TimeFrame.values().forEach { tf ->
                    val isSelected = uiState.selectedTimeFrame == tf
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ArtisticPrimary else ArtisticCard)
                            .border(1.dp, if (isSelected) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(6.dp))
                            .clickable { onTimeFrameSelected(tf) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("timeframe_${tf.label}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.label,
                            color = if (isSelected) Color.White else ArtisticTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.height(18.dp).width(1.dp).background(ArtisticBorder))
                Spacer(modifier = Modifier.width(4.dp))

                // Quick Indicator Toggles on Chart
                QuickIndicatorChip(
                    label = "4-MA",
                    isActive = uiState.indicatorConfig.showMaMatrix,
                    onClick = { onToggleMaMatrix(!uiState.indicatorConfig.showMaMatrix) },
                    testTag = "toggle_4ma_chart"
                )
                QuickIndicatorChip(
                    label = "RSI",
                    isActive = uiState.indicatorConfig.showRsi,
                    onClick = { onToggleRsi(!uiState.indicatorConfig.showRsi) },
                    testTag = "toggle_rsi_chart"
                )
                QuickIndicatorChip(
                    label = "MACD",
                    isActive = uiState.indicatorConfig.showMacd,
                    onClick = { onToggleMacd(!uiState.indicatorConfig.showMacd) },
                    testTag = "toggle_macd_chart"
                )
                QuickIndicatorChip(
                    label = "Pip Ctrl",
                    isActive = isPipControlExpanded,
                    onClick = { isPipControlExpanded = !isPipControlExpanded },
                    testTag = "toggle_pip_control_chart"
                )
            }
        }

        // Active Orders Visual Count Badge (Tap to toggle Pip Control)
        if (currentSymbolOrders.isNotEmpty()) {
            Surface(
                color = ArtisticCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, ArtisticBorder)
                    .clickable { isPipControlExpanded = !isPipControlExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(ArtisticGreen, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentSymbolOrders.size} Order Placed on Chart • ${if (isPipControlExpanded) "Close Pip Control ▲" else "Adjust Pips ▼"}",
                            fontSize = 11.sp,
                            color = ArtisticTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    val totalPips = currentSymbolOrders.sumOf { it.profitPips }
                    val totalUsd = currentSymbolOrders.sumOf { it.profitUsd }
                    Text(
                        text = "${if (totalPips >= 0) "+" else ""}${String.format("%.1f", totalPips)} pips ($${String.format("%.2f", totalUsd)})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalPips >= 0) ArtisticGreen else ArtisticRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Expandable Pip Control Area on Chart
        AnimatedVisibility(visible = isPipControlExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(ArtisticBg)
            ) {
                PipControlArea(
                    openOrders = currentSymbolOrders,
                    symbol = uiState.selectedSymbol,
                    defaultTpPips = uiState.botConfig.targetProfitPips,
                    defaultSlPips = uiState.botConfig.riskStopPips,
                    onStepOrderExitPips = onStepOrderExitPips,
                    onUpdateOrderExitPips = onUpdateOrderExitPips,
                    onSetBreakeven = onSetBreakeven,
                    onResetOrderToNormal = onResetOrderToNormal,
                    onCloseOrderManual = onCloseOrder,
                    onUpdateDefaultTp = onUpdateDefaultTp,
                    onUpdateDefaultSl = onUpdateDefaultSl,
                    onResetDefaults = onResetDefaults,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Chart Area: High visual fidelity with order lines and indicators
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MarketChartCanvas(
                candles = uiState.candles,
                orders = currentSymbolOrders,
                symbol = uiState.selectedSymbol,
                currentPrice = uiState.currentPrice,
                indicatorConfig = uiState.indicatorConfig,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Controls: Lot size & Immediate Buy / Sell entry buttons
        Surface(
            color = ArtisticSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().border(1.dp, ArtisticBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Lot Size Selector & Target Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lot Size:",
                            color = ArtisticTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        listOf(0.01, 0.05, 0.10, 0.50, 1.00).forEach { lot ->
                            val isLotSelected = uiState.selectedLotSize == lot
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isLotSelected) ArtisticPrimary.copy(alpha = 0.2f) else ArtisticCard)
                                    .border(1.dp, if (isLotSelected) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(4.dp))
                                    .clickable { onLotSizeSelected(lot) }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("lot_size_$lot"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$lot",
                                    fontSize = 11.sp,
                                    fontWeight = if (isLotSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isLotSelected) ArtisticPrimary else ArtisticTextPrimary
                                )
                            }
                        }
                    }

                    // Strict Risk/Reward Badge (5% Profit Auto TP / 15 pips SL)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ArtisticCard)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Auto-TP @ 5% Profit", fontSize = 10.sp, color = ArtisticGreen, fontWeight = FontWeight.Bold)
                        Text(text = " | ", fontSize = 10.sp, color = ArtisticTextMuted)
                        Text(text = "SL -15p", fontSize = 10.sp, color = ArtisticRed, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Buy and Sell Execution Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SELL BUTTON
                    Button(
                        onClick = { onPlaceOrder(OrderType.SELL) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArtisticRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("quick_sell_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SELL", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                            }
                            Text(
                                text = "Bid: %.${uiState.selectedSymbol.digits}f".format(uiState.bidPrice),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // BUY BUTTON
                    Button(
                        onClick = { onPlaceOrder(OrderType.BUY) },
                        colors = ButtonDefaults.buttonColors(containerColor = ArtisticGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("quick_buy_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BUY", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)
                            }
                            Text(
                                text = "Ask: %.${uiState.selectedSymbol.digits}f".format(uiState.askPrice),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active Orders List Sheet (if any)
        if (currentSymbolOrders.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArtisticCard)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Open Orders on Chart (${currentSymbolOrders.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisticTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                currentSymbolOrders.take(3).forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (order.type == OrderType.BUY) ArtisticGreen.copy(alpha = 0.2f) else ArtisticRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = order.type.name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.type == OrderType.BUY) ArtisticGreen else ArtisticRed
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "#${order.id} (${order.placedBy})",
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val sign = if (order.profitPips >= 0) "+" else ""
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$sign${String.format("%.1f", order.profitPips)}p ($sign${String.format("%.1f", order.profitPercent)}%)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (order.profitPips >= 0) ArtisticGreen else ArtisticRed,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (order.isTakeProfitInserted) "Auto-TP Inserted" else "Auto-TP Pending (5%)",
                                    fontSize = 8.sp,
                                    color = if (order.isTakeProfitInserted) ArtisticGreen else Color(0xFFFFB300),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Close",
                                fontSize = 11.sp,
                                color = ArtisticPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable { onCloseOrder(order.id) }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("close_order_${order.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickIndicatorChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) ArtisticPrimary.copy(alpha = 0.2f) else ArtisticCard)
            .border(1.dp, if (isActive) ArtisticPrimary else ArtisticBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 5.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) ArtisticPrimary else ArtisticTextMuted,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
