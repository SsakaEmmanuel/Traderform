package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Candle
import com.example.model.IndicatorConfig
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
import com.example.ui.theme.ArtisticTextMuted
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun MarketChartCanvas(
    candles: List<Candle>,
    orders: List<TradeOrder>,
    symbol: MarketSymbol,
    currentPrice: Double,
    indicatorConfig: IndicatorConfig = IndicatorConfig(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(ArtisticBg)
            .testTag("market_chart_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp)) {
            if (candles.isEmpty()) return@Canvas

            val canvasWidth = size.width
            val canvasHeight = size.height
            val rightMargin = 72.dp.toPx()
            val chartWidth = canvasWidth - rightMargin

            // Calculate height for lower oscillators (RSI & MACD)
            val rsiHeight = if (indicatorConfig.showRsi) 58.dp.toPx() else 0f
            val macdHeight = if (indicatorConfig.showMacd) 58.dp.toPx() else 0f
            val totalOscillatorHeight = rsiHeight + macdHeight
            val mainChartHeight = max(canvasHeight - totalOscillatorHeight - 8.dp.toPx(), 120.dp.toPx())

            // Calculate min and max price across candles and orders to ensure all order lines fit
            var minP = candles.minOf { it.low }
            var maxP = candles.maxOf { it.high }

            for (order in orders) {
                minP = min(minP, min(order.entryPrice, min(order.takeProfitPrice, order.stopLossPrice)))
                maxP = max(maxP, max(order.entryPrice, max(order.takeProfitPrice, order.stopLossPrice)))
            }
            minP = min(minP, currentPrice)
            maxP = max(maxP, currentPrice)

            val pricePadding = (maxP - minP) * 0.08
            val effectiveMin = minP - pricePadding
            val effectiveMax = maxP + pricePadding
            val priceRange = max(effectiveMax - effectiveMin, symbol.pipSize * 5)

            fun priceToY(price: Double): Float {
                val normalized = ((price - effectiveMin) / priceRange).toFloat()
                return mainChartHeight - (normalized * mainChartHeight)
            }

            // Draw horizontal grid lines and price labels
            val gridSteps = 4
            val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            for (i in 0..gridSteps) {
                val gridPrice = effectiveMin + (priceRange * (i.toDouble() / gridSteps))
                val y = priceToY(gridPrice)

                drawLine(
                    color = ArtisticBorder.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f,
                    pathEffect = gridDash
                )

                // Right axis price text
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 140, 145, 160)
                        textSize = 22f
                        isAntiAlias = true
                    }
                    val label = "%.${symbol.digits}f".format(gridPrice)
                    drawText(label, chartWidth + 8f, y + 7f, paint)
                }
            }

            // Draw Candlesticks
            val candleWidth = max(2f, (chartWidth / candles.size) * 0.68f)
            val candleSpacing = chartWidth / candles.size

            candles.forEachIndexed { index, candle ->
                val xCenter = (index * candleSpacing) + (candleSpacing / 2f)
                val yOpen = priceToY(candle.open)
                val yClose = priceToY(candle.close)
                val yHigh = priceToY(candle.high)
                val yLow = priceToY(candle.low)

                val candleColor = if (candle.isBullish) ArtisticGreen else ArtisticRed

                // Wick
                drawLine(
                    color = candleColor,
                    start = Offset(xCenter, yHigh),
                    end = Offset(xCenter, yLow),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Body
                val bodyTop = min(yOpen, yClose)
                val bodyHeight = max(abs(yClose - yOpen), 2f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(xCenter - (candleWidth / 2f), bodyTop),
                    size = Size(candleWidth, bodyHeight)
                )
            }

            // 4-MA MATRIX VISUALIZER (MA 9, 21, 50, 200)
            if (indicatorConfig.showMaMatrix) {
                val ma9 = calculateMovingAverage(candles, indicatorConfig.ma1Period)
                val ma21 = calculateMovingAverage(candles, indicatorConfig.ma2Period)
                val ma50 = calculateMovingAverage(candles, indicatorConfig.ma3Period)
                val ma200 = calculateMovingAverage(candles, indicatorConfig.ma4Period)

                val maConfigs = listOf(
                    Triple(ma9, Color(0xFFFFB300), "MA9"),    // Amber
                    Triple(ma21, Color(0xFF00B0FF), "MA21"),  // Cyan/Sky
                    Triple(ma50, Color(0xFF9C27B0), "MA50"),  // Purple
                    Triple(ma200, Color(0xFFE91E63), "MA200") // Magenta/Pink
                )

                maConfigs.forEach { (maValues, color, _) ->
                    val path = Path()
                    var started = false
                    candles.indices.forEach { index ->
                        val maVal = maValues[index]
                        val x = (index * candleSpacing) + (candleSpacing / 2f)
                        val y = priceToY(maVal.toDouble())
                        if (!started) {
                            path.moveTo(x, y)
                            started = true
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }

                // 4-MA Legend in top left
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = 20f
                        isAntiAlias = true
                        isFakeBoldText = true
                    }
                    var xOffset = 10f
                    val yPos = 20f

                    val legendItems = listOf(
                        Pair(Color(0xFFFFB300), "● MA9: %.${symbol.digits}f".format(ma9.last())),
                        Pair(Color(0xFF00B0FF), "● MA21: %.${symbol.digits}f".format(ma21.last())),
                        Pair(Color(0xFF9C27B0), "● MA50: %.${symbol.digits}f".format(ma50.last())),
                        Pair(Color(0xFFE91E63), "● MA200: %.${symbol.digits}f".format(ma200.last()))
                    )

                    legendItems.forEach { (col, text) ->
                        paint.color = android.graphics.Color.argb(255, (col.red * 255).toInt(), (col.green * 255).toInt(), (col.blue * 255).toInt())
                        drawText(text, xOffset, yPos, paint)
                        xOffset += paint.measureText(text) + 16f
                    }
                }
            }

            // Draw Current Live Price line
            val currentY = priceToY(currentPrice)
            drawLine(
                color = ArtisticPrimary,
                start = Offset(0f, currentY),
                end = Offset(canvasWidth, currentY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Current price badge on the right
            val priceBadgeBg = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(0, 102, 204)
                style = android.graphics.Paint.Style.FILL
                isAntiAlias = true
            }
            val priceBadgeText = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }

            drawContext.canvas.nativeCanvas.apply {
                val text = "%.${symbol.digits}f".format(currentPrice)
                val rect = android.graphics.RectF(chartWidth + 4f, currentY - 14f, canvasWidth, currentY + 14f)
                drawRoundRect(rect, 4f, 4f, priceBadgeBg)
                drawText(text, chartWidth + 8f, currentY + 7f, priceBadgeText)
            }

            // ON-CHART ORDER PLACEMENT VISUALIZER WITH DYNAMIC ADJUSTED PIPS
            for (order in orders) {
                val yEntry = priceToY(order.entryPrice)
                val yTp = priceToY(order.takeProfitPrice)
                val ySl = priceToY(order.stopLossPrice)

                val isBuy = order.type == OrderType.BUY
                val orderColor = if (isBuy) Color(0xFF00B0FF) else Color(0xFFFF9100)

                // 1. Entry Line (Dashed)
                val orderDash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                drawLine(
                    color = orderColor,
                    start = Offset(0f, yEntry),
                    end = Offset(chartWidth, yEntry),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = orderDash
                )

                // Entry Label Badge on Canvas
                drawContext.canvas.nativeCanvas.apply {
                    val badgePaint = android.graphics.Paint().apply {
                        color = if (isBuy) android.graphics.Color.rgb(0, 176, 255) else android.graphics.Color.rgb(255, 145, 0)
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 20f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val sign = if (order.profitPips >= 0) "+" else ""
                    val orderText = "${order.type.name} #${order.id} @ %.${symbol.digits}f ($sign%.1f p) [${order.placedBy}]"
                        .format(order.entryPrice, order.profitPips)
                    val textWidth = textPaint.measureText(orderText)
                    val rect = android.graphics.RectF(10f, yEntry - 20f, 22f + textWidth, yEntry + 4f)
                    drawRoundRect(rect, 5f, 5f, badgePaint)
                    drawText(orderText, 16f, yEntry - 4f, textPaint)
                }

                // 2. Take Profit Line (Inserted only after profit reaches 5% on either side)
                val isTpInserted = order.isTakeProfitInserted
                val tpDash = if (isTpInserted) null else PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                val tpColor = if (isTpInserted) ArtisticGreen else Color(0xFFFFB300)

                drawLine(
                    color = tpColor,
                    start = Offset(0f, yTp),
                    end = Offset(chartWidth, yTp),
                    strokeWidth = if (isTpInserted) 2.2.dp.toPx() else 1.5.dp.toPx(),
                    pathEffect = tpDash
                )

                // TP Label Badge
                drawContext.canvas.nativeCanvas.apply {
                    val tpBadgePaint = android.graphics.Paint().apply {
                        color = if (isTpInserted) android.graphics.Color.rgb(0, 200, 83) else android.graphics.Color.rgb(255, 179, 0)
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val tpTextPaint = android.graphics.Paint().apply {
                        color = if (isTpInserted) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                        textSize = 19f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val sign = if (order.profitPercent >= 0) "+" else ""
                    val tpText = if (isTpInserted) {
                        "AUTO-TP INSERTED (+5% PROFIT REACHED @ %.${symbol.digits}f)".format(order.takeProfitPrice)
                    } else {
                        "AUTO-TP PENDING (INSERTS @ +5% PROFIT • NOW: $sign%.1f%%)".format(order.profitPercent)
                    }
                    val tpWidth = tpTextPaint.measureText(tpText)
                    val rect = android.graphics.RectF(chartWidth - tpWidth - 20f, yTp - 16f, chartWidth - 6f, yTp + 4f)
                    drawRoundRect(rect, 4f, 4f, tpBadgePaint)
                    drawText(tpText, chartWidth - tpWidth - 14f, yTp - 2f, tpTextPaint)
                }

                // 3. Stop Loss Line (Red Dashed: shows adjusted riskStopPips)
                val slDash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                drawLine(
                    color = ArtisticRed,
                    start = Offset(0f, ySl),
                    end = Offset(chartWidth, ySl),
                    strokeWidth = 1.8.dp.toPx(),
                    pathEffect = slDash
                )

                // SL Label Badge
                drawContext.canvas.nativeCanvas.apply {
                    val slBadgePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(255, 82, 82)
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val slTextPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 19f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val isBe = order.riskStopPips <= 1.0
                    val slText = if (isBe) "SL BREAKEVEN +%.1fp (%.${symbol.digits}f)".format(order.riskStopPips, order.stopLossPrice)
                    else "SL -%.1f PIPS (%.${symbol.digits}f)".format(order.riskStopPips, order.stopLossPrice)
                    val slWidth = slTextPaint.measureText(slText)
                    val rect = android.graphics.RectF(chartWidth - slWidth - 20f, ySl - 16f, chartWidth - 6f, ySl + 4f)
                    drawRoundRect(rect, 4f, 4f, slBadgePaint)
                    drawText(slText, chartWidth - slWidth - 14f, ySl - 2f, slTextPaint)
                }
            }

            // ==========================================
            // SUB-PANEL 1: RSI OSCILLATOR (14)
            // ==========================================
            var currentPanelTop = mainChartHeight + 4.dp.toPx()

            if (indicatorConfig.showRsi) {
                val rsiTop = currentPanelTop
                val rsiBottom = rsiTop + rsiHeight - 6.dp.toPx()
                currentPanelTop = rsiBottom + 6.dp.toPx()

                // Divider line
                drawLine(
                    color = ArtisticBorder,
                    start = Offset(0f, rsiTop - 2f),
                    end = Offset(canvasWidth, rsiTop - 2f),
                    strokeWidth = 1.dp.toPx()
                )

                // RSI Levels (70 Overbought, 50 Mid, 30 Oversold)
                fun rsiToY(rsiVal: Float): Float {
                    val clamped = rsiVal.coerceIn(0f, 100f)
                    return rsiBottom - ((clamped / 100f) * (rsiBottom - rsiTop))
                }

                val y70 = rsiToY(70f)
                val y50 = rsiToY(50f)
                val y30 = rsiToY(30f)

                // Boundary lines
                drawLine(color = ArtisticRed.copy(alpha = 0.5f), start = Offset(0f, y70), end = Offset(chartWidth, y70), strokeWidth = 1f, pathEffect = gridDash)
                drawLine(color = ArtisticBorder.copy(alpha = 0.4f), start = Offset(0f, y50), end = Offset(chartWidth, y50), strokeWidth = 1f, pathEffect = gridDash)
                drawLine(color = ArtisticGreen.copy(alpha = 0.5f), start = Offset(0f, y30), end = Offset(chartWidth, y30), strokeWidth = 1f, pathEffect = gridDash)

                // Calculate and draw RSI curve
                val rsiValues = calculateRsi(candles, indicatorConfig.rsiPeriod)
                val rsiPath = Path()
                var rsiStarted = false

                candles.indices.forEach { index ->
                    val rsiVal = rsiValues[index]
                    val x = (index * candleSpacing) + (candleSpacing / 2f)
                    val y = rsiToY(rsiVal)
                    if (!rsiStarted) {
                        rsiPath.moveTo(x, y)
                        rsiStarted = true
                    } else {
                        rsiPath.lineTo(x, y)
                    }
                }
                drawPath(path = rsiPath, color = Color(0xFF7C4DFF), style = Stroke(width = 1.6.dp.toPx()))

                // Draw RSI labels
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(124, 77, 255)
                        textSize = 20f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val currentRsi = rsiValues.last()
                    val rsiTag = "RSI(14): %.1f".format(currentRsi)
                    drawText(rsiTag, 10f, rsiTop + 16f, textPaint)

                    val boundPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 150, 150, 160)
                        textSize = 18f
                        isAntiAlias = true
                    }
                    drawText("70", chartWidth + 8f, y70 + 6f, boundPaint)
                    drawText("30", chartWidth + 8f, y30 + 6f, boundPaint)
                }
            }

            // ==========================================
            // SUB-PANEL 2: MACD (12, 26, 9)
            // ==========================================
            if (indicatorConfig.showMacd) {
                val macdTop = currentPanelTop
                val macdBottom = macdTop + macdHeight - 6.dp.toPx()

                // Divider line
                drawLine(
                    color = ArtisticBorder,
                    start = Offset(0f, macdTop - 2f),
                    end = Offset(canvasWidth, macdTop - 2f),
                    strokeWidth = 1.dp.toPx()
                )

                val (macdLine, signalLine, hist) = calculateMacd(candles)
                val maxAbsHist = max(0.0001f, hist.maxOfOrNull { abs(it) } ?: 0.001f)
                val macdMidY = (macdTop + macdBottom) / 2f

                // Zero line
                drawLine(color = ArtisticBorder.copy(alpha = 0.5f), start = Offset(0f, macdMidY), end = Offset(chartWidth, macdMidY), strokeWidth = 1f)

                // Draw histogram bars
                val barWidth = max(1.5f, candleWidth * 0.7f)
                candles.indices.forEach { index ->
                    val xCenter = (index * candleSpacing) + (candleSpacing / 2f)
                    val hVal = hist[index]
                    val barHeight = ((hVal / maxAbsHist) * ((macdBottom - macdTop) / 2f)).coerceIn(-30f, 30f)
                    val barColor = if (hVal >= 0) ArtisticGreen.copy(alpha = 0.8f) else ArtisticRed.copy(alpha = 0.8f)

                    if (hVal >= 0) {
                        drawRect(color = barColor, topLeft = Offset(xCenter - (barWidth / 2f), macdMidY - barHeight), size = Size(barWidth, barHeight))
                    } else {
                        drawRect(color = barColor, topLeft = Offset(xCenter - (barWidth / 2f), macdMidY), size = Size(barWidth, -barHeight))
                    }
                }

                // Draw MACD Line (Blue) & Signal Line (Orange)
                val macdPath = Path()
                val signalPath = Path()
                var started = false

                candles.indices.forEach { index ->
                    val x = (index * candleSpacing) + (candleSpacing / 2f)
                    val mVal = ((macdLine[index] / maxAbsHist) * ((macdBottom - macdTop) / 2f)).coerceIn(-30f, 30f)
                    val sVal = ((signalLine[index] / maxAbsHist) * ((macdBottom - macdTop) / 2f)).coerceIn(-30f, 30f)

                    val yM = macdMidY - mVal
                    val yS = macdMidY - sVal

                    if (!started) {
                        macdPath.moveTo(x, yM)
                        signalPath.moveTo(x, yS)
                        started = true
                    } else {
                        macdPath.lineTo(x, yM)
                        signalPath.lineTo(x, yS)
                    }
                }

                drawPath(path = macdPath, color = Color(0xFF2979FF), style = Stroke(width = 1.6.dp.toPx()))
                drawPath(path = signalPath, color = Color(0xFFFF6D00), style = Stroke(width = 1.4.dp.toPx()))

                // MACD Legend
                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        textSize = 19f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    textPaint.color = android.graphics.Color.rgb(41, 121, 255)
                    drawText("MACD(12,26,9)", 10f, macdTop + 16f, textPaint)
                }
            }
        }
    }
}

// Indicator Calculation Helpers
private fun calculateMovingAverage(candles: List<Candle>, period: Int): List<Float> {
    val result = mutableListOf<Float>()
    for (i in candles.indices) {
        val start = max(0, i - period + 1)
        val sub = candles.subList(start, i + 1)
        val avg = sub.map { it.close }.average().toFloat()
        result.add(avg)
    }
    return result
}

private fun calculateRsi(candles: List<Candle>, period: Int): List<Float> {
    if (candles.size < 2) return List(candles.size) { 50f }
    val result = mutableListOf<Float>()
    var avgGain = 0.0
    var avgLoss = 0.0

    for (i in candles.indices) {
        if (i == 0) {
            result.add(50f)
            continue
        }
        val change = candles[i].close - candles[i - 1].close
        val gain = if (change > 0) change else 0.0
        val loss = if (change < 0) -change else 0.0

        if (i <= period) {
            avgGain += gain / period
            avgLoss += loss / period
            if (i == period) {
                val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
                val rsi = (100.0 - (100.0 / (1.0 + rs))).toFloat()
                result.add(rsi.coerceIn(0f, 100f))
            } else {
                result.add(50f)
            }
        } else {
            avgGain = ((avgGain * (period - 1)) + gain) / period
            avgLoss = ((avgLoss * (period - 1)) + loss) / period
            val rs = if (avgLoss == 0.0) 100.0 else avgGain / avgLoss
            val rsi = (100.0 - (100.0 / (1.0 + rs))).toFloat()
            result.add(rsi.coerceIn(0f, 100f))
        }
    }
    return result
}

private fun calculateMacd(candles: List<Candle>): Triple<List<Float>, List<Float>, List<Float>> {
    val closes = candles.map { it.close.toFloat() }
    val ema12 = calculateEma(closes, 12)
    val ema26 = calculateEma(closes, 26)
    val macd = ema12.indices.map { ema12[it] - ema26[it] }
    val signal = calculateEma(macd, 9)
    val hist = macd.indices.map { macd[it] - signal[it] }
    return Triple(macd, signal, hist)
}

private fun calculateEma(values: List<Float>, period: Int): List<Float> {
    if (values.isEmpty()) return emptyList()
    val k = 2f / (period + 1)
    val emaList = mutableListOf<Float>()
    var currentEma = values[0]
    emaList.add(currentEma)
    for (i in 1 until values.size) {
        currentEma = (values[i] * k) + (currentEma * (1f - k))
        emaList.add(currentEma)
    }
    return emaList
}
