package com.example.model

import java.util.UUID

enum class OrderType {
    BUY, SELL
}

enum class OrderStatus {
    OPEN, CLOSED_TP, CLOSED_SL, CANCELLED_MANUAL
}

enum class SignalType {
    STRONG_BUY, BUY, NEUTRAL, SELL, STRONG_SELL
}

enum class TimeFrame(val label: String, val seconds: Long) {
    TF_1M("1m", 60),
    TF_5M("5m", 300),
    TF_15M("15m", 900),
    TF_30M("30m", 1800),
    TF_1H("1hr", 3600),
    TF_4H("4hr", 14400)
}

data class MarketSymbol(
    val id: String,
    val name: String,
    val category: String,
    val basePrice: Double,
    val pipSize: Double,
    val digits: Int,
    val spreadPips: Double = 0.8
)

object MarketCatalog {
    val symbols = listOf(
        MarketSymbol(id = "EURUSD", name = "EUR/USD", category = "Forex Major", basePrice = 1.08520, pipSize = 0.0001, digits = 5),
        MarketSymbol(id = "GBPUSD", name = "GBP/USD", category = "Forex Major", basePrice = 1.27350, pipSize = 0.0001, digits = 5),
        MarketSymbol(id = "USDJPY", name = "USD/JPY", category = "Forex Major", basePrice = 154.420, pipSize = 0.01, digits = 3),
        MarketSymbol(id = "AUDUSD", name = "AUD/USD", category = "Forex Major", basePrice = 0.65840, pipSize = 0.0001, digits = 5),
        MarketSymbol(id = "USDCAD", name = "USD/CAD", category = "Forex Major", basePrice = 1.36480, pipSize = 0.0001, digits = 5),
        MarketSymbol(id = "XAUUSD", name = "XAU/USD (Gold)", category = "Precious Metals", basePrice = 2382.40, pipSize = 0.1, digits = 2),
        MarketSymbol(id = "BTCUSD", name = "BTC/USD", category = "Crypto MT5", basePrice = 64320.00, pipSize = 1.0, digits = 2)
    )

    val defaultSymbol = symbols[0]
}

data class Candle(
    val timeIndex: Int,
    val timestampMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Float
) {
    val isBullish: Boolean get() = close >= open
}

data class TradeOrder(
    val id: String = UUID.randomUUID().toString().take(8),
    val symbolId: String,
    val type: OrderType,
    val lotSize: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val takeProfitPrice: Double,
    val stopLossPrice: Double,
    val targetProfitPips: Double = 10.0,
    val riskStopPips: Double = 15.0,
    val profitPips: Double,
    val profitUsd: Double,
    val profitPercent: Double = 0.0,
    val isTakeProfitInserted: Boolean = false, // Set to true only after profit reaches 5% on either side
    val autoTpThresholdPercent: Double = 5.0, // Automatic Take Profit inserted only after 5% profit
    val status: OrderStatus = OrderStatus.OPEN,
    val placedBy: String = "Bot", // "User Manual" or "Auto Bot"
    val timestamp: Long = System.currentTimeMillis(),
    val closeNote: String? = null
)

data class IndicatorConfig(
    val showMaMatrix: Boolean = true,  // 4-MA matrix (9, 21, 50, 200)
    val showRsi: Boolean = true,       // RSI (14)
    val showMacd: Boolean = false,     // MACD (12, 26, 9)
    val ma1Period: Int = 9,
    val ma2Period: Int = 21,
    val ma3Period: Int = 50,
    val ma4Period: Int = 200,
    val rsiPeriod: Int = 14
)

data class WorldMarketStats(
    val symbolId: String,
    val price: Double,
    val high24h: Double,
    val low24h: Double,
    val change24h: Double,
    val changePercent24h: Double,
    val volume24hUsd: Double, // in USD
    val dailyPipRange: Double,
    val atr14Pips: Double,
    val bullishSentimentPercent: Int, // e.g. 62%
    val bearishSentimentPercent: Int, // 38%
    val liquidityRating: String,      // "Ultra High", "High", "Moderate"
    val volatilityLevel: String,      // "Low", "Normal", "Elevated", "Extreme"
    val usdCorrelation: Double,       // e.g. -0.92
    val lastSyncTimeFormatted: String,
    val syncLatencyMs: Long = 45,
    val isLiveSynced: Boolean = true,
    val dataSource: String = "Global Interbank & Liquidity Aggregator"
)

data class MarketSessionInfo(
    val name: String,
    val city: String,
    val flag: String,
    val isOpen: Boolean,
    val openUtcHour: Int,
    val closeUtcHour: Int,
    val activeVolumeSharePercent: Int,
    val currentRole: String
)

data class WorldMacroEvent(
    val id: String,
    val symbolId: String,
    val title: String,
    val impact: String, // "HIGH", "MEDIUM", "LOW"
    val timeAgo: String,
    val sentiment: String, // "Bullish", "Bearish", "Neutral"
    val details: String,
    val source: String
)

data class MarketAnalysis(
    val symbolId: String,
    val timeFrame: TimeFrame,
    val signal: SignalType,
    val confidencePercent: Int,
    val rsi14: Double,
    val macdHistogram: Double,
    val trendDescription: String,
    val supportLevel: Double,
    val resistanceLevel: Double,
    val recommendedEntry: Double,
    val targetPips: Double = 10.0,
    val riskStopPips: Double = 15.0,
    val summaryText: String
)

data class BotConfig(
    val isAutoTradingEnabled: Boolean = false,
    val targetProfitPips: Double = 10.0, // Fixed user requirement: 10 pips
    val riskStopPips: Double = 15.0,      // Fixed user requirement: 15 pips cancel
    val defaultLotSize: Double = 0.10,
    val minSignalConfidence: Int = 50,    // 50% minimum threshold for 1 trade
    val cooldownSeconds: Int = 3,
    val autoTakeProfitThresholdPercent: Double = 5.0 // Automatic Take Profit inserted only after 5% profit on both sides
) {
    companion object {
        /**
         * Calculates the number of trades executed by the bot based on signal accuracy:
         * - 50% accuracy (or 50% to <72%): 1 trade
         * - 72% accuracy: 2 trades
         * - Above 72% accuracy (>72%): 3 trades
         * - Below 50%: 0 trades (bot stands by)
         */
        fun getTradeCountForAccuracy(accuracy: Int): Int {
            return when {
                accuracy > 72 -> 3
                accuracy == 72 -> 2
                accuracy >= 50 -> 1
                else -> 0
            }
        }

        fun getAccuracyTierLabel(accuracy: Int): String {
            return when {
                accuracy > 72 -> "Above 72% (3 Trades)"
                accuracy == 72 -> "72% (2 Trades)"
                accuracy >= 50 -> "50% (1 Trade)"
                else -> "< 50% (Standby)"
            }
        }

        /**
         * User mandate:
         * "Insert an automatic take profit only on both sides after finding out that the profit reaches 5 percent"
         * Returns true if profit has reached or exceeded 5.0% threshold.
         */
        fun isTakeProfitTriggerReached(profitPercent: Double, thresholdPercent: Double = 5.0): Boolean {
            return profitPercent >= thresholdPercent
        }
    }
}
