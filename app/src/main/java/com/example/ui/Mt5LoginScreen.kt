package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.ui.components.ConnectedAccountCard
import com.example.ui.components.ExnessCredentialsInputCard
import com.example.ui.components.Mt5HelpBottomSheet
import com.example.ui.components.SavedAccountsRow
import com.example.ui.components.ServerSelectionCard
import com.example.ui.components.ServerSelectorModal
import com.example.ui.theme.ArtisticBg
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticSurfaceElevated
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary
import com.example.ui.theme.ArtisticTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mt5LoginScreen(
    viewModel: Mt5LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isConnected = state.connectionState is Mt5ConnectionState.Connected

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("mt5_login_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ArtisticPrimary, Color(0xFF006399))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Exness MT5 Gateway",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArtisticTextPrimary
                            )
                            Text(
                                text = "Direct MetaTrader 5 Bridge",
                                fontSize = 11.sp,
                                color = ArtisticTextMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowHelpDialog(true) },
                        modifier = Modifier.testTag("help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help Guide",
                            tint = ArtisticPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArtisticSurface
                )
            )
        },
        containerColor = ArtisticBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Hero Status Card
            HeroBanner(
                isConnected = isConnected,
                serverName = state.selectedServer.name
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isConnected) {
                val connectedState = state.connectionState as Mt5ConnectionState.Connected
                ConnectedAccountCard(
                    account = connectedState.account,
                    isReadOnly = state.readOnlyInvestorMode,
                    onDisconnect = { viewModel.disconnect() }
                )
            } else {
                // Saved Accounts quick switcher
                if (state.savedAccounts.isNotEmpty()) {
                    SavedAccountsRow(
                        savedAccounts = state.savedAccounts,
                        currentAccountId = state.accountId,
                        onSelectAccount = { viewModel.selectSavedAccount(it) }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // 1. Server Selection Card
                ServerSelectionCard(
                    server = state.selectedServer,
                    onClick = { viewModel.setShowServerPicker(true) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Account ID & Password Inputs
                ExnessCredentialsInputCard(
                    accountId = state.accountId,
                    password = state.password,
                    isPasswordVisible = state.isPasswordVisible,
                    rememberCredentials = state.rememberCredentials,
                    readOnlyMode = state.readOnlyInvestorMode,
                    connectionState = state.connectionState,
                    onAccountIdChange = { viewModel.onAccountIdChange(it) },
                    onPasswordChange = { viewModel.onPasswordChange(it) },
                    onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
                    onToggleRemember = { viewModel.toggleRememberCredentials(it) },
                    onToggleReadOnly = { viewModel.toggleReadOnlyInvestorMode(it) },
                    onConnectClick = { viewModel.connect() },
                    onQuickFillDemo = { viewModel.fillSampleCredentials(demo = true) },
                    onQuickFillLive = { viewModel.fillSampleCredentials(demo = false) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Security & Privacy Footer Note
                SecurityFooterNote()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Sheet for Server Selection
    if (state.showServerPicker) {
        ServerSelectorModal(
            servers = state.filteredServers,
            selectedServer = state.selectedServer,
            searchQuery = state.serverSearchQuery,
            currentFilter = state.serverFilter,
            onSearchChange = { viewModel.setServerSearchQuery(it) },
            onFilterChange = { viewModel.setServerFilter(it) },
            onServerSelect = { viewModel.onServerSelected(it) },
            onDismiss = { viewModel.setShowServerPicker(false) }
        )
    }

    // Modal Sheet for Finding Exness Credentials
    if (state.showHelpDialog) {
        Mt5HelpBottomSheet(
            onDismiss = { viewModel.setShowHelpDialog(false) }
        )
    }
}

@Composable
private fun HeroBanner(
    isConnected: Boolean,
    serverName: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(ArtisticSurfaceElevated, ArtisticSurface)
                )
            )
            .border(1.dp, ArtisticBorder.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("hero_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) ArtisticGreen else ArtisticPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "ONLINE & SYNCED" else "BRIDGE READY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConnected) ArtisticGreen else ArtisticPrimary,
                        letterSpacing = 0.6.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isConnected) "Live MT5 Terminal Active" else "Exness Terminal Integration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisticTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isConnected) "Receiving live bid/ask ticks & bot orders"
                    else "Connect your account to enable automatic bot execution",
                    fontSize = 11.sp,
                    color = ArtisticTextSecondary,
                    lineHeight = 15.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConnected) ArtisticGreen.copy(alpha = 0.12f)
                        else ArtisticPrimary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isConnected) ArtisticGreen else ArtisticPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SecurityFooterNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = ArtisticTextMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Direct TLS 1.3 encryption. Credentials stored locally on device.",
            fontSize = 11.sp,
            color = ArtisticTextMuted,
            fontWeight = FontWeight.Medium
        )
    }
}
