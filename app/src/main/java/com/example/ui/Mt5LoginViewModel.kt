package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ExnessServer
import com.example.model.ExnessServerCatalog
import com.example.model.Mt5AccountSummary
import com.example.model.Mt5ConnectionState
import com.example.model.SavedAccount
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ServerFilter {
    ALL, REAL, DEMO
}

data class Mt5LoginUiState(
    val selectedServer: ExnessServer = ExnessServerCatalog.defaultReal,
    val accountId: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberCredentials: Boolean = true,
    val readOnlyInvestorMode: Boolean = false,
    val serverSearchQuery: String = "",
    val serverFilter: ServerFilter = ServerFilter.ALL,
    val showServerPicker: Boolean = false,
    val showHelpDialog: Boolean = false,
    val connectionState: Mt5ConnectionState = Mt5ConnectionState.Idle,
    val savedAccounts: List<SavedAccount> = listOf(
        SavedAccount(accountId = "82940182", serverName = "Exness-MT5Real", isDemo = false, lastConnected = "Yesterday"),
        SavedAccount(accountId = "51093844", serverName = "Exness-MT5Trial", isDemo = true, lastConnected = "3 days ago")
    )
) {
    val filteredServers: List<ExnessServer>
        get() {
            return ExnessServerCatalog.servers.filter { server ->
                val matchesCategory = when (serverFilter) {
                    ServerFilter.ALL -> true
                    ServerFilter.REAL -> !server.isDemo
                    ServerFilter.DEMO -> server.isDemo
                }
                val matchesQuery = serverSearchQuery.isBlank() ||
                        server.name.contains(serverSearchQuery, ignoreCase = true) ||
                        server.region.contains(serverSearchQuery, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }

    val isValidInput: Boolean
        get() = accountId.trim().length in 5..12 && password.length >= 6
}

class Mt5LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(Mt5LoginUiState())
    val uiState: StateFlow<Mt5LoginUiState> = _uiState.asStateFlow()

    private var connectionJob: Job? = null

    fun onAccountIdChange(newId: String) {
        // Keep only alphanumeric/digits for account ID
        val sanitized = newId.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(accountId = sanitized) }
    }

    fun onPasswordChange(newPass: String) {
        _uiState.update { it.copy(password = newPass) }
    }

    fun onServerSelected(server: ExnessServer) {
        _uiState.update {
            it.copy(
                selectedServer = server,
                showServerPicker = false,
                serverSearchQuery = ""
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleRememberCredentials(remember: Boolean) {
        _uiState.update { it.copy(rememberCredentials = remember) }
    }

    fun toggleReadOnlyInvestorMode(readOnly: Boolean) {
        _uiState.update { it.copy(readOnlyInvestorMode = readOnly) }
    }

    fun setServerFilter(filter: ServerFilter) {
        _uiState.update { it.copy(serverFilter = filter) }
    }

    fun setServerSearchQuery(query: String) {
        _uiState.update { it.copy(serverSearchQuery = query) }
    }

    fun setShowServerPicker(show: Boolean) {
        _uiState.update { it.copy(showServerPicker = show) }
    }

    fun setShowHelpDialog(show: Boolean) {
        _uiState.update { it.copy(showHelpDialog = show) }
    }

    fun selectSavedAccount(saved: SavedAccount) {
        val matchingServer = ExnessServerCatalog.servers.find { it.name == saved.serverName }
            ?: if (saved.isDemo) ExnessServerCatalog.defaultDemo else ExnessServerCatalog.defaultReal

        _uiState.update {
            it.copy(
                accountId = saved.accountId,
                selectedServer = matchingServer,
                password = "••••••••••" // Masked placeholder indication
            )
        }
    }

    fun fillSampleCredentials(demo: Boolean) {
        val server = if (demo) ExnessServerCatalog.defaultDemo else ExnessServerCatalog.defaultReal
        val id = if (demo) "51093844" else "82940182"
        val pwd = if (demo) "ExnessDemo@2026" else "TradePro#994"

        _uiState.update {
            it.copy(
                selectedServer = server,
                accountId = id,
                password = pwd
            )
        }
    }

    fun connect() {
        val state = _uiState.value
        if (state.accountId.trim().length < 5) {
            _uiState.update {
                it.copy(
                    connectionState = Mt5ConnectionState.Error(
                        "Exness MT5 Account ID must be at least 5 digits.",
                        canRetry = true
                    )
                )
            }
            return
        }
        if (state.password.length < 6) {
            _uiState.update {
                it.copy(
                    connectionState = Mt5ConnectionState.Error(
                        "Password must be at least 6 characters.",
                        canRetry = true
                    )
                )
            }
            return
        }

        connectionJob?.cancel()
        connectionJob = viewModelScope.launch {
            // Stage 1: DNS & Gateway Resolution
            _uiState.update {
                it.copy(
                    connectionState = Mt5ConnectionState.Connecting(
                        step = "Connecting to gateway (${it.selectedServer.name})...",
                        progress = 0.25f
                    )
                )
            }
            delay(600)

            // Stage 2: Handshake & Credentials Check
            _uiState.update {
                it.copy(
                    connectionState = Mt5ConnectionState.Connecting(
                        step = "Verifying MT5 Login #${it.accountId}...",
                        progress = 0.65f
                    )
                )
            }
            delay(700)

            // Stage 3: Account Sync & Ping
            _uiState.update {
                it.copy(
                    connectionState = Mt5ConnectionState.Connecting(
                        step = "Synchronizing balance, leverage & trading rights...",
                        progress = 0.90f
                    )
                )
            }
            delay(500)

            val isDemo = state.selectedServer.isDemo
            val summary = Mt5AccountSummary(
                accountId = state.accountId,
                server = state.selectedServer.name,
                accountHolder = if (isDemo) "Exness Trial Trader" else "Exness Pro Trader",
                accountType = if (isDemo) "Trial Standard (MT5)" else "Pro Zero Spread (MT5)",
                currency = "USD",
                balance = if (isDemo) 10000.00 else 24850.50,
                equity = if (isDemo) 10045.20 else 25112.80,
                margin = if (isDemo) 120.00 else 450.00,
                freeMargin = if (isDemo) 9925.20 else 24662.80,
                marginLevelPercent = if (isDemo) 8371.0 else 5580.6,
                leverage = "1:2000",
                isDemo = isDemo,
                pingMs = state.selectedServer.pingMs
            )

            _uiState.update { current ->
                val updatedSaved = if (current.rememberCredentials) {
                    val alreadyInList = current.savedAccounts.any { it.accountId == current.accountId }
                    if (!alreadyInList) {
                        listOf(
                            SavedAccount(
                                accountId = current.accountId,
                                serverName = current.selectedServer.name,
                                isDemo = isDemo,
                                lastConnected = "Just now"
                            )
                        ) + current.savedAccounts
                    } else current.savedAccounts
                } else current.savedAccounts

                current.copy(
                    connectionState = Mt5ConnectionState.Connected(summary),
                    savedAccounts = updatedSaved
                )
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        _uiState.update { it.copy(connectionState = Mt5ConnectionState.Idle) }
    }

    fun dismissError() {
        _uiState.update { it.copy(connectionState = Mt5ConnectionState.Idle) }
    }
}
