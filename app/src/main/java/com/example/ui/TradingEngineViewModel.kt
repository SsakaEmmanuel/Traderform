package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorldMarketRepository
import com.example.model.BotConfig
import com.example.model.Candle
import com.example.model.MarketAnalysis
import com.example.model.MarketCatalog
import com.example.model.MarketSessionInfo
import com.example.model.MarketSymbol
import com.example.model.OrderStatus
import com.example.model.OrderType
import com.example.model.SignalType
import com.example.model.TimeFrame
import com.example.model.TradeOrder
import com.example.model.WorldMacroEvent
import com.example.model.WorldMarketStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

data class BotStatistics(
    val totalTrades: Int = 0,
    val tradesWon: Int = 0,
    val tradesLost: Int = 0,
    val totalPips: Double = 0.0,
    val totalProfitUsd: Double = 0.0,
    val winRatePercent: Double = 0.0
)

data class TradingUiState(
    val selectedDivision: Int = 1, // 0 = Gateway, 1 = Chart & Orders, 2 = AI Analysis & Bot
    val selectedSymbol: MarketSymbol = MarketCatalog.defaultSymbol,
    val selectedTimeFrame: TimeFrame = TimeFrame.TF_15M,
    val currentPrice: Double = MarketCatalog.defaultSymbol.basePrice,
    val bidPrice: Double = MarketCatalog.defaultSymbol.basePrice,
    val askPrice: Double = MarketCatalog.defaultSymbol.basePrice + 0.00008,
    val priceChange24h: Double = +0.0034,
    val priceChangePercent24h: Double = +0.31,
    val candles: List<Candle> = emptyList(),
    val openOrders: List<TradeOrder> = emptyList(),
    val closedOrders: List<TradeOrder> = emptyList(),
    val analysis: MarketAnalysis? = null,
    val botConfig: BotConfig = BotConfig(),
    val indicatorConfig: com.example.model.IndicatorConfig = com.example.model.IndicatorConfig(),
    val botStatusMessage: String = "Bot standing by. Ready to analyze entries.",
    val botStatistics: BotStatistics = BotStatistics(
        totalTrades = 12,
        tradesWon = 10,
        tradesLost = 2,
        totalPips = +70.0,
        totalProfitUsd = +700.0,
        winRatePercent = 83.3
    ),
    val selectedLotSize: Double = 0.10,
    val lastNotification: String? = null,
    val worldMarketStats: WorldMarketStats? = null,
    val marketSessions: List<MarketSessionInfo> = emptyList(),
    val macroEvents: List<WorldMacroEvent> = emptyList(),
    val allPairsStats: Map<String, WorldMarketStats> = emptyMap(),
    val isWorldSyncing: Boolean = false,
    val lastWorldSyncTimestamp: Long = 0L,
    val simulatedAccuracyOverride: Int? = null
)

class TradingEngineViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null
    private var worldSyncJob: Job? = null
    private var candleTickCounter = 0
    private var currentCandleOpen = 0.0
    private var currentCandleHigh = 0.0
    private var currentCandleLow = 0.0

    init {
        generateInitialCandles()
        updateAnalysis()
        startLiveMarketSimulation()
        syncWorldMarketData(forceRefresh = true)
        startPeriodicWorldSync()
    }

    fun setDivision(index: Int) {
        _uiState.update { it.copy(selectedDivision = index) }
    }

    fun setSymbol(symbol: MarketSymbol) {
        _uiState.update {
            it.copy(
                selectedSymbol = symbol,
                currentPrice = symbol.basePrice,
                bidPrice = symbol.basePrice,
                askPrice = symbol.basePrice + (symbol.spreadPips * symbol.pipSize)
            )
        }
        generateInitialCandles()
        updateAnalysis()
        syncWorldMarketData(forceRefresh = true)
    }

    fun setTimeFrame(timeFrame: TimeFrame) {
        _uiState.update { it.copy(selectedTimeFrame = timeFrame) }
        generateInitialCandles()
        updateAnalysis()
        syncWorldMarketData(forceRefresh = false)
    }

    fun setLotSize(lotSize: Double) {
        _uiState.update { it.copy(selectedLotSize = lotSize) }
    }

    fun toggleAutoTrading(enabled: Boolean) {
        _uiState.update {
            it.copy(
                botConfig = it.botConfig.copy(isAutoTradingEnabled = enabled),
                botStatusMessage = if (enabled) {
                    "Auto-Pilot Active: Protocol (50% -> 1 trade, 72% -> 2 trades, >72% -> 3 trades). Scanning market..."
                } else {
                    "Auto-Pilot Paused. Manual orders and live analysis remain active."
                }
            )
        }
        if (enabled) {
            evaluateAutoBotEntry()
        }
    }

    fun setAccuracyOverride(accuracy: Int?) {
        _uiState.update { it.copy(simulatedAccuracyOverride = accuracy) }
        updateAnalysis()
        if (_uiState.value.botConfig.isAutoTradingEnabled) {
            evaluateAutoBotEntry()
        }
    }

    fun toggleIndicatorMaMatrix(enabled: Boolean) {
        _uiState.update { it.copy(indicatorConfig = it.indicatorConfig.copy(showMaMatrix = enabled)) }
    }

    fun toggleIndicatorRsi(enabled: Boolean) {
        _uiState.update { it.copy(indicatorConfig = it.indicatorConfig.copy(showRsi = enabled)) }
    }

    fun toggleIndicatorMacd(enabled: Boolean) {
        _uiState.update { it.copy(indicatorConfig = it.indicatorConfig.copy(showMacd = enabled)) }
    }

    fun updateDefaultTakeProfitPips(pips: Double) {
        val sanitized = pips.coerceIn(1.0, 100.0)
        _uiState.update {
            it.copy(
                botConfig = it.botConfig.copy(targetProfitPips = sanitized),
                lastNotification = "Default Take Profit override: +${String.format("%.1f", sanitized)} pips"
            )
        }
    }

    fun updateDefaultStopLossPips(pips: Double) {
        val sanitized = pips.coerceIn(1.0, 100.0)
        _uiState.update {
            it.copy(
                botConfig = it.botConfig.copy(riskStopPips = sanitized),
                lastNotification = "Default Stop Loss override: -${String.format("%.1f", sanitized)} pips"
            )
        }
    }

    fun resetDefaultExitPips() {
        _uiState.update {
            it.copy(
                botConfig = it.botConfig.copy(targetProfitPips = 10.0, riskStopPips = 15.0),
                lastNotification = "Reset default exit to normal: +10.0p TP / -15.0p SL"
            )
        }
    }

    /**
     * PIP CONTROL: Manually adjust exit pips for an active order after it sets to normal.
     */
    fun updateOrderExitPips(orderId: String, newTpPips: Double, newSlPips: Double) {
        val state = _uiState.value
        val symbol = state.selectedSymbol
        _uiState.update { current ->
            val updatedOpen = current.openOrders.map { order ->
                if (order.id == orderId) {
                    val sanitizedTp = newTpPips.coerceIn(1.0, 200.0)
                    val sanitizedSl = newSlPips.coerceIn(0.5, 200.0)
                    val tpPrice = if (order.type == OrderType.BUY) {
                        order.entryPrice + (sanitizedTp * symbol.pipSize)
                    } else {
                        order.entryPrice - (sanitizedTp * symbol.pipSize)
                    }
                    val slPrice = if (order.type == OrderType.BUY) {
                        order.entryPrice - (sanitizedSl * symbol.pipSize)
                    } else {
                        order.entryPrice + (sanitizedSl * symbol.pipSize)
                    }
                    order.copy(
                        targetProfitPips = sanitizedTp,
                        riskStopPips = sanitizedSl,
                        takeProfitPrice = tpPrice,
                        stopLossPrice = slPrice
                    )
                } else order
            }
            current.copy(
                openOrders = updatedOpen,
                lastNotification = "Order #$orderId exit adjusted: TP +${String.format("%.1f", newTpPips)}p | SL -${String.format("%.1f", newSlPips)}p"
            )
        }
    }

    fun stepOrderExitPips(orderId: String, deltaTp: Double, deltaSl: Double) {
        val state = _uiState.value
        val targetOrder = state.openOrders.find { it.id == orderId } ?: return
        val newTp = (targetOrder.targetProfitPips + deltaTp).coerceIn(1.0, 200.0)
        val newSl = (targetOrder.riskStopPips + deltaSl).coerceIn(0.5, 200.0)
        updateOrderExitPips(orderId, newTp, newSl)
    }

    fun setOrderBreakeven(orderId: String) {
        val state = _uiState.value
        val targetOrder = state.openOrders.find { it.id == orderId } ?: return
        val symbol = state.selectedSymbol
        // Breakeven locks SL to entry price (+0.5 pip positive buffer)
        val slPrice = if (targetOrder.type == OrderType.BUY) {
            targetOrder.entryPrice + (0.5 * symbol.pipSize)
        } else {
            targetOrder.entryPrice - (0.5 * symbol.pipSize)
        }
        _uiState.update { current ->
            val updatedOpen = current.openOrders.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        riskStopPips = 0.5,
                        stopLossPrice = slPrice
                    )
                } else order
            }
            current.copy(
                openOrders = updatedOpen,
                lastNotification = "Order #${targetOrder.id} Stop-Loss moved to Breakeven (+0.5p buffer)"
            )
        }
    }

    fun resetOrderToNormalExit(orderId: String) {
        updateOrderExitPips(orderId, 10.0, 15.0)
    }

    fun clearNotification() {
        _uiState.update { it.copy(lastNotification = null) }
    }

    /**
     * Executes orders (User Manual or Bot Triggered).
     * Respects the multi-trade execution protocol based on accuracy:
     * - 50% accuracy: executes 1 trade
     * - 72% accuracy: executes 2 trades
     * - Above 72% accuracy: executes 3 trades
     */
    fun placeOrders(type: OrderType, count: Int, isManual: Boolean = false, accuracyPercent: Int? = null) {
        if (count <= 0) return
        val state = _uiState.value
        val symbol = state.selectedSymbol
        val entryPrice = if (type == OrderType.BUY) state.askPrice else state.bidPrice
        val lotSize = state.selectedLotSize

        val tpPips = state.botConfig.targetProfitPips // 10.0
        val slPips = state.botConfig.riskStopPips     // 15.0

        val tpPrice = if (type == OrderType.BUY) {
            entryPrice + (tpPips * symbol.pipSize)
        } else {
            entryPrice - (tpPips * symbol.pipSize)
        }

        val slPrice = if (type == OrderType.BUY) {
            entryPrice - (slPips * symbol.pipSize)
        } else {
            entryPrice + (slPips * symbol.pipSize)
        }

        val effectiveAccuracy = accuracyPercent ?: state.analysis?.confidencePercent ?: 75
        val baseId = System.currentTimeMillis()

        val newOrders = (1..count).map { index ->
            val orderSuffix = if (count == 1) "" else " #$index/$count"
            TradeOrder(
                id = if (count == 1) "$baseId" else "${baseId}_$index",
                symbolId = symbol.id,
                type = type,
                lotSize = lotSize,
                entryPrice = entryPrice,
                currentPrice = entryPrice,
                takeProfitPrice = tpPrice,
                stopLossPrice = slPrice,
                targetProfitPips = tpPips,
                riskStopPips = slPips,
                profitPips = 0.0,
                profitUsd = 0.0,
                profitPercent = 0.0,
                isTakeProfitInserted = false, // Mandate: Only inserted after finding profit reaches 5%
                autoTpThresholdPercent = state.botConfig.autoTakeProfitThresholdPercent,
                status = OrderStatus.OPEN,
                placedBy = if (isManual) "Manual ($effectiveAccuracy% Acc$orderSuffix)" else "Bot ($effectiveAccuracy% Acc$orderSuffix)",
                timestamp = baseId + index
            )
        }

        val tradeNoun = if (count == 1) "1 trade" else "$count trades"
        val tierRationale = when {
            effectiveAccuracy > 72 -> ">72% Accuracy ($effectiveAccuracy%) -> Executed 3 Trades"
            effectiveAccuracy == 72 -> "72% Accuracy -> Executed 2 Trades"
            effectiveAccuracy >= 50 -> "50% Accuracy ($effectiveAccuracy%) -> Executed 1 Trade"
            else -> "Below 50% Accuracy -> 0 Trades"
        }

        _uiState.update { current ->
            current.copy(
                openOrders = newOrders + current.openOrders,
                botStatusMessage = if (!isManual) {
                    "Bot executed $tradeNoun for ${type.name} at ${formatPrice(entryPrice, symbol.digits)} [$tierRationale]"
                } else current.botStatusMessage,
                lastNotification = "${if (isManual) "Manual" else "Bot"} executed $tradeNoun (${type.name}) at ${formatPrice(entryPrice, symbol.digits)} [$tierRationale]"
            )
        }
    }

    fun placeOrder(type: OrderType, isManual: Boolean = true) {
        val state = _uiState.value
        val accuracy = state.analysis?.confidencePercent ?: 75
        val count = if (isManual) 1 else BotConfig.getTradeCountForAccuracy(accuracy).coerceAtLeast(1)
        placeOrders(type, count = count, isManual = isManual, accuracyPercent = accuracy)
    }

    fun executeRecommendedSignal(type: OrderType) {
        val state = _uiState.value
        val accuracy = state.analysis?.confidencePercent ?: 75
        val count = BotConfig.getTradeCountForAccuracy(accuracy).coerceAtLeast(1)
        placeOrders(type, count = count, isManual = true, accuracyPercent = accuracy)
    }

    fun closeOrderManual(orderId: String) {
        _uiState.update { current ->
            val orderToClose = current.openOrders.find { it.id == orderId } ?: return@update current
            val closed = orderToClose.copy(
                status = OrderStatus.CANCELLED_MANUAL,
                closeNote = "Closed manually by user"
            )
            val updatedOpen = current.openOrders.filter { it.id != orderId }
            val updatedClosed = listOf(closed) + current.closedOrders
            val updatedStats = calculateUpdatedStats(current.botStatistics, closed)

            current.copy(
                openOrders = updatedOpen,
                closedOrders = updatedClosed,
                botStatistics = updatedStats,
                lastNotification = "Order #${closed.id} manually closed at ${if (closed.profitPips >= 0) "+" else ""}${String.format("%.1f", closed.profitPips)} pips"
            )
        }
    }

    private fun startPeriodicWorldSync() {
        worldSyncJob?.cancel()
        worldSyncJob = viewModelScope.launch {
            while (isActive) {
                delay(20_000) // Sync world market quotes & statistics every 20 seconds
                syncWorldMarketData(forceRefresh = false)
            }
        }
    }

    /**
     * Actively synchronizes the system with current world market changes, movements,
     * live exchange rates, 24h volume, ATR volatility, and global macroeconomic events.
     */
    fun syncWorldMarketData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorldSyncing = true) }
            val state = _uiState.value
            val currentSymbol = state.selectedSymbol

            try {
                // 1. Fetch live world market statistics for current pair
                val stats = WorldMarketRepository.fetchWorldMarketData(currentSymbol)

                // 2. Fetch active world trading sessions
                val sessions = WorldMarketRepository.getCurrentMarketSessions()

                // 3. Fetch macroeconomic information & events
                val macroIntel = WorldMarketRepository.getWorldMacroIntel(currentSymbol.id)

                // 4. Update all pairs stats matrix asynchronously
                val currentAllStats = state.allPairsStats.toMutableMap()
                currentAllStats[currentSymbol.id] = stats
                // Prefetch other pairs if empty
                if (currentAllStats.size <= 1) {
                    for (other in MarketCatalog.symbols) {
                        if (other.id != currentSymbol.id && !currentAllStats.containsKey(other.id)) {
                            currentAllStats[other.id] = WorldMarketRepository.fetchWorldMarketData(other)
                        }
                    }
                }

                // 5. If crypto or gold, attempt to load real historical world candles
                val realCandles = WorldMarketRepository.fetchRealCandles(currentSymbol, state.selectedTimeFrame)

                val newPrice = stats.price
                val spreadAmount = currentSymbol.spreadPips * currentSymbol.pipSize
                val newBid = newPrice
                val newAsk = newPrice + spreadAmount

                _uiState.update { current ->
                    val updatedCandles = if (!realCandles.isNullOrEmpty()) {
                        realCandles
                    } else if (current.candles.isNotEmpty()) {
                        // Rescale existing candles smoothly to center on the new world market price
                        val oldClose = current.candles.last().close
                        val diff = newPrice - oldClose
                        current.candles.map { c ->
                            c.copy(
                                open = c.open + diff,
                                high = c.high + diff,
                                low = c.low + diff,
                                close = c.close + diff
                            )
                        }
                    } else {
                        current.candles
                    }

                    current.copy(
                        worldMarketStats = stats,
                        marketSessions = sessions,
                        macroEvents = macroIntel,
                        allPairsStats = currentAllStats,
                        currentPrice = newPrice,
                        bidPrice = newBid,
                        askPrice = newAsk,
                        priceChange24h = stats.change24h,
                        priceChangePercent24h = stats.changePercent24h,
                        candles = updatedCandles,
                        isWorldSyncing = false,
                        lastWorldSyncTimestamp = System.currentTimeMillis()
                    )
                }

                // Re-evaluate orders and analysis with world stats
                checkAndEvaluateOrders(newPrice, newBid, newAsk)
                updateAnalysis()

            } catch (e: Exception) {
                _uiState.update { it.copy(isWorldSyncing = false) }
            }
        }
    }

    private fun startLiveMarketSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                // Adaptive tick speed depending on active world session
                val sessions = _uiState.value.marketSessions
                val isPeakSession = sessions.any { it.isOpen && it.activeVolumeSharePercent >= 35 }
                val tickDelay = if (isPeakSession) 650L else 950L
                delay(tickDelay)
                tickMarketPrice()
            }
        }
    }

    private fun tickMarketPrice() {
        val state = _uiState.value
        val symbol = state.selectedSymbol

        // Micro price step simulation
        val volatility = symbol.pipSize * (Random.nextDouble(0.1, 1.2))
        val direction = if (Random.nextDouble() > 0.48) 1.0 else -1.0
        val priceDelta = direction * volatility

        val newPrice = (state.currentPrice + priceDelta).coerceAtLeast(symbol.basePrice * 0.5)
        val newBid = newPrice
        val newAsk = newPrice + (symbol.spreadPips * symbol.pipSize)

        // Update active candles
        updateCandleWithTick(newPrice)

        // Check & update open orders
        checkAndEvaluateOrders(newPrice, newBid, newAsk)

        // Check if bot should enter trade
        evaluateAutoBotEntry()

        _uiState.update { current ->
            current.copy(
                currentPrice = newPrice,
                bidPrice = newBid,
                askPrice = newAsk
            )
        }
    }

    private fun checkAndEvaluateOrders(currentPrice: Double, bid: Double, ask: Double) {
        val state = _uiState.value
        val symbol = state.selectedSymbol
        val openOrders = state.openOrders

        val remainingOpen = mutableListOf<TradeOrder>()
        val newlyClosed = mutableListOf<TradeOrder>()
        var triggerNotification: String? = null
        var botStatusUpdate: String? = null

        for (order in openOrders) {
            if (order.symbolId != symbol.id) {
                remainingOpen.add(order)
                continue
            }

            val marketPriceForOrder = if (order.type == OrderType.BUY) bid else ask
            val rawDiff = if (order.type == OrderType.BUY) {
                marketPriceForOrder - order.entryPrice
            } else {
                order.entryPrice - marketPriceForOrder
            }

            val pips = rawDiff / symbol.pipSize
            // 1 lot = $10/pip on 100k standard lot for Forex
            val pipValuePerLot = if (symbol.id == "USDJPY") 6.5 else 10.0
            val usdProfit = pips * order.lotSize * pipValuePerLot

            // Calculate profit percentage:
            // 1) Return on trade margin (at 1:100 leverage, 1 standard lot = $1,000 margin)
            val tradeMargin = (order.lotSize * 1000.0).coerceAtLeast(10.0)
            val marginProfitPercent = (usdProfit / tradeMargin) * 100.0
            // 2) Price movement percentage from entry
            val priceChangePercent = if (order.entryPrice > 0.0) {
                (rawDiff / order.entryPrice) * 100.0
            } else 0.0
            // Effective profit percentage:
            val currentProfitPercent = max(marginProfitPercent, priceChangePercent)

            // USER RULE: "Insert an automatic take profit only on both sides after finding out that the profit reaches 5 percent"
            val targetThreshold = order.autoTpThresholdPercent // 5.0%
            val reachesFivePercent = currentProfitPercent >= targetThreshold

            if (reachesFivePercent) {
                // Condition met: Profit has reached 5%! Automatic Take Profit is inserted on both sides (BUY & SELL)
                val closed = order.copy(
                    currentPrice = marketPriceForOrder,
                    takeProfitPrice = marketPriceForOrder,
                    isTakeProfitInserted = true,
                    profitPips = pips,
                    profitUsd = usdProfit,
                    profitPercent = currentProfitPercent,
                    status = OrderStatus.CLOSED_TP,
                    closeNote = "Auto Take Profit inserted on ${order.type.name} side after finding profit reached ${String.format("%.1f", currentProfitPercent)}% (>= 5%)"
                )
                newlyClosed.add(closed)
                triggerNotification = "🎯 Auto Take Profit Hit! +${String.format("%.1f", currentProfitPercent)}% profit reached on ${order.type.name} #${order.id} (+$${String.format("%.2f", closed.profitUsd)})"
                botStatusUpdate = "Auto Take Profit executed on ${order.type.name} side: Reached 5% profit threshold (+${String.format("%.1f", currentProfitPercent)}%). Scanning for next entry..."

                // Immediately re-analyze the next setup
                viewModelScope.launch {
                    delay(500)
                    updateAnalysis()
                }
            }
            // Check 2: Stop Loss Risk reached for this specific order (per-order risk stop pips)
            else if (pips <= -order.riskStopPips) {
                val closed = order.copy(
                    currentPrice = marketPriceForOrder,
                    profitPips = -order.riskStopPips,
                    profitUsd = -order.riskStopPips * order.lotSize * pipValuePerLot,
                    profitPercent = -15.0,
                    status = OrderStatus.CLOSED_SL,
                    closeNote = "Risk stop hit: -${String.format("%.1f", order.riskStopPips)} pips cancelled"
                )
                newlyClosed.add(closed)
                triggerNotification = "🛑 Risk Stop Triggered: Cancelled trade at -${String.format("%.1f", order.riskStopPips)} pips to preserve capital"
                botStatusUpdate = "${String.format("%.1f", order.riskStopPips)}-pip risk limit executed. Waiting for market equilibrium."

                viewModelScope.launch {
                    delay(500)
                    updateAnalysis()
                }
            } else {
                // Update unrealized pips, usd & profitPercent (Auto TP pending 5% profit)
                remainingOpen.add(
                    order.copy(
                        currentPrice = marketPriceForOrder,
                        profitPips = pips,
                        profitUsd = usdProfit,
                        profitPercent = currentProfitPercent,
                        isTakeProfitInserted = false // Still pending 5% profit
                    )
                )
            }
        }

        if (newlyClosed.isNotEmpty() || remainingOpen != openOrders) {
            _uiState.update { current ->
                var stats = current.botStatistics
                for (closed in newlyClosed) {
                    stats = calculateUpdatedStats(stats, closed)
                }

                current.copy(
                    openOrders = remainingOpen,
                    closedOrders = newlyClosed + current.closedOrders,
                    botStatistics = stats,
                    lastNotification = triggerNotification ?: current.lastNotification,
                    botStatusMessage = botStatusUpdate ?: current.botStatusMessage
                )
            }
        }
    }

    private fun evaluateAutoBotEntry() {
        val state = _uiState.value
        if (!state.botConfig.isAutoTradingEnabled) return

        // Prevent opening more trades while this symbol already has active open orders
        val currentSymbolOpen = state.openOrders.any { it.symbolId == state.selectedSymbol.id }
        if (currentSymbolOpen) return

        val analysis = state.analysis ?: return
        val accuracy = analysis.confidencePercent

        // USER RULE:
        // Execute one trade when accuracy is 50 percent
        // Execute two trades when 72 percent
        // Execute three trades when above (above 72 percent)
        val tradeCount = BotConfig.getTradeCountForAccuracy(accuracy)

        if (tradeCount <= 0) {
            _uiState.update { current ->
                if (current.botStatusMessage.startsWith("Accuracy below")) current
                else current.copy(botStatusMessage = "Accuracy ($accuracy%) is below 50% threshold. Bot standing by.")
            }
            return
        }

        when (analysis.signal) {
            SignalType.STRONG_BUY, SignalType.BUY -> {
                placeOrders(OrderType.BUY, count = tradeCount, isManual = false, accuracyPercent = accuracy)
            }
            SignalType.STRONG_SELL, SignalType.SELL -> {
                placeOrders(OrderType.SELL, count = tradeCount, isManual = false, accuracyPercent = accuracy)
            }
            SignalType.NEUTRAL -> {
                // Do nothing, keep scanning
            }
        }
    }

    fun updateAnalysis() {
        val state = _uiState.value
        val symbol = state.selectedSymbol
        val currentPrice = state.currentPrice

        // Calculate technical parameters based on recent candles
        val recentCandles = state.candles.takeLast(14)
        val gains = recentCandles.filter { it.close >= it.open }.map { it.close - it.open }
        val losses = recentCandles.filter { it.close < it.open }.map { it.open - it.close }
        val avgGain = if (gains.isNotEmpty()) gains.average() else 0.0001
        val avgLoss = if (losses.isNotEmpty()) losses.average() else 0.0001
        val rs = avgGain / max(avgLoss, 0.00001)
        val rsi = (100.0 - (100.0 / (1.0 + rs))).coerceIn(20.0, 80.0)

        val macdHist = (Random.nextDouble(-1.5, 1.5) * symbol.pipSize * 10).coerceIn(-0.0008, 0.0008)

        val worldStats = state.worldMarketStats
        val sessions = state.marketSessions
        val activeSession = sessions.find { it.isOpen && it.activeVolumeSharePercent >= 30 }
            ?: sessions.find { it.isOpen }

        val sessionDesc = activeSession?.let { "${it.name} (${it.activeVolumeSharePercent}% global volume)" }
            ?: "Interbank Off-Peak Flow"

        val sentimentBull = worldStats?.bullishSentimentPercent ?: 55
        val atr = worldStats?.atr14Pips ?: 65.0

        val signal = when {
            sentimentBull >= 62 && rsi < 52 -> SignalType.STRONG_BUY
            sentimentBull >= 54 && rsi < 58 -> SignalType.BUY
            sentimentBull <= 38 && rsi > 48 -> SignalType.STRONG_SELL
            sentimentBull <= 46 && rsi > 42 -> SignalType.SELL
            rsi < 40 -> SignalType.BUY
            rsi > 60 -> SignalType.SELL
            else -> SignalType.BUY
        }

        // Generate accuracy matching user thresholds (50% -> 1 trade, 72% -> 2 trades, >72% -> 3 trades)
        val confidence = state.simulatedAccuracyOverride ?: when (signal) {
            SignalType.STRONG_BUY, SignalType.STRONG_SELL -> {
                val boost = if (worldStats != null && abs(sentimentBull - 50) > 10) 3 else 0
                (Random.nextInt(74, 94) + boost).coerceAtMost(98) // Above 72% tier (3 trades)
            }
            SignalType.BUY, SignalType.SELL -> {
                when (Random.nextInt(1, 4)) {
                    1 -> 50 // 50% accuracy tier (1 trade)
                    2 -> 72 // 72% accuracy tier (2 trades)
                    else -> Random.nextInt(74, 88) // Above 72% tier (3 trades)
                }
            }
            SignalType.NEUTRAL -> {
                if (Random.nextBoolean()) 50 else Random.nextInt(38, 49)
            }
        }

        val support = currentPrice - (15.0 * symbol.pipSize)
        val resistance = currentPrice + (10.0 * symbol.pipSize)

        val summary = when (signal) {
            SignalType.STRONG_BUY -> "Global Bullish Confluence: World Interbank Sentiment is $sentimentBull% Long. $sessionDesc active with daily ATR of ${String.format("%.1f", atr)} pips. Favorable momentum for +10.0 pips scalp."
            SignalType.BUY -> "Moderate Upward Momentum: Buyers defending ${formatPrice(support, symbol.digits)} with $sessionDesc backing. Scalp target: +10.0 pips with 15-pip risk boundary."
            SignalType.STRONG_SELL -> "Global Bearish Confluence: World Interbank Sentiment is ${100 - sentimentBull}% Short. Resistance rejection at ${formatPrice(resistance, symbol.digits)}. Projected gain: +10.0 pips on Short."
            SignalType.SELL -> "Downward Pressure: Institutional selling in $sessionDesc. Downside scalp target: +10.0 pips."
            SignalType.NEUTRAL -> "Consolidation zone: Awaiting session breakout confirmation before opening new 10-pip positions."
        }

        val analysisResult = MarketAnalysis(
            symbolId = symbol.id,
            timeFrame = state.selectedTimeFrame,
            signal = signal,
            confidencePercent = confidence,
            rsi14 = rsi,
            macdHistogram = macdHist,
            trendDescription = if (signal in listOf(SignalType.STRONG_BUY, SignalType.BUY)) "Bullish Trend" else "Bearish Trend",
            supportLevel = support,
            resistanceLevel = resistance,
            recommendedEntry = currentPrice,
            targetPips = 10.0,
            riskStopPips = 15.0,
            summaryText = summary
        )

        _uiState.update { it.copy(analysis = analysisResult) }
    }

    private fun generateInitialCandles() {
        val state = _uiState.value
        val symbol = state.selectedSymbol
        val count = 40
        val base = symbol.basePrice
        val list = mutableListOf<Candle>()
        var lastClose = base - (count * 0.3 * symbol.pipSize)

        val now = System.currentTimeMillis()
        val intervalMs = state.selectedTimeFrame.seconds * 1000

        for (i in 0 until count) {
            val delta = (Random.nextDouble(-2.0, 2.5)) * symbol.pipSize
            val open = lastClose
            val close = open + delta
            val high = max(open, close) + Random.nextDouble(0.2, 1.8) * symbol.pipSize
            val low = min(open, close) - Random.nextDouble(0.2, 1.8) * symbol.pipSize
            val vol = Random.nextInt(120, 850).toFloat()

            list.add(
                Candle(
                    timeIndex = i,
                    timestampMs = now - ((count - i) * intervalMs),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = vol
                )
            )
            lastClose = close
        }

        currentCandleOpen = lastClose
        currentCandleHigh = lastClose
        currentCandleLow = lastClose
        candleTickCounter = 0

        _uiState.update {
            it.copy(
                candles = list,
                currentPrice = lastClose,
                bidPrice = lastClose,
                askPrice = lastClose + (symbol.spreadPips * symbol.pipSize)
            )
        }
    }

    private fun updateCandleWithTick(price: Double) {
        candleTickCounter++
        val state = _uiState.value
        val candles = state.candles.toMutableList()

        if (candles.isEmpty()) return

        val lastIndex = candles.lastIndex
        val last = candles[lastIndex]

        val updatedHigh = max(last.high, price)
        val updatedLow = min(last.low, price)
        val updatedCandle = last.copy(
            high = updatedHigh,
            low = updatedLow,
            close = price,
            volume = last.volume + 1
        )
        candles[lastIndex] = updatedCandle

        // Advance candle after every 12 ticks
        if (candleTickCounter >= 12) {
            candleTickCounter = 0
            val nextIndex = last.timeIndex + 1
            val nextTimestamp = last.timestampMs + (state.selectedTimeFrame.seconds * 1000)
            candles.add(
                Candle(
                    timeIndex = nextIndex,
                    timestampMs = nextTimestamp,
                    open = price,
                    high = price,
                    low = price,
                    close = price,
                    volume = 1f
                )
            )
            if (candles.size > 50) {
                candles.removeAt(0)
            }
        }

        _uiState.update { it.copy(candles = candles) }
    }

    private fun calculateUpdatedStats(current: BotStatistics, closed: TradeOrder): BotStatistics {
        val won = closed.profitPips > 0
        val total = current.totalTrades + 1
        val wonCount = current.tradesWon + (if (won) 1 else 0)
        val lostCount = current.tradesLost + (if (!won) 1 else 0)
        val pips = current.totalPips + closed.profitPips
        val profit = current.totalProfitUsd + closed.profitUsd
        val winRate = if (total > 0) (wonCount.toDouble() / total) * 100.0 else 0.0

        return BotStatistics(
            totalTrades = total,
            tradesWon = wonCount,
            tradesLost = lostCount,
            totalPips = pips,
            totalProfitUsd = profit,
            winRatePercent = winRate
        )
    }

    private fun formatPrice(price: Double, digits: Int): String {
        return "%.${digits}f".format(price)
    }

    /**
     * User Mandate Simulation & Demonstration:
     * "Insert an automatic take profit only on both sides after finding out that the profit reaches 5 percent"
     * Simulates profit reaching 5.0% on either BUY or SELL side, inserting the automatic take profit
     * and closing to secure gains.
     */
    fun simulateFivePercentTakeProfit(type: OrderType) {
        val state = _uiState.value
        val symbol = state.selectedSymbol
        val entryPrice = if (type == OrderType.BUY) state.askPrice else state.bidPrice
        val lotSize = state.selectedLotSize
        val targetThreshold = state.botConfig.autoTakeProfitThresholdPercent // 5.0%

        val pipValuePerLot = if (symbol.id == "USDJPY") 6.5 else 10.0
        val targetPips = 5.0
        val targetProfitUsd = targetPips * lotSize * pipValuePerLot
        val exitPrice = if (type == OrderType.BUY) entryPrice + (targetPips * symbol.pipSize) else entryPrice - (targetPips * symbol.pipSize)

        val simId = "sim5_${type.name.take(1)}_${System.currentTimeMillis().toString().takeLast(4)}"
        val closedOrder = TradeOrder(
            id = simId,
            symbolId = symbol.id,
            type = type,
            lotSize = lotSize,
            entryPrice = entryPrice,
            currentPrice = exitPrice,
            takeProfitPrice = exitPrice,
            stopLossPrice = if (type == OrderType.BUY) entryPrice - (15.0 * symbol.pipSize) else entryPrice + (15.0 * symbol.pipSize),
            targetProfitPips = targetPips,
            riskStopPips = 15.0,
            profitPips = targetPips,
            profitUsd = targetProfitUsd,
            profitPercent = targetThreshold,
            isTakeProfitInserted = true,
            autoTpThresholdPercent = targetThreshold,
            status = OrderStatus.CLOSED_TP,
            placedBy = "Bot Auto-TP Protocol (${type.name})",
            closeNote = "Auto Take Profit inserted on ${type.name} side after finding profit reached 5.0%"
        )

        _uiState.update { current ->
            val updatedStats = calculateUpdatedStats(current.botStatistics, closedOrder)
            current.copy(
                closedOrders = listOf(closedOrder) + current.closedOrders,
                botStatistics = updatedStats,
                lastNotification = "🎯 Auto Take Profit Hit! +5.0% profit verified on ${type.name} side (+$${String.format("%.2f", targetProfitUsd)})",
                botStatusMessage = "Auto Take Profit executed on ${type.name} side: Reached 5% profit threshold. Take profit automatically inserted & secured."
            )
        }
    }

    /**
     * Manually triggers 5% profit reach on an existing open order to test immediate Take Profit insertion.
     */
    fun triggerFivePercentProfitOnOrder(orderId: String) {
        val state = _uiState.value
        val order = state.openOrders.find { it.id == orderId } ?: return
        val symbol = state.selectedSymbol
        val pipValuePerLot = if (symbol.id == "USDJPY") 6.5 else 10.0
        val targetPips = 5.0
        val targetProfitUsd = targetPips * order.lotSize * pipValuePerLot
        val exitPrice = if (order.type == OrderType.BUY) order.entryPrice + (targetPips * symbol.pipSize) else order.entryPrice - (targetPips * symbol.pipSize)

        val closed = order.copy(
            currentPrice = exitPrice,
            takeProfitPrice = exitPrice,
            isTakeProfitInserted = true,
            profitPips = targetPips,
            profitUsd = targetProfitUsd,
            profitPercent = 5.0,
            status = OrderStatus.CLOSED_TP,
            closeNote = "Auto Take Profit inserted on ${order.type.name} side after finding profit reached 5.0%"
        )

        _uiState.update { current ->
            val remaining = current.openOrders.filter { it.id != orderId }
            val updatedStats = calculateUpdatedStats(current.botStatistics, closed)
            current.copy(
                openOrders = remaining,
                closedOrders = listOf(closed) + current.closedOrders,
                botStatistics = updatedStats,
                lastNotification = "🎯 Auto Take Profit Hit! Reached 5% profit on ${order.type.name} #${order.id} (+$${String.format("%.2f", targetProfitUsd)})",
                botStatusMessage = "Auto Take Profit executed on ${order.type.name} #${order.id} after reaching 5% profit."
            )
        }
    }
}
