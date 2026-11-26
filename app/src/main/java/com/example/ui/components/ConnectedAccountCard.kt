package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Mt5AccountSummary
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticRed
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticSurfaceElevated
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary
import com.example.ui.theme.ArtisticTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ConnectedAccountCard(
    account: Mt5AccountSummary,
    isReadOnly: Boolean,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("connected_account_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
        border = BorderStroke(1.5.dp, ArtisticGreen.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Status + Disconnect
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ArtisticGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUTHENTICATED & ACTIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticGreen,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ArtisticGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${account.pingMs}ms Latency",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisticGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Account Title & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ArtisticPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = ArtisticPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Account #${account.accountId}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArtisticTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (account.isDemo) Color(0xFFE1F5FE) else Color(0xFFE8F5E9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (account.isDemo) "DEMO" else "LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (account.isDemo) Color(0xFF0277BD) else ArtisticGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${account.server} • ${account.accountType}",
                        fontSize = 12.sp,
                        color = ArtisticTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Financial Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBox(
                    label = "BALANCE",
                    value = currencyFormatter.format(account.balance),
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "EQUITY",
                    value = currencyFormatter.format(account.equity),
                    valueColor = ArtisticGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBox(
                    label = "FREE MARGIN",
                    value = currencyFormatter.format(account.freeMargin),
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    label = "LEVERAGE",
                    value = account.leverage,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trading Rights indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isReadOnly) Color(0xFFFFF3E0) else ArtisticPrimary.copy(alpha = 0.08f)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isReadOnly) Icons.Default.Security else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (isReadOnly) Color(0xFFE65100) else ArtisticPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isReadOnly) "Mode: Investor Read-Only (Audit mode, trades disabled)"
                    else "Mode: Master Trading Mode (Full Bot & Manual Execution)",
                    fontSize = 11.sp,
                    color = if (isReadOnly) Color(0xFFE65100) else ArtisticPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("disconnect_account_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ArtisticRed.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ArtisticRed)
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DISCONNECT TERMINAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArtisticRed
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    valueColor: Color = ArtisticTextPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ArtisticSurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ArtisticTextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}
