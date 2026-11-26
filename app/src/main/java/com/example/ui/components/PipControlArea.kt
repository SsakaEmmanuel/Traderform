package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.model.MarketSymbol
import com.example.model.OrderType
import com.example.model.TradeOrder
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

/**
 * Pip Control Area:
 * Determines the number of pips to exit the trade, and allows manual adjustment after it sets to normal.
 */
@Composable
fun PipControlArea(
    openOrders: List<TradeOrder>,
    symbol: MarketSymbol,
    defaultTpPips: Double,
    defaultSlPips: Double,
    onStepOrderExitPips: (orderId: String, deltaTp: Double, deltaSl: Double) -> Unit,
    onUpdateOrderExitPips: (orderId: String, tpPips: Double, slPips: Double) -> Unit,
    onSetBreakeven: (orderId: String) -> Unit,
    onResetOrderToNormal: (orderId: String) -> Unit,
    onCloseOrderManual: (orderId: String) -> Unit,
    onUpdateDefaultTp: (Double) -> Unit,
    onUpdateDefaultSl: (Double) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if there are active orders for the current symbol, or fallback to any open order
    val relevantOrders = openOrders.filter { it.symbolId == symbol.id }
    val activeOrder = relevantOrders.firstOrNull() ?: openOrders.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pip_control_area"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ArtisticBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
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
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = ArtisticPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PIP CONTROL & EXIT MANAGEMENT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ArtisticPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (activeOrder != null) "Adjust exit targets dynamically after default setup" else "Pre-set exit targets before trade execution",
                            fontSize = 10.sp,
                            color = ArtisticTextMuted
                        )
                    }
                }

                if (activeOrder != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (activeOrder.profitPips >= 0) ArtisticGreen.copy(alpha = 0.15f) else ArtisticRed.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${if (activeOrder.profitPips >= 0) "+" else ""}${String.format("%.1f", activeOrder.profitPips)}p",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeOrder.profitPips >= 0) ArtisticGreen else ArtisticRed,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ArtisticBorder.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("NORMAL: 10p/15p", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ArtisticTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ArtisticBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            if (activeOrder != null) {
                // ACTIVE TRADE CONTROLLER
                ActiveTradePipController(
                    order = activeOrder,
                    symbol = symbol,
                    onStepPips = { deltaTp, deltaSl -> onStepOrderExitPips(activeOrder.id, deltaTp, deltaSl) },
                    onSetExitPips = { tp, sl -> onUpdateOrderExitPips(activeOrder.id, tp, sl) },
                    onSetBreakeven = { onSetBreakeven(activeOrder.id) },
                    onResetToNormal = { onResetOrderToNormal(activeOrder.id) },
                    onCloseNow = { onCloseOrderManual(activeOrder.id) }
                )
            } else {
                // PRE-TRADE PIP TARGET CONTROLLER
                PreTradePipController(
                    defaultTpPips = defaultTpPips,
                    defaultSlPips = defaultSlPips,
                    symbol = symbol,
                    onUpdateTp = onUpdateDefaultTp,
                    onUpdateSl = onUpdateDefaultSl,
                    onResetDefaults = onResetDefaults
                )
            }
        }
    }
}

@Composable
private fun ActiveTradePipController(
    order: TradeOrder,
    symbol: MarketSymbol,
    onStepPips: (deltaTp: Double, deltaSl: Double) -> Unit,
    onSetExitPips: (tpPips: Double, slPips: Double) -> Unit,
    onSetBreakeven: () -> Unit,
    onResetToNormal: () -> Unit,
    onCloseNow: () -> Unit
) {
    val isBuy = order.type == OrderType.BUY
    val isNormal = order.targetProfitPips == 10.0 && order.riskStopPips == 15.0
    val isBreakeven = order.riskStopPips <= 1.0

    // Order Overview Banner
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ArtisticCard)
            .border(1.dp, ArtisticBorder, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isBuy) Color(0xFF00B0FF) else Color(0xFFFF9100))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = order.type.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "${order.symbolId} • ${order.lotSize} Lots @ ${String.format("%.${symbol.digits}f", order.entryPrice)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisticTextPrimary
                )
                Text(
                    text = if (isNormal) "Current Mode: Normal (10p TP / 15p SL)" else "Current Mode: Manual Override (${String.format("%.1f", order.targetProfitPips)}p TP / ${String.format("%.1f", order.riskStopPips)}p SL)",
                    fontSize = 9.sp,
                    color = if (isNormal) ArtisticTextMuted else ArtisticPrimary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (order.profitUsd >= 0) "+" else ""}$${String.format("%.2f", order.profitUsd)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (order.profitUsd >= 0) ArtisticGreen else ArtisticRed,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${if (order.profitPips >= 0) "+" else ""}${String.format("%.1f", order.profitPips)} pips (${if (order.profitPercent >= 0) "+" else ""}${String.format("%.1f", order.profitPercent)}%)",
                fontSize = 9.sp,
                color = ArtisticTextMuted
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5% Auto Take Profit Protocol Status Card (Applies to both BUY and SELL sides)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (order.isTakeProfitInserted) ArtisticGreen.copy(alpha = 0.12f) else ArtisticCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (order.isTakeProfitInserted) ArtisticGreen else ArtisticBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (order.isTakeProfitInserted) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (order.isTakeProfitInserted) ArtisticGreen else Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (order.isTakeProfitInserted) "Auto-TP Inserted (5% Reached)" else "Auto-TP Protocol (Pending 5% Profit)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isTakeProfitInserted) ArtisticGreen else ArtisticTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (order.isTakeProfitInserted) ArtisticGreen.copy(alpha = 0.2f) else Color(0xFFFFB300).copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${order.type.name} Side • 5% Rule",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.isTakeProfitInserted) ArtisticGreen else Color(0xFFFFB300)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val progress = (order.profitPercent / 5.0).coerceIn(0.0, 1.0).toFloat()
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = if (order.isTakeProfitInserted) ArtisticGreen else Color(0xFFFFB300),
                trackColor = ArtisticSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (order.isTakeProfitInserted) {
                    "Automatic Take Profit order is inserted at 5% profit and securing trade."
                } else {
                    "Automatic Take Profit will insert only on ${order.type.name} side once profit reaches 5% (Progress: ${String.format("%.1f", order.profitPercent.coerceAtLeast(0.0))}% / 5.0%)."
                },
                fontSize = 9.sp,
                color = ArtisticTextMuted
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 1. TAKE PROFIT PIP CONTROL
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Target Profit Exit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisticGreen
            )
            Text(
                text = "Exit @ ${String.format("%.${symbol.digits}f", order.takeProfitPrice)}",
                fontSize = 9.sp,
                color = ArtisticTextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onStepPips(-1.0, 0.0) },
                modifier = Modifier.size(28.dp).testTag("step_tp_minus_1")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "-1 pip", tint = ArtisticGreen, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArtisticGreen.copy(alpha = 0.15f))
                    .border(1.dp, ArtisticGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+${String.format("%.1f", order.targetProfitPips)} PIPS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ArtisticGreen,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { onStepPips(1.0, 0.0) },
                modifier = Modifier.size(28.dp).testTag("step_tp_plus_1")
            ) {
                Icon(Icons.Default.Add, contentDescription = "+1 pip", tint = ArtisticGreen, modifier = Modifier.size(16.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Quick TP Presets
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(5.0, 8.0, 10.0, 12.0, 15.0, 20.0).forEach { p ->
            val isSelected = order.targetProfitPips == p
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) ArtisticGreen.copy(alpha = 0.25f) else ArtisticCard)
                    .border(1.dp, if (isSelected) ArtisticGreen else ArtisticBorder, RoundedCornerShape(4.dp))
                    .clickable { onSetExitPips(p, order.riskStopPips) }
                    .padding(vertical = 3.dp)
                    .testTag("tp_quick_preset_${p.toInt()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${p.toInt()}p${if (p == 10.0) "*" else ""}",
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) ArtisticGreen else ArtisticTextPrimary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. STOP LOSS PIP CONTROL
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Risk Stop-Loss Exit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisticRed
            )
            Text(
                text = "Exit @ ${String.format("%.${symbol.digits}f", order.stopLossPrice)}",
                fontSize = 9.sp,
                color = ArtisticTextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onStepPips(0.0, -1.0) },
                modifier = Modifier.size(28.dp).testTag("step_sl_minus_1")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "-1 pip", tint = ArtisticRed, modifier = Modifier.size(16.dp))
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArtisticRed.copy(alpha = 0.15f))
                    .border(1.dp, ArtisticRed.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isBreakeven) "BREAKEVEN (+0.5p)" else "-${String.format("%.1f", order.riskStopPips)} PIPS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isBreakeven) ArtisticPrimary else ArtisticRed,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(
                onClick = { onStepPips(0.0, 1.0) },
                modifier = Modifier.size(28.dp).testTag("step_sl_plus_1")
            ) {
                Icon(Icons.Default.Add, contentDescription = "+1 pip", tint = ArtisticRed, modifier = Modifier.size(16.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Quick SL Presets
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(5.0, 8.0, 10.0, 15.0, 20.0).forEach { p ->
            val isSelected = order.riskStopPips == p
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) ArtisticRed.copy(alpha = 0.25f) else ArtisticCard)
                    .border(1.dp, if (isSelected) ArtisticRed else ArtisticBorder, RoundedCornerShape(4.dp))
                    .clickable { onSetExitPips(order.targetProfitPips, p) }
                    .padding(vertical = 3.dp)
                    .testTag("sl_quick_preset_${p.toInt()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${p.toInt()}p${if (p == 15.0) "*" else ""}",
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) ArtisticRed else ArtisticTextPrimary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Action Control Bar: Breakeven, Reset to Normal, Market Close
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Breakeven button
        OutlinedButton(
            onClick = onSetBreakeven,
            modifier = Modifier.weight(1f).height(34.dp).testTag("lock_breakeven_button"),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArtisticPrimary)
            Spacer(modifier = Modifier.width(3.dp))
            Text("Breakeven", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ArtisticPrimary)
        }

        // Reset to Normal (10/15)
        OutlinedButton(
            onClick = onResetToNormal,
            modifier = Modifier.weight(1f).height(34.dp).testTag("reset_order_normal_button"),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = ArtisticTextPrimary)
            Spacer(modifier = Modifier.width(3.dp))
            Text("Reset (10/15)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ArtisticTextPrimary)
        }

        // Market Exit Button
        Button(
            onClick = onCloseNow,
            modifier = Modifier.weight(1f).height(34.dp).testTag("market_exit_order_button"),
            colors = ButtonDefaults.buttonColors(containerColor = if (order.profitPips >= 0) ArtisticGreen else ArtisticRed),
            shape = RoundedCornerShape(6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text(
                text = "Exit (${if (order.profitPips >= 0) "+" else ""}${String.format("%.1f", order.profitPips)}p)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PreTradePipController(
    defaultTpPips: Double,
    defaultSlPips: Double,
    symbol: MarketSymbol,
    onUpdateTp: (Double) -> Unit,
    onUpdateSl: (Double) -> Unit,
    onResetDefaults: () -> Unit
) {
    val isNormal = defaultTpPips == 10.0 && defaultSlPips == 15.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Exit Pip Target Defaults for Next Trade:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ArtisticTextMuted
            )
            if (!isNormal) {
                Text(
                    text = "Manual Override Active",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisticPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Target Profit Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ArtisticCard)
                    .border(1.dp, ArtisticBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Take Profit Target", fontSize = 10.sp, color = ArtisticTextMuted)
                        Text("+${String.format("%.1f", defaultTpPips)}p", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticGreen)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateTp(defaultTpPips - 1.0) },
                            modifier = Modifier.weight(1f).height(28.dp).testTag("pre_tp_minus"),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-1p", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onUpdateTp(defaultTpPips + 1.0) },
                            modifier = Modifier.weight(1f).height(28.dp).testTag("pre_tp_plus"),
                            colors = ButtonDefaults.buttonColors(containerColor = ArtisticGreen),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+1p", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            // Stop Loss Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ArtisticCard)
                    .border(1.dp, ArtisticBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stop Loss Risk", fontSize = 10.sp, color = ArtisticTextMuted)
                        Text("-${String.format("%.1f", defaultSlPips)}p", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = ArtisticRed)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateSl(defaultSlPips - 1.0) },
                            modifier = Modifier.weight(1f).height(28.dp).testTag("pre_sl_minus"),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-1p", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { onUpdateSl(defaultSlPips + 1.0) },
                            modifier = Modifier.weight(1f).height(28.dp).testTag("pre_sl_plus"),
                            colors = ButtonDefaults.buttonColors(containerColor = ArtisticRed),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+1p", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        if (!isNormal) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onResetDefaults,
                    modifier = Modifier.height(28.dp).testTag("pre_reset_to_normal"),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Revert to Normal (10p/15p)", fontSize = 10.sp)
                }
            }
        }
    }
}
