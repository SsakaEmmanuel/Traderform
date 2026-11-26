package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.model.SavedAccount
import com.example.ui.theme.ArtisticBorder
import com.example.ui.theme.ArtisticGreen
import com.example.ui.theme.ArtisticPrimary
import com.example.ui.theme.ArtisticSurface
import com.example.ui.theme.ArtisticTextMuted
import com.example.ui.theme.ArtisticTextPrimary
import com.example.ui.theme.ArtisticTextSecondary

@Composable
fun SavedAccountsRow(
    savedAccounts: List<SavedAccount>,
    currentAccountId: String,
    onSelectAccount: (SavedAccount) -> Unit,
    modifier: Modifier = Modifier
) {
    if (savedAccounts.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "SAVED ACCOUNTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = ArtisticTextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("saved_accounts_row")
        ) {
            items(savedAccounts, key = { it.accountId }) { account ->
                val isCurrent = account.accountId == currentAccountId
                SavedAccountChip(
                    account = account,
                    isCurrent = isCurrent,
                    onClick = { onSelectAccount(account) }
                )
            }
        }
    }
}

@Composable
private fun SavedAccountChip(
    account: SavedAccount,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("saved_account_${account.accountId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ArtisticSurface),
        border = BorderStroke(
            1.dp,
            if (isCurrent) ArtisticPrimary else ArtisticBorder.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (account.isDemo) Color(0xFF0277BD).copy(alpha = 0.12f)
                        else ArtisticPrimary.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = if (account.isDemo) Color(0xFF0277BD) else ArtisticPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${account.accountId}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticTextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (account.isDemo) Color(0xFFE1F5FE) else Color(0xFFE8F5E9))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (account.isDemo) "DEMO" else "LIVE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (account.isDemo) Color(0xFF0277BD) else ArtisticGreen
                        )
                    }
                }
                Text(
                    text = account.serverName,
                    fontSize = 10.sp,
                    color = ArtisticTextMuted
                )
            }
        }
    }
}
