package com.example.model

/**
 * Representation of an Exness MetaTrader 5 server node.
 */
data class ExnessServer(
    val name: String,
    val isDemo: Boolean,
    val region: String,
    val pingMs: Int
)

/**
 * Connection states for MT5 authentication handshake.
 */
sealed class Mt5ConnectionState {
    object Idle : Mt5ConnectionState()
    data class Connecting(val step: String, val progress: Float) : Mt5ConnectionState()
    data class Connected(val account: Mt5AccountSummary) : Mt5ConnectionState()
    data class Error(val message: String, val canRetry: Boolean = true) : Mt5ConnectionState()
}

/**
 * Connected MT5 account details.
 */
data class Mt5AccountSummary(
    val accountId: String,
    val server: String,
    val accountHolder: String = "Exness Trader",
    val accountType: String = "Raw Spread (MT5)",
    val currency: String = "USD",
    val balance: Double = 12450.75,
    val equity: Double = 12512.30,
    val margin: Double = 312.40,
    val freeMargin: Double = 12199.90,
    val marginLevelPercent: Double = 4005.2,
    val leverage: String = "1:2000",
    val isDemo: Boolean = false,
    val pingMs: Int = 14
)

/**
 * Saved account profile for quick switching.
 */
data class SavedAccount(
    val accountId: String,
    val serverName: String,
    val isDemo: Boolean,
    val lastConnected: String
)

/**
 * Standard Exness MT5 servers catalog with region and low-latency estimates.
 */
object ExnessServerCatalog {
    val servers: List<ExnessServer> = listOf(
        ExnessServer(name = "Exness-MT5Real", isDemo = false, region = "London, UK", pingMs = 12),
        ExnessServer(name = "Exness-MT5Real2", isDemo = false, region = "Amsterdam, NL", pingMs = 15),
        ExnessServer(name = "Exness-MT5Real3", isDemo = false, region = "Frankfurt, DE", pingMs = 16),
        ExnessServer(name = "Exness-MT5Real4", isDemo = false, region = "New York, USA", pingMs = 45),
        ExnessServer(name = "Exness-MT5Real5", isDemo = false, region = "Singapore, SG", pingMs = 28),
        ExnessServer(name = "Exness-MT5Real6", isDemo = false, region = "Hong Kong, HK", pingMs = 32),
        ExnessServer(name = "Exness-MT5Real7", isDemo = false, region = "Tokyo, JP", pingMs = 38),
        ExnessServer(name = "Exness-MT5Real8", isDemo = false, region = "Sydney, AU", pingMs = 74),
        ExnessServer(name = "Exness-MT5Real9", isDemo = false, region = "London, UK", pingMs = 14),
        ExnessServer(name = "Exness-MT5Real10", isDemo = false, region = "Dublin, IE", pingMs = 18),
        ExnessServer(name = "Exness-MT5Trial", isDemo = true, region = "London, UK (Demo)", pingMs = 11),
        ExnessServer(name = "Exness-MT5Trial2", isDemo = true, region = "Frankfurt, DE (Demo)", pingMs = 15),
        ExnessServer(name = "Exness-MT5Trial3", isDemo = true, region = "Amsterdam, NL (Demo)", pingMs = 17),
        ExnessServer(name = "Exness-MT5Trial4", isDemo = true, region = "Singapore, SG (Demo)", pingMs = 30)
    )

    val defaultReal = servers[0]
    val defaultDemo = servers.first { it.isDemo }
}
