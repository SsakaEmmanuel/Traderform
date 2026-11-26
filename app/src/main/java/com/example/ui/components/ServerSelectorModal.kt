package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExnessServer
import com.example.ui.ServerFilter
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
fun ServerSelectorModal(
    servers: List<ExnessServer>,
    selectedServer: ExnessServer,
    searchQuery: String,
    currentFilter: ServerFilter,
    onSearchChange: (String) -> Unit,
    onFilterChange: (ServerFilter) -> Unit,
    onServerSelect: (ExnessServer) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ArtisticSurfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ArtisticBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("server_selector_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Select Exness MT5 Server",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArtisticTextPrimary
                    )
                    Text(
                        text = "Choose your assigned trading server cluster",
                        fontSize = 12.sp,
                        color = ArtisticTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArtisticPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${servers.size} Servers",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisticPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_search_input"),
                placeholder = {
                    Text("Search server name or location...", fontSize = 13.sp, color = ArtisticTextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ArtisticPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = ArtisticTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ArtisticPrimary,
                    unfocusedBorderColor = ArtisticBorder,
                    focusedContainerColor = ArtisticSurface,
                    unfocusedContainerColor = ArtisticSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips: ALL, REAL, DEMO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServerFilterChip(
                    label = "All Servers",
                    selected = currentFilter == ServerFilter.ALL,
                    onClick = { onFilterChange(ServerFilter.ALL) },
                    testTag = "filter_all"
                )
                ServerFilterChip(
                    label = "Real Accounts",
                    selected = currentFilter == ServerFilter.REAL,
                    onClick = { onFilterChange(ServerFilter.REAL) },
                    testTag = "filter_real"
                )
                ServerFilterChip(
                    label = "Trial / Demo",
                    selected = currentFilter == ServerFilter.DEMO,
                    onClick = { onFilterChange(ServerFilter.DEMO) },
                    testTag = "filter_demo"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of servers
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .testTag("server_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(servers, key = { it.name }) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = server.name == selectedServer.name,
                        onClick = { onServerSelect(server) }
                    )
                }

                if (servers.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = ArtisticTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No matching Exness servers found",
                                fontSize = 13.sp,
                                color = ArtisticTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ServerFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ArtisticPrimary,
            selectedLabelColor = Color.White,
            containerColor = ArtisticSurface,
            labelColor = ArtisticTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = ArtisticBorder,
            selectedBorderColor = ArtisticPrimary
        ),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
private fun ServerListItem(
    server: ExnessServer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ArtisticPrimary else ArtisticBorder.copy(alpha = 0.5f)
    val bgColor = if (isSelected) ArtisticPrimary.copy(alpha = 0.08f) else ArtisticSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("server_item_${server.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (server.isDemo) Color(0xFF006399).copy(alpha = 0.12f) else ArtisticPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = if (server.isDemo) Color(0xFF006399) else ArtisticPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ArtisticTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (server.isDemo) Color(0xFFE1F5FE) else Color(0xFFE8F5E9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (server.isDemo) "DEMO" else "REAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (server.isDemo) Color(0xFF0277BD) else ArtisticGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = ArtisticTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = server.region,
                        fontSize = 11.sp,
                        color = ArtisticTextMuted
                    )
                }
            }
        }

        // Ping & Checkmark
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArtisticGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ArtisticGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${server.pingMs}ms",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArtisticGreen
                    )
                }
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ArtisticPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
