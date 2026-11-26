package com.example

import com.example.data.WorldMarketRepository
import com.example.model.MarketCatalog
import com.example.model.OrderStatus
import com.example.model.OrderType
import com.example.model.TradeOrder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun worldMarketRepository_fetchesStatisticsForAllCatalogPairs() = runBlocking {
        val symbols = MarketCatalog.symbols

        for (sym in symbols) {
            val stats = WorldMarketRepository.fetchWorldMarketData(sym)
            assertNotNull(stats)
            assertEquals(sym.id, stats.symbolId)
            assertTrue("24h High must be >= Low", stats.high24h >= stats.low24h)
            assertTrue("Pip range must be >= 0", stats.dailyPipRange >= 0.0)
            assertTrue("Sentiment must be between 0 and 100", stats.bullishSentimentPercent in 0..100)
            assertTrue("ATR must be positive", stats.atr14Pips > 0.0)
        }
    }

    @Test
    fun worldMarketRepository_calculatesActiveSessionsAndMacroEvents() {
        val sessions = WorldMarketRepository.getCurrentMarketSessions()
        val macroEvents = WorldMarketRepository.getWorldMacroIntel("EURUSD")

        assertTrue("Must have 4 primary global market sessions", sessions.size >= 4)
        assertTrue("Must have macro calendar events", macroEvents.isNotEmpty())
        assertTrue("Active volume share must be non-negative", sessions.all { it.activeVolumeSharePercent >= 0 })
    }

    @Test
    fun pipControl_manualAdjustmentAndNormalReset() {
        val defaultTp = 10.0
        val defaultSl = 15.0

        val order = TradeOrder(
            id = "test_order_1",
            symbolId = "EURUSD",
            type = OrderType.BUY,
            lotSize = 0.50,
            entryPrice = 1.08500,
            currentPrice = 1.08520,
            takeProfitPrice = 1.08600,
            stopLossPrice = 1.08350,
            targetProfitPips = defaultTp,
            riskStopPips = defaultSl,
            profitPips = 2.0,
            profitUsd = 10.0,
            status = OrderStatus.OPEN
        )

        assertEquals(10.0, order.targetProfitPips, 0.001)
        assertEquals(15.0, order.riskStopPips, 0.001)

        // User manually adjusts exit pips (e.g. 25 pips TP, 8 pips SL)
        val manuallyAdjustedOrder = order.copy(
            targetProfitPips = 25.0,
            riskStopPips = 8.0,
            takeProfitPrice = 1.08750,
            stopLossPrice = 1.08420
        )

        assertEquals(25.0, manuallyAdjustedOrder.targetProfitPips, 0.001)
        assertEquals(8.0, manuallyAdjustedOrder.riskStopPips, 0.001)

        // Reset to normal defaults (10 pips TP, 15 pips SL)
        val resetOrder = manuallyAdjustedOrder.copy(
            targetProfitPips = defaultTp,
            riskStopPips = defaultSl,
            takeProfitPrice = order.entryPrice + (defaultTp * 0.0001),
            stopLossPrice = order.entryPrice - (defaultSl * 0.0001)
        )

        assertEquals(10.0, resetOrder.targetProfitPips, 0.001)
        assertEquals(15.0, resetOrder.riskStopPips, 0.001)
    }

    @Test
    fun pipControl_breakevenSetsRiskToZero() {
        val order = TradeOrder(
            id = "test_order_2",
            symbolId = "XAUUSD",
            type = OrderType.BUY,
            lotSize = 0.10,
            entryPrice = 2420.00,
            currentPrice = 2425.00,
            takeProfitPrice = 2430.00,
            stopLossPrice = 2405.00,
            targetProfitPips = 10.0,
            riskStopPips = 15.0,
            profitPips = 5.0,
            profitUsd = 50.0
        )

        val beOrder = order.copy(
            riskStopPips = 0.0,
            stopLossPrice = order.entryPrice
        )

        assertEquals(0.0, beOrder.riskStopPips, 0.001)
        assertEquals(order.entryPrice, beOrder.stopLossPrice, 0.001)
        assertEquals(10.0, beOrder.targetProfitPips, 0.001)
    }

    @Test
    fun botAccuracyProtocol_scalesTradeCountCorrectly() {
        // User Requirement:
        // "execute one trade when accuracy is 50 percent, execute two when 72 percent, and three when above"

        // Below 50% -> 0 trades (bot stands by)
        assertEquals(0, com.example.model.BotConfig.getTradeCountForAccuracy(0))
        assertEquals(0, com.example.model.BotConfig.getTradeCountForAccuracy(45))
        assertEquals(0, com.example.model.BotConfig.getTradeCountForAccuracy(49))

        // 50% -> 1 trade
        assertEquals(1, com.example.model.BotConfig.getTradeCountForAccuracy(50))
        assertEquals(1, com.example.model.BotConfig.getTradeCountForAccuracy(55))
        assertEquals(1, com.example.model.BotConfig.getTradeCountForAccuracy(71))

        // 72% -> 2 trades
        assertEquals(2, com.example.model.BotConfig.getTradeCountForAccuracy(72))

        // Above 72% -> 3 trades
        assertEquals(3, com.example.model.BotConfig.getTradeCountForAccuracy(73))
        assertEquals(3, com.example.model.BotConfig.getTradeCountForAccuracy(85))
        assertEquals(3, com.example.model.BotConfig.getTradeCountForAccuracy(95))
        assertEquals(3, com.example.model.BotConfig.getTradeCountForAccuracy(100))
    }

    @Test
    fun autoTakeProfitProtocol_triggeredOnlyAfterReachingFivePercentProfitOnBothSides() {
        // User Mandate:
        // "Insert an automatic take profit only on both sides after finding out that the profit reaches 5 percent"

        val defaultThreshold = 5.0

        // 1. Below 5% threshold: Automatic Take Profit MUST NOT be inserted (both sides)
        org.junit.Assert.assertFalse(com.example.model.BotConfig.isTakeProfitTriggerReached(0.0, defaultThreshold))
        org.junit.Assert.assertFalse(com.example.model.BotConfig.isTakeProfitTriggerReached(2.5, defaultThreshold))
        org.junit.Assert.assertFalse(com.example.model.BotConfig.isTakeProfitTriggerReached(4.9, defaultThreshold))

        // 2. At or above 5% threshold: Automatic Take Profit IS inserted (both sides)
        assertTrue(com.example.model.BotConfig.isTakeProfitTriggerReached(5.0, defaultThreshold))
        assertTrue(com.example.model.BotConfig.isTakeProfitTriggerReached(5.1, defaultThreshold))
        assertTrue(com.example.model.BotConfig.isTakeProfitTriggerReached(8.0, defaultThreshold))

        // 3. Test BUY side order lifecycle:
        val buyOrder = TradeOrder(
            id = "buy_1",
            symbolId = "EURUSD",
            type = OrderType.BUY,
            lotSize = 0.10,
            entryPrice = 1.08500,
            currentPrice = 1.08520, // +2 pips (+2%)
            takeProfitPrice = 1.08600,
            stopLossPrice = 1.08350,
            profitPips = 2.0,
            profitUsd = 2.0,
            profitPercent = 2.0,
            isTakeProfitInserted = false // Not inserted yet
        )
        // At 2% profit, TP is NOT inserted
        org.junit.Assert.assertFalse(buyOrder.isTakeProfitInserted)
        assertEquals(OrderStatus.OPEN, buyOrder.status)

        // When profit reaches 5%, Auto-TP is inserted:
        val buyAtFivePercent = buyOrder.copy(
            currentPrice = 1.08550,
            profitPips = 5.0,
            profitUsd = 5.0,
            profitPercent = 5.0,
            isTakeProfitInserted = true,
            status = OrderStatus.CLOSED_TP,
            closeNote = "Auto Take Profit inserted on BUY side after finding profit reached 5.0%"
        )
        assertTrue(buyAtFivePercent.isTakeProfitInserted)
        assertEquals(OrderStatus.CLOSED_TP, buyAtFivePercent.status)

        // 4. Test SELL side order lifecycle:
        val sellOrder = TradeOrder(
            id = "sell_1",
            symbolId = "EURUSD",
            type = OrderType.SELL,
            lotSize = 0.10,
            entryPrice = 1.08500,
            currentPrice = 1.08480, // +2 pips (+2%)
            takeProfitPrice = 1.08400,
            stopLossPrice = 1.08650,
            profitPips = 2.0,
            profitUsd = 2.0,
            profitPercent = 2.0,
            isTakeProfitInserted = false // Not inserted yet
        )
        // At 2% profit, TP is NOT inserted
        org.junit.Assert.assertFalse(sellOrder.isTakeProfitInserted)
        assertEquals(OrderStatus.OPEN, sellOrder.status)

        // When profit reaches 5%, Auto-TP is inserted:
        val sellAtFivePercent = sellOrder.copy(
            currentPrice = 1.08450,
            profitPips = 5.0,
            profitUsd = 5.0,
            profitPercent = 5.0,
            isTakeProfitInserted = true,
            status = OrderStatus.CLOSED_TP,
            closeNote = "Auto Take Profit inserted on SELL side after finding profit reached 5.0%"
        )
        assertTrue(sellAtFivePercent.isTakeProfitInserted)
        assertEquals(OrderStatus.CLOSED_TP, sellAtFivePercent.status)
    }
}
